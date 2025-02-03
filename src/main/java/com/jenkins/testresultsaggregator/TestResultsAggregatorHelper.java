package com.jenkins.testresultsaggregator;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.TestResultsAggregator.Descriptor;
import com.jenkins.testresultsaggregator.actions.Collector;
import com.jenkins.testresultsaggregator.actions.TestResultHistoryUtil;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.helper.Helper;
import com.jenkins.testresultsaggregator.helper.LocalMessages;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.tasks.Notifier;
import hudson.util.VariableResolver;
import jenkins.tasks.SimpleBuildStep;

public class TestResultsAggregatorHelper extends Notifier implements SimpleBuildStep {
	
	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}
	
	public String resolveJenkinsUrl(EnvVars envVars, PrintStream logger) {
		Descriptor desc = getDescriptor();
		String jenkinsUrl = desc.getJenkinsUrl();
		if (Strings.isNullOrEmpty(jenkinsUrl)) {
			// Resolve default url
			jenkinsUrl = envVars.get("JENKINS_URL");
			logger.println("Fallback Jenkins url : " + jenkinsUrl);
		}
		return jenkinsUrl;
	}
	
	public List<Data> checkUserInputForInjection(List<Data> validatedData) {
		for (Data tempData : validatedData) {
			if (!Strings.isNullOrEmpty(tempData.getGroupName())) {
				if (tempData.getGroupName().contains("<") || tempData.getGroupName().contains(">")) {
					tempData.setGroupName(tempData.getGroupName().replaceAll(">", "").replace("<", ""));
				}
			}
			for (Job tempJob : tempData.getJobs()) {
				if (!Strings.isNullOrEmpty(tempJob.getJobFriendlyName())) {
					if (tempJob.getJobFriendlyName().contains("<") || tempJob.getJobFriendlyName().contains(">")) {
						tempJob.setJobFriendlyName(tempJob.getJobFriendlyName().replaceAll("<", "").replaceAll(">", ""));
					}
				}
			}
		}
		return validatedData;
	}
	
	public void resolveVariables(Properties properties, VariableResolver<?> buildVars, EnvVars envVars) throws IOException, InterruptedException {
		// Variables
		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		Iterator<Entry<Object, Object>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Object, Object> entry = iterator.next();
			String originalValue = entry.getValue().toString();
			if (!Strings.isNullOrEmpty(originalValue)) {
				while (originalValue.contains("${")) {
					String tempValue = null;
					if (originalValue.contains("${")) {
						tempValue = originalValue.substring(originalValue.indexOf("${") + 2, originalValue.indexOf('}'));
					}
					Object buildVariable = null;
					// Resolve from building variables
					if (buildVars != null) {
						buildVariable = buildVars.resolve(tempValue);
					}
					// If null try resolve it from env variables
					if (buildVariable == null) {
						buildVariable = envVars.get(tempValue);
					}
					if (buildVariable != null) {
						originalValue = originalValue.replaceAll("\\$\\{" + tempValue + "}", buildVariable.toString());
					} else {
						originalValue = originalValue.replaceAll("\\$\\{" + tempValue + "}", "\\$[" + tempValue + "]");
					}
				}
				entry.setValue(originalValue);
			}
		}
	}
	
	public void previousSavedResults(List<Data> validatedData, Aggregated previousAggregated) {
		if (previousAggregated != null && previousAggregated.getData() != null) {
			for (Data data : validatedData) {
				for (Job job : data.getJobs()) {
					for (Data pdata : previousAggregated.getData()) {
						for (Job pjob : pdata.getJobs()) {
							if (job.getJobName().equals(pjob.getJobName())) {
								// The last saved in aggregator its the report now
								job.setResults(pjob.getResults());
								break;
							}
						}
					}
				}
			}
		}
	}
	
	public List<LocalMessages> calculateColumns(String userSelectionColumns) {
		List<LocalMessages> columns = new ArrayList<>(Arrays.asList(LocalMessages.COLUMN_GROUP));
		if (!Strings.isNullOrEmpty(userSelectionColumns)) {
			String[] splitter = userSelectionColumns.split(",");
			for (String temp : splitter) {
				if (temp != null) {
					temp = temp.trim();
					if (temp.equalsIgnoreCase("Status")) {
						columns.add(LocalMessages.COLUMN_JOB_STATUS);
					} else if (temp.equalsIgnoreCase("Job")) {
						columns.add(LocalMessages.COLUMN_JOB);
					} else if (temp.equalsIgnoreCase("Percentage")) {
						columns.add(LocalMessages.COLUMN_PERCENTAGE);
					} else if (temp.equalsIgnoreCase("Total")) {
						columns.add(LocalMessages.COLUMN_TESTS);
					} else if (temp.equalsIgnoreCase("Pass")) {
						columns.add(LocalMessages.COLUMN_PASS);
					} else if (temp.equalsIgnoreCase("Fail")) {
						columns.add(LocalMessages.COLUMN_FAIL);
					} else if (temp.equalsIgnoreCase("Skip")) {
						columns.add(LocalMessages.COLUMN_SKIP);
					} else if (temp.equalsIgnoreCase("Commits")) {
						columns.add(LocalMessages.COLUMN_COMMITS);
					} else if (temp.equalsIgnoreCase("LastRun")) {
						columns.add(LocalMessages.COLUMN_LAST_RUN);
					} else if (temp.equalsIgnoreCase("Duration")) {
						columns.add(LocalMessages.COLUMN_DURATION);
					} else if (temp.equalsIgnoreCase("Description")) {
						columns.add(LocalMessages.COLUMN_DESCRIPTION);
					} else if (temp.equalsIgnoreCase("Health")) {
						columns.add(LocalMessages.COLUMN_HEALTH);
					} else if (temp.equalsIgnoreCase("Packages")) {
						columns.add(LocalMessages.COLUMN_CC_PACKAGES);
					} else if (temp.equalsIgnoreCase("Files")) {
						columns.add(LocalMessages.COLUMN_CC_FILES);
					} else if (temp.equalsIgnoreCase("Classes")) {
						columns.add(LocalMessages.COLUMN_CC_CLASSES);
					} else if (temp.equalsIgnoreCase("Methods")) {
						columns.add(LocalMessages.COLUMN_CC_METHODS);
					} else if (temp.equalsIgnoreCase("Lines")) {
						columns.add(LocalMessages.COLUMN_CC_LINES);
					} else if (temp.equalsIgnoreCase("Conditions")) {
						columns.add(LocalMessages.COLUMN_CC_CONDITIONS);
					} else if (temp.equalsIgnoreCase("Sonar")) {
						columns.add(LocalMessages.COLUMN_SONAR_URL);
					} else if (temp.equalsIgnoreCase("Build")) {
						columns.add(LocalMessages.COLUMN_BUILD_NUMBER);
					}
				}
			}
		}
		return columns;
	}
	
	public List<Data> validateInputData(List<Data> data, String jenkinsUrl) throws UnsupportedEncodingException, MalformedURLException {
		List<Data> validateData = new ArrayList<>();
		for (Data tempDataDTO : data) {
			if (tempDataDTO.getJobs() != null && !tempDataDTO.getJobs().isEmpty()) {
				boolean allJobsareEmpty = true;
				List<Job> validateDataJobs = new ArrayList<>();
				for (Job temp : tempDataDTO.getJobs()) {
					if (!Strings.isNullOrEmpty(temp.getJobName())) {
						allJobsareEmpty = false;
						validateDataJobs.add(temp);
					}
				}
				if (!allJobsareEmpty) {
					tempDataDTO.setJobs(validateDataJobs);
					validateData.add(tempDataDTO);
				}
			}
		}
		return evaluateInputData(validateData, jenkinsUrl);
	}
	
	public List<Data> evaluateInputData(List<Data> data, String jenkinsUrl) throws UnsupportedEncodingException, MalformedURLException {
		String JOB = "job";
		for (Data jobs : data) {
			for (Job job : jobs.getJobs()) {
				if (job.getJobName().contains("/")) {
					String[] spliter = job.getJobName().split("/");
					if (spliter[spliter.length - 1].equals("*")) {
						// Do nothing for now
					} else {
						StringBuilder folders = new StringBuilder();
						for (int i = 0; i < spliter.length - 1; i++) {
							if (!Strings.isNullOrEmpty(spliter[i])) {
								folders.append(spliter[i] + "/");
							}
						}
						job.setJobNameOnly(spliter[spliter.length - 1]);
						job.setFolder(folders.toString().replaceAll("/", "/" + JOB + "/"));
						job.setUrl(jenkinsUrl + "/" + JOB + "/" + Helper.encodeValue(job.getFolder()).replace("%2F", "/") + Helper.encodeValue(spliter[spliter.length - 1]));
					}
				} else {
					job.setFolder(Collector.ROOT_FOLDER);
					job.setJobNameOnly(job.getJobName());
					if (Strings.isNullOrEmpty(job.getUrl())) {
						job.setUrl(jenkinsUrl + "/" + JOB + "/" + Helper.encodeValue(job.getJobName()));
					}
				}
			}
		}
		return data;
	}
	
	public Aggregated getPreviousData(Run build, List<Data> validatedData) {
		if (build != null) {// Get Previous Saved Results
			Aggregated previousSavedAggregatedResults = TestResultHistoryUtil.getTestResults(build);
			if (previousSavedAggregatedResults != null) {
				// Check previous Data
				previousSavedResults(validatedData, previousSavedAggregatedResults);
			}
			return previousSavedAggregatedResults;
		}
		return null;
	}
	
	public Aggregated getPreviousData2(Run build, List<Data> validatedData) {
		if (build != null) {// Get Previous Saved Results
			Aggregated previousSavedAggregatedResults = TestResultsAggregatorTestResultBuildAction.loadResults(build);
			if (previousSavedAggregatedResults != null) {
				// Check previous Data
				previousSavedResults(validatedData, previousSavedAggregatedResults);
			}
			return previousSavedAggregatedResults;
		}
		return null;
	}
	
}
