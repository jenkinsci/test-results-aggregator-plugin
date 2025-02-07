package com.jenkins.testresultsaggregator.actions;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.jenkins.testresultsaggregator.data.BuildWithDetailsAggregator;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.data.JobResults;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.jenkins.testresultsaggregator.data.JobWithDetailsAggregator;
import com.jenkins.testresultsaggregator.data.Results;
import com.jenkins.testresultsaggregator.helper.LocalMessages;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpConnection;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.FolderJob;

public class Collector {
	
	private int mode = 2;
	public static final String ROOT_FOLDER = "root";
	public static final int parrallelThreads = 6;
	public static final int delayThreads = 6000;
	public static final int maxThreadTime = 180000;
	// Urls
	public static final String DEPTH = "?depth=1";
	public static final String API = "/api/json";
	
	private PrintStream logger;
	JenkinsServer jenkins;
	Map<String, com.offbytwo.jenkins.model.Job> jobs;
	JenkinsHttpConnection client;
	String passwordPlainText;
	
	public Collector(String jenkinsUrl, String username, String password, PrintStream printStream, List<Data> data) throws IOException {
		this.logger = printStream;
		if (password != null) {
			this.passwordPlainText = password;
		}
		try {
			this.jenkins = new JenkinsServer(new URI(jenkinsUrl), username, passwordPlainText);
			this.jobs = jenkins.getJobs();
		} catch (Exception ex) {
			logger.println("ERROR " + Throwables.getStackTraceAsString(ex));
			throw new IOException(ex);
		}
		this.client = jenkins.getQueue().getClient();
		StringBuilder text = new StringBuilder();
		text.append("Total Jenkins jobs found " + jobs.size());
		List<Job> list = new ArrayList<>();
		for (Data temp : data) {
			list.addAll(temp.getJobs());
		}
		int successResolvedModel = 0;
		for (Job temp : list) {
			try {
				resolveModel(temp, null);
				if (temp.getModelJob() != null) {
					successResolvedModel++;
				}
			} catch (Exception ex) {
				logger.println("Error resolve model for job " + temp.getJobName() + " " + Throwables.getStackTraceAsString(ex));
			}
		}
		text.append(" and Aggregator has resolved successfully " + successResolvedModel + "/" + list.size());
		logger.println(text.toString());
	}
	
	public void closeJenkinsConnection() {
		if (jenkins != null) {
			jenkins.close();
		}
	}
	
	private Job resolveModel(Job job, FolderJob folderJob) throws IOException {
		if (job.getFolder().equalsIgnoreCase(ROOT_FOLDER)) {
			job.setModelJob(jobs.get(job.getJobNameOnly()));
		} else {
			if (folderJob == null) {
				for (String tempFolder : job.getFolders()) {
					com.offbytwo.jenkins.model.Job found = jobs.get(tempFolder);
					if (found != null) {
						resolveModel(job, client.get(found.getUrl(), FolderJob.class));
						break;
					}
				}
			} else {
				Map<String, com.offbytwo.jenkins.model.Job> jobsIntoFolder = jenkins.getJobs(folderJob);
				if (jobsIntoFolder != null && jobsIntoFolder.containsKey(job.getJobNameOnly())) {
					job.setModelJob(jobsIntoFolder.get(job.getJobNameOnly()));
				} else {
					for (String tempFolder : job.getFolders()) {
						if (jobsIntoFolder.get(tempFolder) != null) {
							FolderJob found = client.get(jobsIntoFolder.get(tempFolder).getUrl(), FolderJob.class);
							if (found != null) {
								resolveModel(job, found);
							}
						}
					}
				}
			}
		}
		return job;
	}
	
	public JobWithDetailsAggregator getDetails(Job job) throws Exception {
		return getDetailsMode(job, mode);
	}
	
	public BuildWithDetailsAggregator getLastBuildDetails(Job job) throws Exception {
		return getLastBuildDetailsMode(job, mode);
	}
	
