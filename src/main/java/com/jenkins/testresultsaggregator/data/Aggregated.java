package com.jenkins.testresultsaggregator.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.TestResultsAggregator;
import com.jenkins.testresultsaggregator.helper.Colors;
import com.jenkins.testresultsaggregator.helper.Helper;

import hudson.model.Run;
import hudson.tasks.test.TestResult;

public class Aggregated extends BaseResult {
	
	private List<Data> data;
	private Results results;
	private Results previousResults;
	private int runningJobs = 0;
	private int successJobs = 0;
	private int fixedJobs = 0;
	private int failedJobs = 0;
	private int keepFailJobs = 0;
	private int unstableJobs = 0;
	private int keepUnstableJobs = 0;
	private int abortedJobs = 0;
	private int disabledJobs = 0;
	private int totalJobs = 0;
	private Long totalDuration = 0L;
	private int totalNumberOfChanges = 0;
	
	private Boolean ignoreDisabledJobs;
	private Boolean ignoreNotFoundJobs;
	private Boolean ignoreRunningJobs;
	private Boolean compareWithPreviousRun;
	
	private Run<?, ?> owner;
	
	public Aggregated() {
		super(TestResultsAggregator.URL);
	}
	
	public List<Data> getData() {
		return data;
	}
	
	public void setData(List<Data> data) {
		this.data = data;
	}
	
	public Results getResults() {
		return results;
	}
	
	public void setResults(Results results) {
		this.results = results;
	}
	
	public int getRunningJobs() {
		return runningJobs;
	}
	
	public void setRunningJobs(int runningJobs) {
		this.runningJobs = runningJobs;
	}
	
	public int getSuccessJobs() {
		return successJobs;
	}
	
	public void setSuccessJobs(int successJobs) {
		this.successJobs = successJobs;
	}
	
	public int getFailedJobs() {
		return failedJobs;
	}
	
	public void setFailedJobs(int countJobFailures) {
		this.failedJobs = countJobFailures;
	}
	
	public int getUnstableJobs() {
		return unstableJobs;
	}
	
	public void setUnstableJobs(int unstableJobs) {
		this.unstableJobs = unstableJobs;
	}
	
	public int getAbortedJobs() {
		return abortedJobs;
	}
	
	public void setAbortedJobs(int abortedJobs) {
		this.abortedJobs = abortedJobs;
	}
	
	public int getFixedJobs() {
		return fixedJobs;
	}
	
	public void setFixedJobs(int fixedJobs) {
		this.fixedJobs = fixedJobs;
	}
	
	public int getKeepFailJobs() {
		return keepFailJobs;
	}
	
	public void setKeepFailJobs(int keepFailJobs) {
		this.keepFailJobs = keepFailJobs;
	}
	
	public int getKeepUnstableJobs() {
		return keepUnstableJobs;
	}
	
	public void setKeepUnstableJobs(int keepUnstableJobs) {
		this.keepUnstableJobs = keepUnstableJobs;
	}
	
	public Run<?, ?> getRun() {
		return owner;
	}
	
	public void setRun(Run<?, ?> owner) {
		this.owner = owner;
	}
	
	public int getTotalJobs() {
		setTotalJobs(runningJobs + successJobs + fixedJobs + failedJobs + keepFailJobs + unstableJobs + keepUnstableJobs + abortedJobs + disabledJobs);
		return totalJobs;
	}
	
	public void setTotalJobs(int totalJobs) {
		this.totalJobs = totalJobs;
	}
	
	@Override
	public Collection<? extends TestResult> getChildren() {
		return null;
	}
	
	@Override
	public boolean hasChildren() {
		return false;
	}
	
	public String getPercentageSummary(boolean withColor) {
		StringBuilder percentage = new StringBuilder("<br>");
		int fontSize = 12;
		String status = null;
		if (runningJobs > 0) {
			status = JobStatus.RUNNING.toString();
		}
		String jobPercentage = calculatePercentageOfJobs(false, fontSize, status);
		String testPercentage = calculatePercentage(false, fontSize, status);
		String fontColor = Colors.html(Color.gray);
		if (!Strings.isNullOrEmpty(jobPercentage)) {
			percentage.append(calculatePercentageOfJobs(withColor, fontSize, status));
			if (!Strings.isNullOrEmpty(testPercentage)) {
				percentage.append("<font style='font-size:" + (fontSize - 2) + "px;color:" + fontColor + "'> Jobs</font>").append("<br>");
			}
		}
		if (!Strings.isNullOrEmpty(testPercentage)) {
			percentage.append(calculatePercentage(withColor, fontSize, status));
			if (!Strings.isNullOrEmpty(jobPercentage)) {
				percentage.append("<font style='font-size:" + (fontSize - 2) + "px;color:" + fontColor + "'> Tests</font>");
			}
		}
		return percentage.toString();
	}
	
	public String calculatePercentage(boolean withColor, int fontSize, String status) {
		if (withColor) {
			return Helper.colorizePercentage(Helper.countPercentage(results), fontSize, status);
		}
		return Helper.countPercentage(results).toString();
	}
	
	public String calculatePercentage() {
		return Helper.countPercentage(results).toString();
	}
	
	public Double calculatePercentageOfSuccessJobs() {
		return Helper.countPercentageD(successJobs + fixedJobs, getTotalJobs());
	}
	
