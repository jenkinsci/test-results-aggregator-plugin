package com.jenkins.testresultsaggregator.actions;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.TestResultsAggregator;
import com.jenkins.testresultsaggregator.TestResultsAggregator.AggregatorProperties;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.jenkins.testresultsaggregator.data.ReportGroup;
import com.jenkins.testresultsaggregator.data.Results;
import com.jenkins.testresultsaggregator.helper.Helper;
import com.jenkins.testresultsaggregator.helper.LocalMessages;

public class Analyzer {
	
	private PrintStream logger;
	
	public Analyzer(PrintStream logger) {
		this.logger = logger;
	}
	
	public Aggregated analyze(Aggregated aggregatedSavedData, List<Data> listData, Properties properties, boolean compareWithPrevious, boolean ignoreRunning, boolean ignoreDisabledJobs, boolean ignoreNotFoundJobs,
			boolean ignoreAbortedJobs) {
		logger.println(LocalMessages.ANALYZE.toString());
		// Resolve
		String outOfDateResults = properties.getProperty(TestResultsAggregator.AggregatorProperties.OUT_OF_DATE_RESULTS_ARG.name());
		// Check if Groups/Names are used
		boolean foundAtLeastOneGroupName = false;
		for (Data data : listData) {
			if (!Strings.isNullOrEmpty(data.getGroupName())) {
				foundAtLeastOneGroupName = true;
			}
		}
		// Order List per Group Name
		if (foundAtLeastOneGroupName) {
			Collections.sort(listData, new Comparator<Data>() {
				@Override
				public int compare(Data dataDTO1, Data dataDTO2) {
					return dataDTO1.getGroupName().compareTo(dataDTO2.getGroupName());
				}
			});
		}
		Aggregated aggregated = new Aggregated();
		if (aggregatedSavedData != null) {
			aggregated.setPreviousResults(aggregatedSavedData.getResults());
		}
		// Calculate Aggregated Results for Reporting
		Results totalResults = new Results();
		for (Data data : listData) {
			boolean foundFailure = false;
			boolean foundRunning = false;
			boolean foundSkip = false;
			boolean foundDisabled = false;
			Results resultsPerGroup = new Results();
			int jobFailed = 0;
			int jobUnstable = 0;
			int jobAborted = 0;
			int jobSuccess = 0;
			int jobRunning = 0;
			int jobDisabled = 0;
			boolean isOnlyTestIntoGroup = true;
			data.setReportGroup(new ReportGroup());
			
			for (Job job : data.getJobs()) {
				boolean foundIgnored = false;
				if (job.getLast() != null && job.getLast().getResults() != null) {
					if (job.getResults() != null && job.getLast().getBuildNumber() == job.getResults().getNumber() && !job.getIsBuilding() && job.getLast().getResults().getStatus().equals(job.getResults().getStatus())) {
						// Already have results and requested the same results
						calculateReport(job, outOfDateResults, ignoreRunning);
					} else {
						new Helper().calculateNewResults(job, ignoreRunning);
						calculateReport(job, outOfDateResults, ignoreRunning);
					}
					String jobStatus = job.getResults().getStatusAdvanced();
					if (jobStatus != null) {
						if (jobStatus.startsWith(JobStatus.SUCCESS.name())) {
							data.getReportGroup().setJobSuccess(data.getReportGroup().getJobSuccess() + 1);
							aggregated.setSuccessJobs(aggregated.getSuccessJobs() + 1);
							jobSuccess++;
						} else if (jobStatus.startsWith(JobStatus.FIXED.name())) {
							data.getReportGroup().setJobSuccess(data.getReportGroup().getJobSuccess() + 1);
							aggregated.setFixedJobs(aggregated.getFixedJobs() + 1);
							jobSuccess++;
						} else if (jobStatus.startsWith(JobStatus.RUNNING.name()) && (!ignoreRunning || !compareWithPrevious)) {
							foundRunning = true;
							data.getReportGroup().setJobRunning(data.getReportGroup().getJobRunning() + 1);
							aggregated.setRunningJobs(aggregated.getRunningJobs() + 1);
							jobRunning++;
						} else if (jobStatus.startsWith(JobStatus.FAILURE.name())) {
							foundFailure = true;
							data.getReportGroup().setJobFailed(data.getReportGroup().getJobFailed() + 1);
							aggregated.setFailedJobs(aggregated.getFailedJobs() + 1);
							jobFailed++;
						} else if (jobStatus.startsWith(JobStatus.STILL_FAILING.name())) {
							foundFailure = true;
							data.getReportGroup().setJobFailed(data.getReportGroup().getJobFailed() + 1);
							aggregated.setKeepFailJobs(aggregated.getKeepFailJobs() + 1);
							jobFailed++;
						} else if (jobStatus.startsWith(JobStatus.UNSTABLE.name())) {
							foundSkip = true;
							data.getReportGroup().setJobUnstable(data.getReportGroup().getJobUnstable() + 1);
							aggregated.setUnstableJobs(aggregated.getUnstableJobs() + 1);
							jobUnstable++;
						} else if (jobStatus.startsWith(JobStatus.STILL_UNSTABLE.name())) {
							foundSkip = true;
							data.getReportGroup().setJobUnstable(data.getReportGroup().getJobUnstable() + 1);
							aggregated.setKeepUnstableJobs(aggregated.getKeepUnstableJobs() + 1);
							jobUnstable++;
						} else if (jobStatus.startsWith(JobStatus.ABORTED.name()) && "false".equalsIgnoreCase((String) properties.get(AggregatorProperties.IGNORE_ABORTED_JOBS.name()))) {
							if (!ignoreAbortedJobs) {
								data.getReportGroup().setJobAborted(data.getReportGroup().getJobAborted() + 1);
								foundSkip = true;
								aggregated.setAbortedJobs(aggregated.getAbortedJobs() + 1);
								jobAborted++;
							} else {
								foundIgnored = true;
							}
						} else if (jobStatus.startsWith(JobStatus.DISABLED.name()) && "false".equalsIgnoreCase((String) properties.get(AggregatorProperties.IGNORE_DISABLED_JOBS.name()))) {
							if (!ignoreDisabledJobs) {
								foundDisabled = true;
								data.getReportGroup().setJobDisabled(data.getReportGroup().getJobDisabled() + 1);
								aggregated.setDisabledJobs(aggregated.getDisabledJobs() + 1);
								jobDisabled++;
							} else {
								foundIgnored = true;
							}
						}
						
						if (jobStatus.contains("*")) {
							data.getReportGroup().setJobRunningReportPrevious(data.getReportGroup().getJobRunningReportPrevious() + 1);
						}
					}
					if (!foundIgnored) {
						// Total Duration
						aggregated.setTotalDuration(aggregated.getTotalDuration() + job.getResults().getDuration());
						// Total Changes
						aggregated.setTotalNumberOfChanges(aggregated.getTotalNumberOfChanges() + job.getResults().getNumberOfChanges());
						// Calculate Total Tests Per Group
						resultsPerGroup.setPass(resultsPerGroup.getPass() + job.getResults().getPass());
						resultsPerGroup.setSkip(resultsPerGroup.getSkip() + job.getResults().getSkip());
						resultsPerGroup.setFail(resultsPerGroup.getFail() + job.getResults().getFail());
						resultsPerGroup.setTotal(resultsPerGroup.getTotal() + job.getResults().getTotal());
						totalResults.addResults(job.getResults());
						// Has tests
						if (job.getLast().getResults().getTotal() <= 0) {
							isOnlyTestIntoGroup = false;
						}
					}
				} else {
					if (job.getResults() != null) {
						logger.println(job.getJobName() + " has status : " + job.getResults().getStatus() + "");
					} else {
						logger.println(job.getJobName() + " no last build or report");
					}
				}
			}
			// Set Results Per Group
			data.getReportGroup().setResults(resultsPerGroup);
			// Calculate Group Status
			if (foundRunning) {
				data.getReportGroup().setStatus(JobStatus.RUNNING.name());
			} else if (foundFailure) {
				data.getReportGroup().setStatus(JobStatus.FAILURE.name());
			} else if (foundSkip) {
				data.getReportGroup().setStatus(JobStatus.UNSTABLE.name());
			} else {
				data.getReportGroup().setStatus(JobStatus.SUCCESS.name());
			}
			// Set status if only tests
			data.getReportGroup().setOnlyTests(isOnlyTestIntoGroup);
			// Calculate Percentage Per Group based on Jobs
			if (!isOnlyTestIntoGroup) {
				data.getReportGroup().setPercentageForJobs(Helper.countPercentage(jobSuccess + jobUnstable + jobRunning, jobSuccess + jobUnstable + jobRunning + jobAborted + jobFailed + jobDisabled));
			}
			// Calculate Percentage Per Group based on Tests
			data.getReportGroup().setPercentageForTests(Helper.countPercentageD(resultsPerGroup.getPass(),
					resultsPerGroup.getPass() + resultsPerGroup.getFail() + resultsPerGroup.getSkip()).toString());
		}
		// Order Jobs per
		final String orderBy = (String) properties.get(TestResultsAggregator.AggregatorProperties.SORT_JOBS_BY.name());
		for (Data data : listData) {
			Collections.sort(data.getJobs(), new Comparator<Job>() {
				@Override
				public int compare(Job dataJobDTO1, Job dataJobDTO2) {
					try {
						if (TestResultsAggregator.SortResultsBy.NAME.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO1.getJobNameFromFriendlyName().compareTo(dataJobDTO2.getJobNameFromFriendlyName());
						} else if (TestResultsAggregator.SortResultsBy.STATUS.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO1.getResults().getStatusFromEnum().getPriority() - dataJobDTO2.getResults().getStatusFromEnum().getPriority();
						} else if (TestResultsAggregator.SortResultsBy.TOTAL_TEST.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO2.getResults().getTotal() - dataJobDTO1.getResults().getTotal();
						} else if (TestResultsAggregator.SortResultsBy.PASS.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO2.getResults().getPass() - dataJobDTO1.getResults().getPass();
						} else if (TestResultsAggregator.SortResultsBy.FAIL.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO2.getResults().getFail() - dataJobDTO1.getResults().getFail();
						} else if (TestResultsAggregator.SortResultsBy.SKIP.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO2.getResults().getSkip() - dataJobDTO1.getResults().getSkip();
						} else if (TestResultsAggregator.SortResultsBy.LAST_RUN.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO1.getResults().getTimestamp().compareTo(dataJobDTO2.getResults().getTimestamp());
						} else if (TestResultsAggregator.SortResultsBy.COMMITS.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO1.getResults().getNumberOfChanges() - dataJobDTO2.getResults().getNumberOfChanges();
						} else if (TestResultsAggregator.SortResultsBy.DURATION.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO1.getResults().getDuration().compareTo(dataJobDTO2.getResults().getDuration());
						} else if (TestResultsAggregator.SortResultsBy.PERCENTAGE.name().equalsIgnoreCase(orderBy)) {
							return dataJobDTO1.getResults().getPercentage().compareTo(dataJobDTO2.getResults().getPercentage());
						} else if (TestResultsAggregator.SortResultsBy.BUILD_NUMBER.name().equalsIgnoreCase(orderBy)) {
							return Integer.toString(dataJobDTO1.getJob().getNextBuildNumber()).compareTo(Integer.toString(dataJobDTO2.getJob().getNextBuildNumber()));
						} else {
							// Default
							return dataJobDTO1.getJobNameFromFriendlyName().compareTo(dataJobDTO2.getJobNameFromFriendlyName());
						}
					} catch (Exception ex) {
						return -1;
					}
				}
			});
		}
		// Set
		aggregated.setData(listData);
		aggregated.setResults(totalResults);
		logger.println(LocalMessages.ANALYZE.toString() + " " + LocalMessages.FINISHED.toString());
		return aggregated;
	}
	
