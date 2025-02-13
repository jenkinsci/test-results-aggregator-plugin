package com.jenkins.testresultsaggregator;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.actions.Analyzer;
import com.jenkins.testresultsaggregator.actions.Collector;
import com.jenkins.testresultsaggregator.actions.Reporter;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.DataPipeline;
import com.jenkins.testresultsaggregator.data.GlobalConfigDTO;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.helper.LocalMessages;
import com.offbytwo.jenkins.JenkinsServer;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

public class TestResultsAggregator extends TestResultsAggregatorHelper implements SimpleBuildStep {
	
	private static final String displayName = "Aggregate Test Results";
	
	private String subject;
	private String recipientsList;
	private String recipientsListCC;
	private String recipientsListBCC;
	private String recipientsListIgnored;
	private String beforebody;
	private String afterbody;
	private String theme;
	private String sortresults;
	private String outOfDateResults;
	private Boolean compareWithPreviousRun;
	private Boolean ignoreNotFoundJobs;
	private Boolean ignoreDisabledJobs;
	private Boolean ignoreAbortedJobs;
	private Boolean ignoreRunningJobs;
	private String columns;
	private List<Data> data;
	private List<DataPipeline> jobs;
	
	private String influxdbUrl;
	private String influxdbToken;
	private String influxdbBucket;
	private String influxdbOrg;
	
	private String overrideJenkinsBaseURL;
	private String overrideAPIAccountUsername;
	private String overrideAPIAccountPassword;
	private String overrideMailNotificationFrom;
	
	private Properties properties;
	private GlobalConfigDTO globalConfigDTO;
	
	public static final String DISPLAY_NAME = "Job Results Aggregated";
	public static final String GRAPH_NAME_JOBS = "Job Results Trend";
	public static final String GRAPH_NAME_TESTS = "Test Results Trend";
	public static final String URL = "reports";
	public static final String ICON_FILE_NAME = "/plugin/test-results-aggregator/icons/report.png";
	
	public enum AggregatorProperties {
		OUT_OF_DATE_RESULTS_ARG,
		TEST_PERCENTAGE_PREFIX,
		TEXT_BEFORE_MAIL_BODY,
		TEXT_AFTER_MAIL_BODY,
		THEME,
		SORT_JOBS_BY,
		SUBJECT_PREFIX,
		RECIPIENTS_LIST,
		RECIPIENTS_LIST_CC,
		RECIPIENTS_LIST_BCC,
		RECIPIENTS_LIST_IGNORED,
		IGNORE_NOTFOUND_JOBS,
		IGNORE_DISABLED_JOBS,
		IGNORE_ABORTED_JOBS,
		IGNORE_RUNNING_JOBS,
		COMPARE_WITH_PREVIOUS_RUN,
		
		INFLUXDB_URL,
		INFLUXDB_TOKEN,
		INFLUXDB_BUCKET,
		INFLUXDB_ORG,
		
		OVERRIDE_JENKINS_BASEURL,
		OVERRIDE_API_ACCOUNT_USERNAME,
		OVERRIDE_API_ACCOUNT_PASSWORD,
		OVERRIDE_MAIL_NOTIFICATION_FROM;
	}
	
	public enum SortResultsBy {
		NAME,
		STATUS,
		TOTAL_TEST,
		PASS,
		FAIL,
		SKIP,
		LAST_RUN,
		COMMITS,
		DURATION,
		PERCENTAGE,
		CC_PACKAGES,
		CC_FILES,
		CC_CLASSES,
		CC_METHODS,
		CC_LINES,
		CC_CONDITIONS,
		SONAR_URL,
		BUILD_NUMBER
	}
	
	public enum Theme {
		dark,
		light
	}
	
