package com.jenkins.testresultsaggregator.data;

import java.awt.Color;
import java.io.Serializable;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.helper.Colors;
import com.jenkins.testresultsaggregator.helper.Helper;

public class ReportGroup implements Serializable {
	
	private static final long serialVersionUID = 3491199923666L;
	
	private String status;
	private String percentageForJobs;
	private String percentageForTests;
	private int jobSuccess;
	private int jobFailed;
	private int jobUnstable;
	private int jobRunning;
	private int jobAborted;
	private int jobDisabled;
	private Results results;
	private boolean onlyTests;
	
	public ReportGroup() {
		this.jobSuccess = 0;
		this.jobFailed = 0;
		this.jobUnstable = 0;
		this.jobRunning = 0;
		this.jobAborted = 0;
		this.onlyTests = true;
		this.setResults(new Results());
	}
	
	public ReportGroup(String status, int jobSuccess, int jobFailed, int jobUnstable, int jobRunning, int jobAborted, Results resultsDTO) {
		super();
		this.status = status;
		this.jobSuccess = jobSuccess;
		this.jobFailed = jobFailed;
		this.jobUnstable = jobUnstable;
		this.jobRunning = jobRunning;
		this.jobAborted = jobAborted;
		this.setResults(resultsDTO);
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getJobSuccess() {
		return jobSuccess;
	}
	
	public void setJobSuccess(int jobSuccess) {
		this.jobSuccess = jobSuccess;
	}
	
	public int getJobFailed() {
		return jobFailed;
	}
	
	public void setJobFailed(int jobFailed) {
		this.jobFailed = jobFailed;
	}
	
	public int getJobUnstable() {
		return jobUnstable;
	}
	
	public void setJobUnstable(int jobUnstable) {
		this.jobUnstable = jobUnstable;
	}
	
	public int getJobRunning() {
		return jobRunning;
	}
	
	public void setJobRunning(int jobRunning) {
		this.jobRunning = jobRunning;
	}
	
	public int getJobAborted() {
		return jobAborted;
	}
	
	public void setJobAborted(int jobAborted) {
		this.jobAborted = jobAborted;
	}
	
	public Results getResults() {
		return results;
	}
	
	public void setResults(Results results) {
		this.results = results;
	}
	
	public String getPercentageForJobs(boolean withColor, Integer fontSize) {
		if (!Strings.isNullOrEmpty(percentageForJobs) && Helper.resolvePercentage(percentageForJobs) >= 0) {
			if (withColor) {
				return Helper.colorizePercentage(Helper.resolvePercentage(percentageForJobs), fontSize, status);
			} else {
				return Helper.resolvePercentage(percentageForJobs).toString();
			}
		}
		return "";
	}
	
	public Double getPercentageForJobs() {
		if (!Strings.isNullOrEmpty(percentageForJobs) && Helper.resolvePercentage(percentageForJobs) >= 0) {
			return Helper.resolvePercentage(percentageForJobs);
		}
		return -1D;
	}
	
	public void setPercentageForJobs(String percentageForJobs) {
		this.percentageForJobs = percentageForJobs;
	}
	
	public String getPercentageForTests(boolean withColor, Integer fontSize) {
		if (!Strings.isNullOrEmpty(percentageForTests) && Helper.resolvePercentage(percentageForTests) >= 0) {
			if (withColor) {
				return Helper.colorizePercentage(Helper.resolvePercentage(percentageForTests), fontSize, status);
			} else {
				return Helper.resolvePercentage(percentageForTests).toString();
			}
		}
		return "";
	}
	
	public Double getPercentageForTests() {
		if (!Strings.isNullOrEmpty(percentageForTests) && Helper.resolvePercentage(percentageForTests) >= 0) {
			return Helper.resolvePercentage(percentageForTests);
		}
		return -1D;
	}
	
	public void setPercentageForTests(String percentageForTests) {
		this.percentageForTests = percentageForTests;
	}
	
	//
	public String getPercentage(boolean jobs, boolean tests, boolean withColor) {
		StringBuilder percentage = new StringBuilder();
		String jobPercentage = getPercentageForJobs(false, null);
		String testPercentage = getPercentageForTests(false, null);
		int fontSize = 12;
		String fontColor = Colors.html(Color.gray);
		if (Helper.resolvePercentage(jobPercentage) > 0 && Helper.resolvePercentage(jobPercentage) < 100) {
			percentage.append(getPercentageForJobs(withColor, fontSize));
		} else if (Helper.resolvePercentage(testPercentage) > 0 && Helper.resolvePercentage(testPercentage) < 100) {
			percentage.append(getPercentageForTests(withColor, fontSize));
		} else if (Helper.resolvePercentage(testPercentage).equals(Helper.resolvePercentage(jobPercentage))) {
			percentage.append(getPercentageForTests(withColor, fontSize));
		} else {
			if (!Strings.isNullOrEmpty(jobPercentage)) {
				percentage.append(getPercentageForJobs(withColor, fontSize));
				if (!Strings.isNullOrEmpty(testPercentage)) {
					percentage.append("<font style='font-size:" + (fontSize - 2) + "px;color:" + fontColor + "'> Jobs</font>").append("<br>");
				}
			}
			if (!Strings.isNullOrEmpty(testPercentage)) {
				percentage.append(getPercentageForTests(withColor, fontSize));
				if (!Strings.isNullOrEmpty(jobPercentage)) {
					percentage.append("<font style='font-size:" + (fontSize - 2) + "px;color:" + fontColor + "'> Tests</font>");
				}
			}
		}
		return percentage.toString();
	}
	
	public int getJobDisabled() {
		return jobDisabled;
	}
	
	public void setJobDisabled(int jobDisabled) {
		this.jobDisabled = jobDisabled;
	}
	
	public boolean isOnlyTests() {
		return onlyTests;
	}
	
	public void setOnlyTests(boolean onlyTests) {
		this.onlyTests = onlyTests;
	}
}
