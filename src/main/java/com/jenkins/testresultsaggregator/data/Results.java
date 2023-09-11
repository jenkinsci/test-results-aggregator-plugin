package com.jenkins.testresultsaggregator.data;

import java.awt.Color;
import java.io.Serializable;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.helper.GetEnumFromString;
import com.jenkins.testresultsaggregator.helper.Helper;

public class Results implements Serializable {
	
	private static final long serialVersionUID = 3491974223667L;
	
	private String status;
	private int number;
	private String durationReport;
	private Long duration;
	private String description;
	private boolean building;
	private String url;
	private String sonarUrl;
	private String timestamp;
	
	private String percentageReport;
	private Double percentage;
	// Changes
	private String numberOfChangesReport;
	private int numberOfChanges;
	private String changesUrl;
	// Tests
	private String passReport;
	private int pass;
	private int passDif;
	private String failReport;
	private int fail;
	private int failDif;
	private String skipReport;
	private int skip;
	private int skipDif;
	private String totalReport;
	private int total;
	private int totalDif;
	// Code Coverage
	private String ccPackagesReport;
	private Integer ccPackages;
	private Integer ccPackagesDif;
	private String ccFilesReport;
	private Integer ccFiles;
	private Integer ccFilesDif;
	private String ccClassesReport;
	private Integer ccClasses;
	private Integer ccClassesDif;
	private String ccMethodsReport;
	private Integer ccMethods;
	private Integer ccMethodsDif;
	private String ccLinesReport;
	private Integer ccLines;
	private Integer ccLinesDif;
	private String ccConditionsReport;
	private Integer ccConditions;
	private Integer ccConditionsDif;
	
	public Results() {
	}
	
	public Results(String status, String url) {
		setStatus(status);
		setUrl(url);
	}
	
	public Results calculate(Job job) {
		// Calculate Job Status
		this.status = Helper.calculateStatusJob(job);
		// Number
		this.number = job.getLast().getResults().getNumber();
		// Duration
		this.duration = job.getLast().getResults().getDuration();
		// Description
		this.description = job.getLast().getResults().getDescription();
		// IsBuilding
		this.building = job.getLast().getResults().isBuilding();
		// Url
		this.url = job.getLast().getResults().getUrl();
		// Sonar Url
		this.sonarUrl = job.getLast().getResults().getSonarUrl();
		// TimeStamp
		this.timestamp = job.getLast().getResults().getTimestamp().toString();
		// Code changes
		this.numberOfChanges = job.getLast().getResults().getNumberOfChanges();
		this.changesUrl = job.getLast().getResults().getChangesUrl();
		// Tests
		this.pass = job.getLast().getResults().getPass();
		this.fail = job.getLast().getResults().getFail();
		this.skip = job.getLast().getResults().getSkip();
		this.total = job.getLast().getResults().getTotal();
		// Percentage
		// this.percentage = Helper.singDoubleSingle((double) (this.pass + this.skip) * 100 / this.total);
		this.percentageReport = Helper.singDoubleSingle((double) (this.pass + this.skip) * 100 / this.total);
		if (job.getPrevious() != null) {
			this.passDif = job.getLast().getResults().getPass() - job.getPrevious().getResults().getPass();
			this.failDif = job.getLast().getResults().getFail() - job.getPrevious().getResults().getFail();
			this.skipDif = job.getLast().getResults().getSkip() - job.getPrevious().getResults().getSkip();
			this.totalDif = job.getLast().getResults().getTotal() - job.getPrevious().getResults().getTotal();
		} else {
			this.passDif = 0;
			this.failDif = 0;
			this.skipDif = 0;
			this.totalDif = 0;
		}
		// Code Coverage
		this.ccPackages = job.getLast().getResults().getCcPackages();
		this.ccFiles = job.getLast().getResults().getCcFiles();
		this.ccClasses = job.getLast().getResults().getCcClasses();
		this.ccMethods = job.getLast().getResults().getCcMethods();
		this.ccLines = job.getLast().getResults().getCcLines();
		this.ccConditions = job.getLast().getResults().getCcConditions();
		if (job.getPrevious() != null) {
			this.ccPackagesDif = job.getLast().getResults().getCcPackages() - job.getPrevious().getResults().getCcPackages();
			this.ccFilesDif = job.getLast().getResults().getCcFiles() - job.getPrevious().getResults().getCcFiles();
			this.ccClassesDif = job.getLast().getResults().getCcClasses() - job.getPrevious().getResults().getCcClasses();
			this.ccMethodsDif = job.getLast().getResults().getCcMethods() - job.getPrevious().getResults().getCcMethods();
			this.ccLinesDif = job.getLast().getResults().getCcLines() - job.getPrevious().getResults().getCcLines();
			this.ccConditionsDif = job.getLast().getResults().getCcConditions() - job.getPrevious().getResults().getCcConditions();
		} else {
			this.ccPackagesDif = 0;
			this.ccFilesDif = 0;
			this.ccClassesDif = 0;
			this.ccMethodsDif = 0;
			this.ccLinesDif = 0;
			this.ccConditionsDif = 0;
		}
		return this;
	}
	