	@DataBoundConstructor
	public TestResultsAggregator(final String subject, final String recipientsList, final String recipientsListCC, final String recipientsListBCC, final String recipientsListIgnored, final String outOfDateResults,
			final List<Data> data, final List<DataPipeline> jobs, String beforebody, String afterbody, String theme,
			String sortresults,
			String columns, Boolean compareWithPreviousRun, Boolean ignoreNotFoundJobs, Boolean ignoreDisabledJobs, Boolean ignoreAbortedJobs, Boolean ignoreRunningJobs,
			String influxdbUrl, String influxdbToken, String influxdbBucket, String influxdbOrg,
			String overrideJenkinsBaseURL, String overrideAPIAccountUsername, String overrideAPIAccountPassword, String overrideMailNotificationFrom) {
		this.setRecipientsList(recipientsList);
		this.setRecipientsListBCC(recipientsListBCC);
		this.setRecipientsListCC(recipientsListCC);
		this.setRecipientsListIgnored(recipientsListIgnored);
		this.setOutOfDateResults(outOfDateResults);
		this.setData(data);
		this.setBeforebody(beforebody);
		this.setAfterbody(afterbody);
		this.setTheme(theme);
		this.setSortresults(sortresults);
		this.setSubject(subject);
		this.setColumns(columns);
		this.setCompareWithPreviousRun(compareWithPreviousRun);
		this.setIgnoreDisabledJobs(ignoreDisabledJobs);
		this.setIgnoreNotFoundJobs(ignoreNotFoundJobs);
		this.setIgnoreAbortedJobs(ignoreAbortedJobs);
		this.setIgnoreRunningJobs(ignoreRunningJobs);
		this.setJobs(jobs);
		this.setInfluxdbUrl(influxdbUrl);
		this.setInfluxdbToken(influxdbToken);
		this.setInfluxdbBucket(influxdbBucket);
		this.setInfluxdbOrg(influxdbOrg);
		this.setOverrideAPIAccountPassword(overrideAPIAccountPassword);
		this.setOverrideAPIAccountUsername(overrideAPIAccountUsername);
		this.setOverrideJenkinsBaseURL(overrideJenkinsBaseURL);
		this.setOverrideMailNotificationFrom(overrideMailNotificationFrom);
	}
	
	/* In use from Pipeline Syntax */
	@Override
	public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
		PrintStream logger = listener.getLogger();
		logger.println(LocalMessages.START_AGGREGATE.toString());
		Descriptor desc = getDescriptor();
		globalConfigDTO = new GlobalConfigDTO(resolveJenkinsUrl(env, logger), desc.getUsername(), desc.getPassword().getPlainText(), desc.getMailNotificationFrom());
		//
		initProperties();
		// Override Global config
		globalConfigDTO = overrideGlobalConfigiguration(globalConfigDTO);
		// Resolve Variables
		resolveVariables(properties, null, run.getEnvironment(listener));
		// Resolve Columns
		List<LocalMessages> localizedColumns = calculateColumns(getColumns());
		