	private void calculateReport(Job job, String outOfDateResults, boolean ignoreRunningJobs) {
		if ((!job.getIsBuilding() || ignoreRunningJobs) && job.getResults() != null) {
			// Calculate Total
			job.getResults().calculateTotal(job);
			// Calculate Pass
			job.getResults().calculatePass(job);
			// Calculate Fail
			// job.getResults().calculateFailedColor(job);
			job.getResults().calculateFailed(job);
			// Calculate Skipped
			job.getResults().calculateSkipped(job);
			// Calculate timestamp
			job.getResults().calculateTimestamp(job, outOfDateResults);
			// Calculate Changes
			job.getResults().calculateChanges(job);
			// Calculate Sonar Url
			job.getResults().calculateSonar(job);
			// Calculate Coverage Packages
			job.getResults().calculateCCPackages(job);
			job.getResults().calculateCCFiles(job);
			job.getResults().calculateCCClasses(job);
			job.getResults().calculateCCMethods(job);
			job.getResults().calculateCCLines(job);
			job.getResults().calculateCCConditions(job);
			// Calculate Percentage
			job.getResults().calculatePercentage(job);
			if (job.getLast() != null) {
				// Description
				job.getResults().setDescription(job.getLast().getDescription());
				// Calculate Duration
				job.getResults().calculateDuration(job.getLast().getDuration());
			}
		}
	}
	
}