	public Results addResults(Results resultsDTO) {
		this.setTotal(this.getTotal() + resultsDTO.getTotal());
		this.setTotalDif(this.getTotalDif() + resultsDTO.getTotalDif());
		this.setFail(this.getFail() + resultsDTO.getFail());
		this.setFailDif(this.getFailDif() + resultsDTO.getFailDif());
		this.setPass(this.getPass() + resultsDTO.getPass());
		this.setPassDif(this.getPassDif() + resultsDTO.getPassDif());
		this.setSkip(this.getSkip() + resultsDTO.getSkip());
		this.setSkipDif(this.getSkipDif() + resultsDTO.getSkipDif());
		return this;
	}
	
	public int getPass() {
		return pass;
	}
	
	public void setPass(int pass) {
		this.pass = pass;
	}
	
	public int getPassDif() {
		return passDif;
	}
	
	public void setPassDif(int passDif) {
		this.passDif = passDif;
	}
	
	public int getFail() {
		return fail;
	}
	
	public void setFail(int fail) {
		this.fail = fail;
	}
	
	public int getFailDif() {
		return failDif;
	}
	
	public void setFailDif(int failDif) {
		this.failDif = failDif;
	}
	
	public int getSkip() {
		return skip;
	}
	
	public void setSkip(int skip) {
		this.skip = skip;
	}
	
	public int getSkipDif() {
		return skipDif;
	}
	
	public void setSkipDif(int skipDif) {
		this.skipDif = skipDif;
	}
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
	
	public int getTotalDif() {
		return totalDif;
	}
	