		// Validate Input Data
		Aggregated aggregatedSavedData = null;
		List<Data> validatedData = validateInputData(getDataFromDataPipeline(), globalConfigDTO.getJenkinsUrl());
		validatedData = checkUserInputForInjection(validatedData);
		//
		if (compareWithPrevious()) {
			aggregatedSavedData = getPreviousData2(run.getPreviousSuccessfulBuild(), validatedData);
		}
		//
		boolean configChanges = checkConfigChanges(logger, aggregatedSavedData);
		// Collect Data
		Collector collector = new Collector(globalConfigDTO.getJenkinsUrl(), globalConfigDTO.getUserName(), globalConfigDTO.getPassword(), listener.getLogger(), validatedData);
		collector.collectResults(validatedData, compareWithPrevious(), getIgnoreRunningJobs(), configChanges, ignoreDisabledJobs);
		collector.closeJenkinsConnection();
		// Analyze Results
		Aggregated aggregated = new Analyzer(logger).analyze(aggregatedSavedData, validatedData, properties, compareWithPrevious(), getIgnoreRunningJobs(), getIgnoreDisabledJobs(), getIgnoreNotFoundJobs(),
				getIgnoreAbortedJobs());
		// Reporter for HTML and mail
		Reporter reporter = new Reporter(logger, workspace, run.getRootDir(), globalConfigDTO.getMailNotificationFrom(), getIgnoreDisabledJobs(), getIgnoreNotFoundJobs(), getIgnoreAbortedJobs(), configChanges);
		reporter.publishResuts(aggregated, properties, localizedColumns, run.getRootDir());
		// Add Build Action
		run.addAction(new TestResultsAggregatorTestResultBuildAction(aggregated));
		logger.println(LocalMessages.FINISHED_AGGREGATE.toString());
	}
	
	/* In use from Free Style Project */
	@Override
	public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
		PrintStream logger = listener.getLogger();
		logger.println(LocalMessages.START_AGGREGATE.toString());
		Descriptor desc = getDescriptor();
		globalConfigDTO = new GlobalConfigDTO(resolveJenkinsUrl(build.getEnvironment(listener), logger), desc.getUsername(), desc.getPassword().getPlainText(), desc.getMailNotificationFrom());
		//
		initProperties();
		// Override Global config
		globalConfigDTO = overrideGlobalConfigiguration(globalConfigDTO);
		// Resolve Variables
		resolveVariables(properties, build.getBuildVariableResolver(), build.getEnvironment(listener));
		// Resolve Columns
		List<LocalMessages> localizedColumns = calculateColumns(getColumns());
		// Validate Input Data
		List<Data> validatedData = validateInputData(getData(), globalConfigDTO.getJenkinsUrl());
		validatedData = checkUserInputForInjection(validatedData);
		Aggregated aggregatedSavedData = null;
		if (compareWithPrevious()) {
			aggregatedSavedData = getPreviousData2(build, validatedData);
		}
		//
		boolean configChanges = checkConfigChanges(logger, aggregatedSavedData);
		// Collect Data
		Collector collector = new Collector(globalConfigDTO.getJenkinsUrl(), globalConfigDTO.getUserName(), globalConfigDTO.getPassword(), listener.getLogger(), validatedData);
		collector.collectResults(validatedData, compareWithPrevious(), getIgnoreRunningJobs(), configChanges, ignoreDisabledJobs);
		collector.closeJenkinsConnection();
		// Analyze Results
		Aggregated aggregated = new Analyzer(logger).analyze(aggregatedSavedData, validatedData, properties, compareWithPrevious(), getIgnoreRunningJobs(), getIgnoreDisabledJobs(), getIgnoreNotFoundJobs(),
				getIgnoreAbortedJobs());
		// Reporter for HTML and mail
		Reporter reporter = new Reporter(logger, build.getProject().getSomeWorkspace(), build.getRootDir(), globalConfigDTO.getMailNotificationFrom(), getIgnoreDisabledJobs(), getIgnoreNotFoundJobs(),
				getIgnoreAbortedJobs(),
				configChanges);
		reporter.publishResuts(aggregated, properties, localizedColumns, build.getRootDir());
		// Add Build Action
		build.addAction(new TestResultsAggregatorTestResultBuildAction(aggregated));
		logger.println(LocalMessages.FINISHED_AGGREGATE.toString());
		return true;
	}
	
	private GlobalConfigDTO overrideGlobalConfigiguration(GlobalConfigDTO globalConfigDTO) {
		if (!Strings.isNullOrEmpty(properties.getProperty(AggregatorProperties.OVERRIDE_JENKINS_BASEURL.name()))) {
			globalConfigDTO.setJenkinsUrl(properties.getProperty(AggregatorProperties.OVERRIDE_JENKINS_BASEURL.name()));
		}
		if (!Strings.isNullOrEmpty(properties.getProperty(AggregatorProperties.OVERRIDE_API_ACCOUNT_USERNAME.name()))) {
			globalConfigDTO.setUserName(properties.getProperty(AggregatorProperties.OVERRIDE_API_ACCOUNT_USERNAME.name()));
		}
		if (!Strings.isNullOrEmpty(properties.getProperty(AggregatorProperties.OVERRIDE_API_ACCOUNT_PASSWORD.name()))) {
			globalConfigDTO.setPassword(properties.getProperty(AggregatorProperties.OVERRIDE_API_ACCOUNT_PASSWORD.name()));
		}
		if (!Strings.isNullOrEmpty(properties.getProperty(AggregatorProperties.OVERRIDE_MAIL_NOTIFICATION_FROM.name()))) {
			globalConfigDTO.setMailNotificationFrom(properties.getProperty(AggregatorProperties.OVERRIDE_MAIL_NOTIFICATION_FROM.name()));
		}
		return globalConfigDTO;
	}
	
	private boolean checkConfigChanges(PrintStream logger, Aggregated aggregatedSavedData) {
		boolean foundConfigChanges = false;
		try {
			if (aggregatedSavedData.getIgnoreDisabledJobs() != null && aggregatedSavedData.getIgnoreDisabledJobs().booleanValue() != getIgnoreDisabledJobs()) {
				foundConfigChanges = true;
			}
			if (aggregatedSavedData.getIgnoreNotFoundJobs() != null && aggregatedSavedData.getIgnoreNotFoundJobs().booleanValue() != getIgnoreNotFoundJobs()) {
				foundConfigChanges = true;
			}
			if (aggregatedSavedData.getIgnoreRunningJobs() != null && aggregatedSavedData.getIgnoreRunningJobs().booleanValue() != getIgnoreRunningJobs()) {
				foundConfigChanges = true;
			}
			if (aggregatedSavedData.getIgnoreAbortedJobs() != null && aggregatedSavedData.getIgnoreAbortedJobs().booleanValue() != getIgnoreAbortedJobs()) {
				foundConfigChanges = true;
			}
			if (aggregatedSavedData.getCompareWithPreviousRun() != null && aggregatedSavedData.getCompareWithPreviousRun().booleanValue() != compareWithPrevious()) {
				foundConfigChanges = true;
			}
		} catch (Exception ex) {
			
		}
		if (foundConfigChanges) {
			logger.println("Found Configuration changes");
		}
		return foundConfigChanges;
	}
	
	private void initProperties() {
		// Set up Properties
		properties = new Properties();
		properties.put(AggregatorProperties.OUT_OF_DATE_RESULTS_ARG.name(), getOutOfDateResults() != null ? getOutOfDateResults() : "");
		// properties.put(AggregatorProperties.TEST_PERCENTAGE_PREFIX.name(), "");
		properties.put(AggregatorProperties.THEME.name(), getTheme() != null ? getTheme() : "light");
		properties.put(AggregatorProperties.TEXT_BEFORE_MAIL_BODY.name(), getBeforebody() != null ? getBeforebody() : "");
		properties.put(AggregatorProperties.TEXT_AFTER_MAIL_BODY.name(), getAfterbody() != null ? getAfterbody() : "");
		properties.put(AggregatorProperties.SORT_JOBS_BY.name(), getSortresults() != null ? getSortresults() : "Job Name");
		properties.put(AggregatorProperties.SUBJECT_PREFIX.name(), getSubject());
		properties.put(AggregatorProperties.RECIPIENTS_LIST.name(), getRecipientsList() != null ? getRecipientsList() : "");
		properties.put(AggregatorProperties.RECIPIENTS_LIST_BCC.name(), getRecipientsListBCC() != null ? getRecipientsListBCC() : "");
		properties.put(AggregatorProperties.RECIPIENTS_LIST_CC.name(), getRecipientsListCC() != null ? getRecipientsListCC() : "");
		properties.put(AggregatorProperties.RECIPIENTS_LIST_IGNORED.name(), getRecipientsListIgnored() != null ? getRecipientsListIgnored() : "");
		properties.put(AggregatorProperties.IGNORE_NOTFOUND_JOBS.name(), getIgnoreNotFoundJobs());
		properties.put(AggregatorProperties.IGNORE_DISABLED_JOBS.name(), getIgnoreDisabledJobs());
		properties.put(AggregatorProperties.IGNORE_ABORTED_JOBS.name(), getIgnoreAbortedJobs());
		properties.put(AggregatorProperties.IGNORE_RUNNING_JOBS.name(), getIgnoreRunningJobs());
		properties.put(AggregatorProperties.COMPARE_WITH_PREVIOUS_RUN.name(), compareWithPrevious());
		properties.put(AggregatorProperties.INFLUXDB_URL.name(), getInfluxdbUrl() != null ? getInfluxdbUrl() : "");
		properties.put(AggregatorProperties.INFLUXDB_TOKEN.name(), getInfluxdbToken() != null ? getInfluxdbToken() : "");
		properties.put(AggregatorProperties.INFLUXDB_BUCKET.name(), getInfluxdbBucket() != null ? getInfluxdbBucket() : "");
		properties.put(AggregatorProperties.INFLUXDB_ORG.name(), getInfluxdbOrg() != null ? getInfluxdbOrg() : "");
		//
		properties.put(AggregatorProperties.OVERRIDE_API_ACCOUNT_PASSWORD.name(), getOverrideAPIAccountPassword() != null ? getOverrideAPIAccountPassword() : "");
		properties.put(AggregatorProperties.OVERRIDE_API_ACCOUNT_USERNAME.name(), getOverrideAPIAccountUsername() != null ? getOverrideAPIAccountUsername() : "");
		properties.put(AggregatorProperties.OVERRIDE_JENKINS_BASEURL.name(), getOverrideJenkinsBaseURL() != null ? getOverrideJenkinsBaseURL() : "");
		properties.put(AggregatorProperties.OVERRIDE_MAIL_NOTIFICATION_FROM.name(), getOverrideMailNotificationFrom() != null ? getOverrideMailNotificationFrom() : "");
	}
	
	@Extension
	@Symbol("testResultsAggregator")
	public static class Descriptor extends BuildStepDescriptor<Publisher> {
		/**
		 * Global configuration information variables.
		 */
		private String jenkinsUrl;
		private String username;
		private Secret password;
		private String mailNotificationFrom;
		
		public String getUsername() {
			return username;
		}
		
		@DataBoundSetter
		public void setUsername(String username) {
			this.username = username;
		}
		
		public Secret getPassword() {
			return password;
		}
		
		@DataBoundSetter
		public void setPassword(String password) {
			this.password = Secret.fromString(password);
		}
		
		public String getJenkinsUrl() {
			return jenkinsUrl;
		}
		
		@DataBoundSetter
		public void setJenkinsUrl(String jenkinsUrl) {
			this.jenkinsUrl = jenkinsUrl;
		}
		
		public String getMailNotificationFrom() {
			return mailNotificationFrom;
		}
		
		@DataBoundSetter
		public void setMailNotificationFrom(String mailNotificationFrom) {
			this.mailNotificationFrom = mailNotificationFrom;
		}
		
		public String defaultMailNotificationFrom() {
			return "Jenkins";
		}
		
		/**
		 * In order to load the persisted global configuration, you have to call load() in the constructor.
		 */
		public Descriptor() {
			load();
		}
		
		@Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			// Indicates that this builder can be used with all kinds of project types.
			// return jobType == FreeStyleProject.class;
			return true;
		}
		
		@Override
		public String getDisplayName() {
			return displayName;
		}
		
		@Override
		public boolean configure(StaplerRequest req, JSONObject jsonObject) throws FormException {
			username = jsonObject.getString("username");
			password = Secret.fromString((String) jsonObject.get("password"));
			jenkinsUrl = jsonObject.getString("jenkinsUrl");
			mailNotificationFrom = jsonObject.getString("mailNotificationFrom");
			save();
			return super.configure(req, jsonObject);
		}
		
		public FormValidation doCheckOutOfDateResults(@QueryParameter final String outOfDateResults) {
			if (!Strings.isNullOrEmpty(outOfDateResults)) {
				try {
					int hours = Integer.parseInt(outOfDateResults);
					if (hours < 0) {
						return FormValidation.error(LocalMessages.VALIDATION_POSITIVE_NUMBER.toString());
					} else {
						return FormValidation.ok();
					}
				} catch (NumberFormatException e) {
					return FormValidation.error(LocalMessages.VALIDATION_INTEGER_NUMBER.toString());
				}
			} else {
				// No OutOfDate
				return FormValidation.ok();
			}
		}
		
		@RequirePOST
		public FormValidation doTestApiConnection(@QueryParameter final String jenkinsUrl, @QueryParameter final String username, @QueryParameter final Secret password) {
			// https://www.jenkins.io/doc/developer/security/form-validation/
			Jenkins.get().checkPermission(Jenkins.ADMINISTER);
			try {
				String plainTextPassword = null;
				if (password != null && !Strings.isNullOrEmpty(password.getPlainText())) {
					plainTextPassword = password.getPlainText();
				}
				JenkinsServer jenkins = new JenkinsServer(new URI(jenkinsUrl), username, plainTextPassword);
				Map<String, com.offbytwo.jenkins.model.Job> jobsfound = jenkins.getJobs();
				String message = LocalMessages.SUCCESS.toString() + " (items found " + jobsfound.size() + ")";
				jenkins.close();
				return FormValidation.ok(message);
			} catch (java.net.UnknownHostException ex) {
				return FormValidation.error(LocalMessages.UNKNOWN_HOST_NAME.toString() + ": " + ex.getMessage());
			} catch (Exception ex) {
				ex.printStackTrace();
				return FormValidation.error(LocalMessages.ERROR_OCCURRED.toString() + ": " + ex.getMessage());
			}
		}
		
	}
	
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	@DataBoundSetter
	public void setData(@CheckForNull List<Data> data) {
		this.data = data;
	}
	
	@DataBoundSetter
	public void setRecipientsList(@CheckForNull String recipientsList) {
		this.recipientsList = recipientsList;
	}
	
	@DataBoundSetter
	public void setRecipientsListCC(@CheckForNull String recipientsListCC) {
		this.recipientsListCC = recipientsListCC;
	}
	
	@DataBoundSetter
	public void setRecipientsListBCC(@CheckForNull String recipientsListBCC) {
		this.recipientsListBCC = recipientsListBCC;
	}
	
	@DataBoundSetter
	public void setRecipientsListIgnored(@CheckForNull String recipientsListIgnored) {
		this.recipientsListIgnored = recipientsListIgnored;
	}
	
	@DataBoundSetter
	public void setOutOfDateResults(@CheckForNull String outOfDateResults) {
		this.outOfDateResults = outOfDateResults;
	}
	
	@DataBoundSetter
	public void setBeforebody(@CheckForNull String beforebody) {
		this.beforebody = beforebody;
	}
	
	@DataBoundSetter
	public void setAfterbody(@CheckForNull String afterbody) {
		this.afterbody = afterbody;
	}
	
	@DataBoundSetter
	public void setTheme(@CheckForNull String theme) {
		this.theme = theme;
	}
	
	@DataBoundSetter
	public void setSortresults(@CheckForNull String sortresults) {
		this.sortresults = sortresults;
	}
	
	@DataBoundSetter
	public void setSubject(@CheckForNull String subject) {
		this.subject = subject;
	}
	
	@DataBoundSetter
	public void setColumns(@CheckForNull String columns) {
		this.columns = columns;
	}
	
	@DataBoundSetter
	public void setCompareWithPreviousRun(Boolean compareWithPreviousRun) {
		this.compareWithPreviousRun = compareWithPreviousRun;
	}
	
	@DataBoundSetter
	public void setIgnoreNotFoundJobs(Boolean ignoreNotFoundJobs) {
		this.ignoreNotFoundJobs = ignoreNotFoundJobs;
	}
	
	@DataBoundSetter
	public void setIgnoreDisabledJobs(Boolean ignoreDisabledJobs) {
		this.ignoreDisabledJobs = ignoreDisabledJobs;
	}
	
	@DataBoundSetter
	public void setIgnoreAbortedJobs(Boolean ignoreAbortedJobs) {
		this.ignoreAbortedJobs = ignoreAbortedJobs;
	}
	
	@DataBoundSetter
	public void setIgnoreRunningJobs(Boolean ignoreRunningJobs) {
		this.ignoreRunningJobs = ignoreRunningJobs;
	}
	
	public String getRecipientsList() {
		return recipientsList;
	}
	
	public String getRecipientsListCC() {
		return recipientsListCC;
	}
	
	public String getRecipientsListBCC() {
		return recipientsListBCC;
	}
	
	public String getRecipientsListIgnored() {
		return recipientsListIgnored;
	}
	
	public String getOutOfDateResults() {
		return outOfDateResults;
	}
	
	public List<Data> getData() {
		return data;
	}
	
	public String getColumns() {
		// Health, Job, Build, Status, Percentage, Total, Pass, Fail, Skip, Commits, LastRun, Duration, Packages, Files, Classes, Methods, Lines, Conditions, Sonar
		return columns != null ? columns : "Job, Build, Status";
	}
	
	public String getSubject() {
		if (subject == null) {
			subject = "";
		}
		return subject;
	}
	
	public String getSortresults() {
		return sortresults;
	}
	
	public String getTheme() {
		return theme;
	}
	
	public String getBeforebody() {
		return beforebody;
	}
	
	public String getAfterbody() {
		return afterbody;
	}
	
	public Boolean isCompareWithPreviousRun() {
		return compareWithPreviousRun;
	}
	
	public boolean compareWithPrevious() {
		if (compareWithPreviousRun == null) {
			compareWithPreviousRun = true;
		}
		return compareWithPreviousRun.booleanValue();
	}
	
	public Boolean isIgnoreNotFoundJobs() {
		return ignoreNotFoundJobs;
	}
	
	public Boolean isIgnoreDisabledJobs() {
		return ignoreDisabledJobs;
	}
	
	public boolean getIgnoreNotFoundJobs() {
		if (ignoreNotFoundJobs == null) {
			ignoreNotFoundJobs = false;
		}
		return ignoreNotFoundJobs.booleanValue();
	}
	
	public boolean getIgnoreDisabledJobs() {
		if (ignoreDisabledJobs == null) {
			ignoreDisabledJobs = false;
		}
		return ignoreDisabledJobs.booleanValue();
	}
	
	public boolean getIgnoreAbortedJobs() {
		if (ignoreAbortedJobs == null) {
			ignoreAbortedJobs = false;
		}
		return ignoreAbortedJobs.booleanValue();
	}
	
	public boolean getIgnoreRunningJobs() {
		if (ignoreRunningJobs == null) {
			ignoreRunningJobs = false;
		}
		return ignoreRunningJobs.booleanValue();
	}
	
	public List<DataPipeline> getJobs() {
		return jobs;
	}
	
	public void setJobs(List<DataPipeline> pipelineJobs) {
		this.jobs = pipelineJobs;
	}
	
	public List<Data> getDataFromDataPipeline() throws IOException {
		List<Data> data = new ArrayList<>();
		if (jobs != null && !jobs.isEmpty()) {
			List<String> groups = jobs.stream().map(DataPipeline::getGroupName).distinct().collect(Collectors.toList());
			if (!groups.isEmpty()) {
				//
				for (String group : groups) {
					List<DataPipeline> listOfJobs = jobs.stream().filter(x -> x.getGroupName().equalsIgnoreCase(group)).collect(Collectors.toList());
					List<Job> listOfJobsIntoGroup = new ArrayList<>();
					for (DataPipeline temp : listOfJobs) {
						listOfJobsIntoGroup.add(new Job(temp.getJobName(), temp.getJobFriendlyName()));
					}
					data.add(new Data(group, listOfJobsIntoGroup));
				}
			} else {
				// No Groups
				List<DataPipeline> dataPipelineItems = jobs.stream().filter(x -> x.getJobName() != null).distinct().collect(Collectors.toList());
				List<Job> jobs = new ArrayList<>();
				for (DataPipeline dataPipeline : dataPipelineItems) {
					jobs.add(new Job(dataPipeline.getJobName(), dataPipeline.getJobFriendlyName()));
				}
				data.add(new Data(null, jobs));
			}
			return data;
		}
		throw new IOException("No data");
	}
	
	public String getInfluxdbUrl() {
		return influxdbUrl;
	}
	
	@DataBoundSetter
	public void setInfluxdbUrl(String influxdbUrl) {
		this.influxdbUrl = influxdbUrl;
	}
	
	public String getInfluxdbToken() {
		return influxdbToken;
	}
	
	@DataBoundSetter
	public void setInfluxdbToken(String influxdbToken) {
		this.influxdbToken = influxdbToken;
	}
	
	public String getInfluxdbBucket() {
		return influxdbBucket;
	}
	
	@DataBoundSetter
	public void setInfluxdbBucket(String influxdbBucket) {
		this.influxdbBucket = influxdbBucket;
	}
	
	public String getInfluxdbOrg() {
		return influxdbOrg;
	}
	
	@DataBoundSetter
	public void setInfluxdbOrg(String influxdbOrg) {
		this.influxdbOrg = influxdbOrg;
	}
	
	public String getOverrideJenkinsBaseURL() {
		return overrideJenkinsBaseURL;
	}
	
	@DataBoundSetter
	public void setOverrideJenkinsBaseURL(String overrideJenkinsBaseURL) {
		this.overrideJenkinsBaseURL = overrideJenkinsBaseURL;
	}
	
	public String getOverrideAPIAccountUsername() {
		return overrideAPIAccountUsername;
	}
	
	@DataBoundSetter
	public void setOverrideAPIAccountUsername(String overrideAPIAccountUsername) {
		this.overrideAPIAccountUsername = overrideAPIAccountUsername;
	}
	
	public String getOverrideAPIAccountPassword() {
		return overrideAPIAccountPassword;
	}
	
	@DataBoundSetter
	public void setOverrideAPIAccountPassword(String overrideAPIAccountPassword) {
		this.overrideAPIAccountPassword = overrideAPIAccountPassword;
	}
	
	public String getOverrideMailNotificationFrom() {
		return overrideMailNotificationFrom;
	}
	
	@DataBoundSetter
	public void setOverrideMailNotificationFrom(String overrideMailNotificationFrom) {
		this.overrideMailNotificationFrom = overrideMailNotificationFrom;
	}
	
}
