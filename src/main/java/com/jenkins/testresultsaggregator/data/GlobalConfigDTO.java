package com.jenkins.testresultsaggregator.data;

public class GlobalConfigDTO {
	
	private String jenkinsUrl;
	private String userName;
	private String password;
	private String mailNotificationFrom;
	
	public GlobalConfigDTO() {
		
	}
	
	public GlobalConfigDTO(String jenkinsUrl, String userName, String password, String mailNotificationFrom) {
		setJenkinsUrl(jenkinsUrl);
		setUserName(userName);
		setPassword(password);
		setMailNotificationFrom(mailNotificationFrom);
	}
	
	public String getJenkinsUrl() {
		return jenkinsUrl;
	}
	
	public void setJenkinsUrl(String jenkinsUrl) {
		this.jenkinsUrl = jenkinsUrl;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getMailNotificationFrom() {
		return mailNotificationFrom;
	}
	
	public void setMailNotificationFrom(String mailNotificationFrom) {
		this.mailNotificationFrom = mailNotificationFrom;
	}
	
}