	public void setTotalDif(int totalDif) {
		this.totalDif = totalDif;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public String getNumberReport(boolean withLinktoResults) {
		if (withLinktoResults) {
			String reportUrl = url + number;
			return "<a href='" + reportUrl + "'>" + number + "</a>";
		}
		return "" + number;
	}
	
	public Long getDuration() {
		return duration;
	}
	
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isBuilding() {
		return building;
	}
	
	public void setBuilding(boolean building) {
		this.building = building;
	}
	
	public int getNumberOfChanges() {
		return numberOfChanges;
	}
	
	public void setNumberOfChanges(int numberOfChanges) {
		this.numberOfChanges = numberOfChanges;
	}
	
	public String getChangesUrl() {
		return changesUrl;
	}
	
	public void setChangesUrl(String changesUrl) {
		this.changesUrl = changesUrl;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Double getPercentage() {
		return percentage;
	}
	
	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getStatusColor() {
		return fixStatusName(Helper.colorizeResultStatus(status));
	}
	
	public String getStatusColor(boolean withLinktoResults) {
		if (withLinktoResults) {
			if (getUrl() != null) {
				return "<a href='" + getUrl() + "' style='text-decoration:none;'>" + fixStatusName(Helper.colorizeResultStatus(status)) + "</a>";
			}
		}
		return getStatusColor();
	}
	
	public String getStatusPlain() {
		if (getStatus() != null && getStatus().contains("*")) {
			return getStatus().replace("*", "");
		}
		return getStatus();
	}
	
	public String getCalculatedCcPackage() {
		return Helper.diff(getCcPackagesDif(), getCcPackages(), false);
	}
	
	public int getCcPackages() {
		return ccPackages;
	}
	
	public void setCcPackages(int ccPackages) {
		this.ccPackages = ccPackages;
	}
	
	public int getCcPackagesDif() {
		return ccPackagesDif;
	}
	
	public void setCcPackagesDif(int ccPackagesDif) {
		this.ccPackagesDif = ccPackagesDif;
	}
	
	public String getCalculatedCcFiles() {
		return Helper.diff(getCcFilesDif(), getCcFiles(), false);
	}
	
	public Integer getCcFiles() {
		return ccFiles;
	}
	
	public void setCcFiles(Integer ccFiles) {
		this.ccFiles = ccFiles;
	}
	
	public Integer getCcFilesDif() {
		return ccFilesDif;
	}
	
	public void setCcFilesDif(Integer ccFilesDif) {
		this.ccFilesDif = ccFilesDif;
	}
	
	public String getCalculatedCcClasses() {
		return Helper.diff(getCcClassesDif(), getCcClasses(), false);
	}
	
	public Integer getCcClasses() {
		return ccClasses;
	}
	
	public void setCcClasses(Integer ccClasses) {
		this.ccClasses = ccClasses;
	}
	
	public Integer getCcClassesDif() {
		return ccClassesDif;
	}
	
	public void setCcClassesDif(Integer ccClassesDif) {
		this.ccClassesDif = ccClassesDif;
	}
	
	public String getCalculatedCcMethods() {
		return Helper.diff(getCcMethodsDif(), getCcMethods(), false);
	}
	
	public Integer getCcMethods() {
		return ccMethods;
	}
	
	public void setCcMethods(Integer ccMethods) {
		this.ccMethods = ccMethods;
	}
	
	public Integer getCcMethodsDif() {
		return ccMethodsDif;
	}
	
	public void setCcMethodsDif(Integer ccMethodsDif) {
		this.ccMethodsDif = ccMethodsDif;
	}
	
	public String getCalculatedCcLines() {
		return Helper.diff(getCcLinesDif(), getCcLines(), false);
	}
	
	public void setCcLines(int ccLines) {
		this.ccLines = ccLines;
	}
	
	public Integer getCcLinesDif() {
		return ccLinesDif;
	}
	
	public void setCcLinesDif(Integer ccLinesDif) {
		this.ccLinesDif = ccLinesDif;
	}
	
	public String getCalculatedCcConditions() {
		return Helper.diff(getCcConditionsDif(), getCcConditions(), false);
	}
	
	public Integer getCcConditions() {
		return ccConditions;
	}
	
	public void setCcConditions(Integer ccConditions) {
		this.ccConditions = ccConditions;
	}
	
	public Integer getCcConditionsDif() {
		return ccConditionsDif;
	}
	
	public void setCcConditionsDif(Integer ccConditionsDif) {
		this.ccConditionsDif = ccConditionsDif;
	}
	
	public String getSonarUrl() {
		return sonarUrl;
	}
	
	public void setSonarUrl(String sonarUrl) {
		this.sonarUrl = sonarUrl;
	}
	
	public String getPassReport() {
		return passReport;
	}
	
	public void setPassReport(String passReport) {
		this.passReport = passReport;
	}
	
	public String getFailReport() {
		return failReport;
	}
	
	public void setFailReport(String failReport) {
		this.failReport = failReport;
	}
	
	public String getSkipReport() {
		return skipReport;
	}
	
	public void setSkipReport(String skipReport) {
		this.skipReport = skipReport;
	}
	
	public String getTotalReport() {
		return totalReport;
	}
	
	public void setTotalReport(String totalReport) {
		this.totalReport = totalReport;
	}
	
	////////////////////
	// Calculate for Job
	public String calculateDuration(Long millis) {
		setDurationReport(Helper.duration(millis));
		return getDurationReport();
	}
	
	public void calculateTotal(Job job) {
		if (job.getResults() != null) {
			setTotalReport(Helper.reportTestDiffs(null, job.getResults().getTotal(), job.getResults().getTotalDif()));
		} else {
			setTotalReport("0");
		}
	}
	
	public void calculatePass(Job job) {
		if (job != null) {
			setPassReport(Helper.reportTestDiffs(null, job.getResults().getPass(), job.getResults().getPassDif()));
		} else {
			setPassReport("0");
		}
	}
	
	public void calculateFailed(Job job) {
		if (job != null) {
			setFailReport(Helper.reportTestDiffs(Color.RED, job.getResults().getFail(), job.getResults().getFailDif()));
		} else {
			setFailReport("0");
		}
	}
	
	public void calculateSkipped(Job job) {
		if (job != null) {
			setSkipReport(Helper.reportTestDiffs(null, job.getResults().getSkip(), job.getResults().getSkipDif()));
		} else {
			setSkipReport("0");
		}
	}
	
	public String calculateTimestamp(Job job, String outOfDateResults) {
		if (Strings.isNullOrEmpty(outOfDateResults)) {
			setTimestamp(Helper.getTimeStamp(job.getLast().getResults().getTimestamp()));
		} else {
			setTimestamp(Helper.getTimeStamp(outOfDateResults, job.getLast().getResults().getTimestamp()));
		}
		return getTimestamp();
	}
	
	public String calculateChanges(Job job) {
		setNumberOfChangesReport(Helper.urlNumberofChanges(job.getLast().getResults().getChangesUrl(), Helper.getNumber(job.getLast().getResults().getNumberOfChanges())));
		return getNumberOfChangesReport();
	}
	
	public String calculateSonar(Job job) {
		if (!Strings.isNullOrEmpty(job.getLast().getResults().getSonarUrl())) {
			setSonarUrl("<a href='" + job.getLast().getResults().getSonarUrl() + "' style='text-decoration:none;'>Sonar</a>");
			return getSonarUrl();
		}
		return "";
	}
	
	public String calculateCCPackages(Job job) {
		if (job.getResults() != null && job.getResults().getCcPackages() > 0) {
			setCcPackagesReport(Helper.diff(job.getResults().getCcPackagesDif(), job.getResults().getCcPackages(), false) + "%");
		} else {
			setCcPackagesReport("");
		}
		return getCcPackagesReport();
	}
	
	public String calculateCCFiles(Job job) {
		if (job.getResults() != null && job.getResults().getCcFiles() > 0) {
			setCcFilesReport(Helper.diff(job.getResults().getCcFilesDif(), job.getResults().getCcFiles(), false) + "%");
		} else {
			setCcFilesReport("");
		}
		return getCcFilesReport();
	}
	
	public String calculateCCClasses(Job job) {
		if (job.getResults() != null && job.getResults().getCcClasses() > 0) {
			setCcClassesReport(Helper.diff(job.getResults().getCcClassesDif(), job.getResults().getCcClasses(), false) + "%");
		} else {
			setCcClassesReport("");
		}
		return getCcClassesReport();
	}
	
	public String calculateCCMethods(Job job) {
		if (job.getResults() != null && job.getResults().getCcMethods() > 0) {
			setCcMethodsReport(Helper.diff(job.getResults().getCcMethodsDif(), job.getResults().getCcMethods(), false) + "%");
		} else {
			setCcMethodsReport("");
		}
		return getCcMethodsReport();
	}
	
	public String calculateCCLines(Job job) {
		if (job.getResults() != null && job.getResults().getCcLines() > 0) {
			setCcLinesReport(Helper.diff(job.getResults().getCcLinesDif(), job.getResults().getCcLines(), false) + "%");
		} else {
			setCcLinesReport("");
		}
		return getCcLinesReport();
	}
	
	public String calculateCCConditions(Job job) {
		if (job.getResults() != null && job.getResults().getCcConditions() > 0) {
			setCcConditionsReport(Helper.diff(job.getResults().getCcConditionsDif(), job.getResults().getCcConditions(), false) + "%");
		} else {
			setCcConditionsReport("");
		}
		return getCcConditionsReport();
	}
	
	public String getCcPackagesReport() {
		return ccPackagesReport;
	}
	
	public void setCcPackagesReport(String ccPackagesReport) {
		this.ccPackagesReport = ccPackagesReport;
	}
	
	public String getCcFilesReport() {
		return ccFilesReport;
	}
	
	public void setCcFilesReport(String ccFilesReport) {
		this.ccFilesReport = ccFilesReport;
	}
	
	public String getCcClassesReport() {
		return ccClassesReport;
	}
	
	public void setCcClassesReport(String ccClassesReport) {
		this.ccClassesReport = ccClassesReport;
	}
	
	public String getCcMethodsReport() {
		return ccMethodsReport;
	}
	
	public void setCcMethodsReport(String ccMethodsReport) {
		this.ccMethodsReport = ccMethodsReport;
	}
	
	public String getCcLinesReport() {
		return ccLinesReport;
	}
	
	public void setCcLinesReport(String ccLinesReport) {
		this.ccLinesReport = ccLinesReport;
	}
	
	public String getCcConditionsReport() {
		return ccConditionsReport;
	}
	
	public void setCcConditionsReport(String ccConditionsReport) {
		this.ccConditionsReport = ccConditionsReport;
	}
	
	public Integer getCcLines() {
		return ccLines;
	}
	
	public void setCcLines(Integer ccLines) {
		this.ccLines = ccLines;
	}
	
	public String getDurationReport() {
		return durationReport;
	}
	
	public void setDurationReport(String durationReport) {
		this.durationReport = durationReport;
	}
	
	public String getNumberOfChangesReport() {
		return numberOfChangesReport;
	}
	
	public void setNumberOfChangesReport(String numberOfChangesReport) {
		this.numberOfChangesReport = numberOfChangesReport;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	// Other
	private String fixStatusName(String jobStatus) {
		if (jobStatus != null) {
			return jobStatus.replace("_", " ");
		}
		return jobStatus;
	}
	
	public void calculatePercentage(Job job) {
		if (JobStatus.ABORTED.name().equalsIgnoreCase(job.getResults().getStatus()) ||
				JobStatus.DISABLED.name().equalsIgnoreCase(job.getResults().getStatus()) ||
				JobStatus.NOT_FOUND.name().equalsIgnoreCase(job.getResults().getStatus()) ||
				JobStatus.RUNNING.name().equalsIgnoreCase(job.getResults().getStatus())) {
			setPercentage(null);
		} else {
			Double calculatedPercentage = Helper.countPercentage(job.getResults());
			if (calculatedPercentage > 0) {
				setPercentage(Helper.countPercentage(job.getResults()));
			} else {
				setPercentage(null);
			}
		}
	}
	
	public String getPercentageReport() {
		return percentageReport;
	}
	
	public void setPercentageReport(String percentageReport) {
		this.percentageReport = percentageReport;
	}
	
	public JobStatus getStatusFromEnum() {
		return GetEnumFromString.get(JobStatus.class, status);
	}
	
}