	public BuildWithDetailsAggregator getBuildDetails(Job job, Integer number) throws Exception {
		return getBuildDetailsMode(job, number, mode);
	}
	
	private JobWithDetailsAggregator getDetailsMode(Job job, int mode) throws Exception {
		JobWithDetailsAggregator response = null;
		int retries = 0;
		StringBuilder errorFound = new StringBuilder();
		while (retries < 4 && response == null) {
			try {
				if (mode == 1) {
					response = job.getModelJob().getClient().get(job.getUrl() + DEPTH, JobWithDetailsAggregator.class);
				} else {
					response = client.get(job.getUrl(), JobWithDetailsAggregator.class);
					// response = getData(job.getUrl() + API + DEPTH, username, passwordPlainText, JobWithDetailsAggregator.class);
				}
				errorFound = new StringBuilder();
			} catch (Exception ex) {
				if (ex.getMessage() != null && ex.getMessage().endsWith("is null") && !ex.getMessage().contains("Unexpected character")) {
					throw ex;
				} else {
					errorFound = new StringBuilder();
					errorFound.append("Error get details for job '" + job.getJobName() + "' " + " mode " + mode + " : " + Throwables.getStackTraceAsString(ex));
				}
			}
			retries++;
			mode = (retries % 2) + 1;
		}
		if (!Strings.isNullOrEmpty(errorFound.toString())) {
			logger.println("ERROR " + errorFound.toString());
		}
		return response;
	}
	
	private BuildWithDetailsAggregator getLastBuildDetailsMode(Job job, int mode) throws Exception {
		BuildWithDetailsAggregator response = null;
		int retries = 0;
		StringBuilder errorFound = new StringBuilder();
		String url = job.getJob().getLastBuild().getUrl() + DEPTH;
		while (retries < 4 && response == null) {
			try {
				if (mode == 1) {
					response = job.getModelJob().getClient().get(url, BuildWithDetailsAggregator.class);
				} else {
					// String url = job.getJob().getLastBuild().getUrl() + API + DEPTH;
					// response = getData(url, username, passwordPlainText, BuildWithDetailsAggregator.class);
					response = client.get(url, BuildWithDetailsAggregator.class);
				}
				errorFound = new StringBuilder();
			} catch (Exception ex) {
				if (ex.getMessage() != null && ex.getMessage().endsWith("is null")) {
					throw ex;
				} else {
					errorFound = new StringBuilder();
					errorFound.append("No last build details for job '" + job.getJobName() + "' " + " mode " + mode + " " + Throwables.getStackTraceAsString(ex));
				}
			}
			retries++;
			mode = (retries % 2) + 1;
		}
		if (!Strings.isNullOrEmpty(errorFound.toString())) {
			logger.println("ERROR " + errorFound.toString());
		}
		return response;
	}
	
	public BuildWithDetailsAggregator getBuildDetailsMode(Job job, Integer number, int mode) throws Exception {
		BuildWithDetailsAggregator response = null;
		StringBuilder errorFound = new StringBuilder();
		if (number != null && number > 0) {
			int retries = 0;
			Build build = null;
			while (retries < 4 && response == null) {
				try {
					if (build == null) {
						build = job.getModelJob().details().getBuildByNumber(number);
					}
					if (build != null) {
						if (mode == 1) {
							response = job.getModelJob().getClient().get(build.details().getUrl() + DEPTH, BuildWithDetailsAggregator.class);
						} else {
							response = client.get(build.details().getUrl() + DEPTH, BuildWithDetailsAggregator.class);
							// response = getData(build.details().getUrl() + API + DEPTH, username, passwordPlainText, BuildWithDetailsAggregator.class);
						}
					}
					errorFound = new StringBuilder();
				} catch (Exception ex) {
					if (ex.getMessage() != null && ex.getMessage().endsWith("is null")) {
						throw ex;
					} else {
						errorFound = new StringBuilder();
						errorFound.append("No build details for job '" + job.getJobName() + "' with number " + number + " mode " + mode + " " + Throwables.getStackTraceAsString(ex));
					}
				}
				retries++;
				mode = (retries % 2) + 1;
			}
		}
		if (!Strings.isNullOrEmpty(errorFound.toString())) {
			logger.println("ERROR " + errorFound.toString());
		}
		return response;
	}
	