	public Double calculatePercentageOfFailedJobs() {
		return Helper.countPercentageD(failedJobs + keepFailJobs, getTotalJobs());
	}
	
	public Double calculatePercentageOfUnstableJobs() {
		return Helper.countPercentageD(unstableJobs + keepUnstableJobs, getTotalJobs());
	}
	
	public String calculatePercentageOfJobs(boolean withColor, int fontSize, String status) {
		if (withColor) {
			return Helper.colorizePercentage(Helper.countPercentageD(successJobs + fixedJobs, getTotalJobs()), fontSize, status);
		}
		return Helper.countPercentageD(successJobs + fixedJobs, getTotalJobs()).toString();
	}
	
	public String calculateTotalDuration() {
		return Helper.duration(totalDuration);
	}
	
	public Long getTotalDuration() {
		return totalDuration;
	}
	
	public void setTotalDuration(Long totalDuration) {
		this.totalDuration = totalDuration;
	}
	
	public int getTotalNumberOfChanges() {
		return totalNumberOfChanges;
	}
	
	public void setTotalNumberOfChanges(int totalNumberOfChanges) {
		this.totalNumberOfChanges = totalNumberOfChanges;
	}
	
	public int getFailed() {
		return failedJobs + keepFailJobs;
	}
	
	public int getUnstable() {
		return unstableJobs + keepUnstableJobs;
	}
	
	public int getDisabledJobs() {
		return disabledJobs;
	}
	
	public void setDisabledJobs(int disabledJobs) {
		this.disabledJobs = disabledJobs;
	}
	
	public Aggregated copy() {
		Aggregated aggregatedCopy = new Aggregated();
		aggregatedCopy.setAbortedJobs(abortedJobs);
		List<Data> copiedDataList = new ArrayList<>();
		for (Data tempData : data) {
			copiedDataList.add(tempData.copy());
		}
		aggregatedCopy.setData(copiedDataList);
		// aggregatedCopy.setDescription(description);
		aggregatedCopy.setDisabledJobs(disabledJobs);
		aggregatedCopy.setFailedJobs(failedJobs);
		aggregatedCopy.setFixedJobs(fixedJobs);
		aggregatedCopy.setKeepFailJobs(keepFailJobs);
		aggregatedCopy.setKeepUnstableJobs(keepUnstableJobs);
		// aggregatedCopy.setParent(aggregatedCopy);
		// aggregatedCopy.setParentAction(aggregatedCopy);
		aggregatedCopy.setResults(results);
		aggregatedCopy.setPreviousResults(previousResults);
		// aggregatedCopy.setRun(owner);
		aggregatedCopy.setRunningJobs(runningJobs);
		aggregatedCopy.setSuccessJobs(successJobs);
		aggregatedCopy.setTotalDuration(totalDuration);
		aggregatedCopy.setTotalJobs(totalJobs);
		aggregatedCopy.setTotalNumberOfChanges(totalNumberOfChanges);
		aggregatedCopy.setUnstableJobs(unstableJobs);
		aggregatedCopy.setDisabledJobs(disabledJobs);
		return aggregatedCopy;
	}
	
	public Results getPreviousResults() {
		return previousResults;
	}
	
	public void setPreviousResults(Results previousResults) {
		this.previousResults = previousResults;
	}
	
	////////////////////
	// Calculate for Total/Summary report
	public String getCalculatedTotal() {
		long dif = results.getTotal();
		if (previousResults != null) {
			dif = dif - previousResults.getTotal();
		}
		return Helper.reportTestDiffs(null, null, results.getTotal(), dif);
	}
	
	public String getCalculatedPass() {
		long dif = results.getPass();
		if (previousResults != null) {
			dif = dif - previousResults.getPass();
		}
		return Helper.reportTestDiffs(null, null, results.getPass(), dif);
	}
	
	public String getCalculatedSkip() {
		long dif = results.getSkip();
		if (previousResults != null) {
			dif = dif - previousResults.getSkip();
		}
		return Helper.reportTestDiffs(null, null, results.getSkip(), dif);
	}
	
	public String getCalculatedFail() {
		long dif = results.getFail();
		if (previousResults != null) {
			dif = dif - previousResults.getFail();
		}
		return Helper.reportTestDiffs(null, Colors.FAILED, results.getFail(), dif);
	}
	
	public Boolean getIgnoreDisabledJobs() {
		return ignoreDisabledJobs;
	}
	
	public void setIgnoreDisabledJobs(Boolean ignoreDisabledJobs) {
		this.ignoreDisabledJobs = ignoreDisabledJobs;
	}
	
	public Boolean getIgnoreNotFoundJobs() {
		return ignoreNotFoundJobs;
	}
	
	public void setIgnoreNotFoundJobs(Boolean ignoreNotFoundJobs) {
		this.ignoreNotFoundJobs = ignoreNotFoundJobs;
	}
	
	public Boolean getIgnoreRunningJobs() {
		return ignoreRunningJobs;
	}
	
	public void setIgnoreRunningJobs(Boolean ignoreRunningJobs) {
		this.ignoreRunningJobs = ignoreRunningJobs;
	}
	
	public Boolean getCompareWithPreviousRun() {
		return compareWithPreviousRun;
	}
	
	public void setCompareWithPreviousRun(Boolean compareWithPreviousRun) {
		this.compareWithPreviousRun = compareWithPreviousRun;
	}
	
}
