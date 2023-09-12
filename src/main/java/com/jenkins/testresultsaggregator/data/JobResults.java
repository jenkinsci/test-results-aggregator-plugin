package com.jenkins.testresultsaggregator.data;

import java.io.Serializable;

public class JobResults implements Serializable {
	
	private static final long serialVersionUID = 3491974223667L;
	
	private String status;
	private int number;
	private String url;
	
	private int pass;
	private int fail;
	private int skip;
	private int total;
	private int ccPackages;
	private int ccFiles;
	private int ccClasses;
	private int ccMethods;
	private int ccLines;
	private int ccConditions;
	private Long duration;
	private String description;
	private boolean building;
	private String sonarUrl;
	private int numberOfChanges;
	private String changesUrl;
	private Long timestamp;
	private Double percentage;
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public int getPass() {
		return pass;
	}
	
	public void setPass(int pass) {
		this.pass = pass;
	}
	
	public int getFail() {
		return fail;
	}
	
	public void setFail(int fail) {
		this.fail = fail;
	}
	
	public int getSkip() {
		return skip;
	}
	
	public void setSkip(int skip) {
		this.skip = skip;
	}
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
	
	public int getCcPackages() {
		return ccPackages;
	}
	
	public void setCcPackages(int ccPackages) {
		this.ccPackages = ccPackages;
	}
	
	public int getCcFiles() {
		return ccFiles;
	}
	
	public void setCcFiles(int ccFiles) {
		this.ccFiles = ccFiles;
	}
	
	public int getCcClasses() {
		return ccClasses;
	}
	
	public void setCcClasses(int ccClasses) {
		this.ccClasses = ccClasses;
	}
	
	public int getCcMethods() {
		return ccMethods;
	}
	
	public void setCcMethods(int ccMethods) {
		this.ccMethods = ccMethods;
	}
	
	public int getCcLines() {
		return ccLines;
	}
	
	public void setCcLines(int ccLines) {
		this.ccLines = ccLines;
	}
	
	public int getCcConditions() {
		return ccConditions;
	}
	
	public void setCcConditions(int ccConditions) {
		this.ccConditions = ccConditions;
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
	
	public String getSonarUrl() {
		return sonarUrl;
	}
	
	public void setSonarUrl(String sonarUrl) {
		this.sonarUrl = sonarUrl;
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
	
	public Long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
	public Double getPercentage() {
		return percentage;
	}
	
	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}
	
}