	public void collectResults(List<Data> dataJob, boolean compareWithPreviousRun, Boolean ignoreRunningJobs, boolean configChanges, boolean ignoreDisabledJobs) throws InterruptedException {
		logger.println("Collect data");
		List<Job> allDataJobDTO = new ArrayList<>();
		for (Data temp : dataJob) {
			if (temp.getJobs() != null && !temp.getJobs().isEmpty()) {
				allDataJobDTO.addAll(temp.getJobs());
			}
		}
		ReportThread[] threads = new ReportThread[allDataJobDTO.size()];
		int index = 0;
		for (Job tempDataJobDTO : allDataJobDTO) {
			threads[index] = new ReportThread(tempDataJobDTO, compareWithPreviousRun, ignoreRunningJobs, configChanges, ignoreDisabledJobs);
			index++;
		}
		index = 0;
		for (ReportThread thread : threads) {
			thread.start();
			index++;
			if (index % parrallelThreads == 0) {
				Thread.sleep(delayThreads);
			}
		}
		for (ReportThread thread : threads) {
			thread.join(maxThreadTime);
		}
		logger.println("Collect data ...Finished");
	}
	
	public class ReportThread extends Thread {
		
		Job job;
		boolean compareWithPreviousRun;
		boolean ignoreRunningJobs;
		boolean configChanges;
		boolean ignoreDisabled;
		
		public ReportThread(Job job, boolean compareWithPreviousRun, boolean ignoreRunningJobs, boolean configChanges, boolean ignoreDisabled) {
			this.job = job;
			this.compareWithPreviousRun = compareWithPreviousRun;
			this.ignoreRunningJobs = ignoreRunningJobs;
			this.configChanges = configChanges;
			this.ignoreDisabled = ignoreDisabled;
		}
		
		@Override
		public void run() {
			StringBuilder text = new StringBuilder();
			if (job.getModelJob() != null) {
				try {
					job.setJob(getDetails(job));
					if (job.getJob() == null) {
						text.append("Job '" + job.getJobName() + "' found " + JobStatus.NOT_FOUND.name());
						job.setResults(new Results(JobStatus.NOT_FOUND, job.getUrl()));
					} else if (!job.getJob().isBuildable()) {
						text.append("Job '" + job.getJobName() + "' found " + JobStatus.DISABLED.name());
						if (!ignoreDisabled) {
							job.setLast(new BuildWithDetailsAggregator());
							job.setLast(getLastBuildDetails(job));
							job.getLast().setResults(calculateResults(job.getLast()));
							job.getLast().getResults().setStatus(JobStatus.DISABLED);
						} else {
							job.setLast(new BuildWithDetailsAggregator());
							job.getLast().setResults(new JobResults(JobStatus.DISABLED, job.getJob().getLastBuild().getNumber(), job.getUrl()));
						}
					} else if (job.getJob().isBuildable() && !job.getJob().hasLastBuildRun()) {
						text.append("Job '" + job.getJobName() + "' found " + JobStatus.NO_LAST_BUILD_DATA.name());
						job.setLast(new BuildWithDetailsAggregator());
						job.getLast().setResults(new JobResults(JobStatus.NO_LAST_BUILD_DATA, 0, job.getUrl()));
					} else {
						job.setLast(new BuildWithDetailsAggregator());
						job.setLast(getLastBuildDetails(job));
						job.getLast().setResults(calculateResults(job.getLast()));
						job.setIsBuilding(job.getLast().isBuilding());
						text.append("Job '" + job.getJobName() + "' found build number #" + job.getLast().getBuildNumber());
						collectData(text, job, ignoreRunningJobs, compareWithPreviousRun, configChanges);
						text.append(LocalMessages.FINISHED.toString());
					}
				} catch (Exception e) {
					logger.println("Job '" + job.getJobName() + "' error : " + Throwables.getStackTraceAsString(e));
					job.setResults(new Results(JobStatus.NOT_FOUND, job.getUrl()));
				}
			} else {
				text.append("Job '" + job.getJobName() + "' " + JobStatus.NOT_FOUND.name() + " url " + job.getUrl());
				job.setResults(new Results(JobStatus.NOT_FOUND, job.getUrl()));
			}
			logger.println(text.toString());
		}
	}
	
	private void collectData(StringBuilder text, Job job, boolean ignoreRunningJobs, boolean compareWithPreviousRun, boolean configChanges) throws Exception {
		JobStatus status = job.getLast().getResults().getStatus();
		text.append(" with status " + status.name());
		switch (status) {
			case SUCCESS:
				if (compareWithPreviousRun) {
					getPrevious(text, job, configChanges);
				}
				break;
			case FAILURE:
				if (compareWithPreviousRun) {
					getPrevious(text, job, configChanges);
				}
				break;
			case UNSTABLE:
				if (compareWithPreviousRun) {
					getPrevious(text, job, configChanges);
				}
				break;
			case RUNNING:
				handleRunning(text, job);
				break;
			default:
				break;
		}
	}
	
	private void getPrevious(StringBuilder text, Job job, boolean configChanges) throws Exception {
		if (job.getResults() != null && !JobStatus.RUNNING.equals(job.getResults().getStatus())) {
			int previousBuildNumber = job.getResults().getNumber();
			if (previousBuildNumber > 0 && !configChanges) {
				text.append(", previous saved build #" + previousBuildNumber);
				text.append(" with status " + job.getResults().getStatusAdvanced());
				
			} else {
				getNewPrevious(text, job);
			}
		} else {
			getNewPrevious(text, job);
		}
	}
	
	private void getNewPrevious(StringBuilder text, Job job) throws Exception {
		int previousBuildNumber = resolvePreviousBuildNumberFromBuild(job, 2);
		if (previousBuildNumber > 0) {
			text.append(", previous new build #" + previousBuildNumber);
			BuildWithDetailsAggregator previous = getBuildDetails(job, previousBuildNumber);
			if (previous != null) {
				job.setPrevious(previous);
				job.getPrevious().setResults(calculateResults(job.getPrevious()));
				text.append(" with status " + job.getPrevious().getResults().getStatus());
			}
		}
	}
	
	private void handleRunning(StringBuilder text, Job job) throws Exception {
		job.setPrevious(null);
		if (job.getResults() == null) { // Not found previously saved results
			text.append(", previous build #" + null);
		} else {
			text.append(", previous build #" + job.getResults().getNumber() + " with status " + job.getResults().getStatus());
		}
	}
	
	////
	private JobResults calculateResults(BuildWithDetails buildWithDetails) {
		JobResults jobResults = new JobResults();
		if (buildWithDetails != null) {
			return new CollectorHelper(jobResults, buildWithDetails).calculate();
		}
		return jobResults;
	}
	
	private int resolvePreviousBuildNumberFromBuild(Job job, int depth) {
		try {
			List<Integer> allBuildNumbers = job.getJob().getAllBuilds().stream().map(Build::getNumber).collect(Collectors.toList());
			int retries = 1;
			while ((allBuildNumbers == null || allBuildNumbers.isEmpty()) && retries < 4) {
				allBuildNumbers = job.getJob().getAllBuilds().stream().map(Build::getNumber).collect(Collectors.toList());
				retries++;
			}
			if (allBuildNumbers != null && !allBuildNumbers.isEmpty()) {
				Collections.sort(allBuildNumbers);
				Integer found = allBuildNumbers.get(allBuildNumbers.size() - depth);
				if (found == null) {
					return 0;
				}
				return found.intValue();
			}
		} catch (Exception ex) {
		}
		return 0;
	}
	
}
