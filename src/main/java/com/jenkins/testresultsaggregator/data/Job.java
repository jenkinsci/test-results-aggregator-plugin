package com.jenkins.testresultsaggregator.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.helper.Colors;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Job extends AbstractDescribableImpl<Job> implements Serializable {
	
	private static final long serialVersionUID = 34911974223666L;
	
	private String jobName;
	private String jobFriendlyName;
	private String jobNameOnly;
	private String url;
	private String folder;
	private boolean isBuilding;
	// Job
	private JobWithDetailsAggregator job;
	// Last Build
	private BuildWithDetailsAggregator last;
	// Previous Build
	private BuildWithDetailsAggregator previous;
	// Results
	private Results results;
	
	@Extension
	public static class JobDescriptor extends Descriptor<Job> {
		@Override
		public String getDisplayName() {
			return "";
		}
		
	}
	
	@DataBoundConstructor
	public Job() {
		
	}
	
	public Job(String jobName, String jobFriendlyName) {
		setJobName(jobName);
		setJobFriendlyName(jobFriendlyName);
	}
	
	public String getJobName() {
		if (jobName != null) {
			return jobName.trim();
		}
		return jobName;
	}
	
	@DataBoundSetter
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public String getJobFriendlyName() {
		if (jobFriendlyName != null) {
			return jobFriendlyName.trim();
		}
		return jobFriendlyName;
	}
	
	@DataBoundSetter
	public void setJobFriendlyName(String jonFriendlyName) {
		this.jobFriendlyName = jonFriendlyName;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getFolder() {
		return folder;
	}
	
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	public JobWithDetailsAggregator getJob() {
		return job;
	}
	
	public void setJob(JobWithDetailsAggregator jobWithDetailsAggregator) {
		this.job = jobWithDetailsAggregator;
	}
	
	public Results getResults() {
		return results;
	}
	
	public void setResults(Results results) {
		this.results = results;
	}
	
	public String getJobNameFromFriendlyName() {
		if (Strings.isNullOrEmpty(jobFriendlyName)) {
			return jobName;
		}
		return jobFriendlyName;
	}
	
	public String getJobNameFromFriendlyName(boolean withLinktoResults) {
		if (withLinktoResults) {
			String reportUrl = null;
			if (last == null) {
				reportUrl = null;
				// Get job and execution id
			} else if (Strings.isNullOrEmpty(last.getUrl())) {
				reportUrl = null;
			} else {
				reportUrl = last.getResults().getUrl();
			}
			// iF this is still null Use Job url
			if (Strings.isNullOrEmpty(reportUrl)) {
				reportUrl = url;
			}
			return "<a href='" + reportUrl + "'><font color='" + Colors.htmlJOB_NAME_URL() + "'>" + getJobNameFromFriendlyName() + "</font></a>";
		}
		return getJobNameFromFriendlyName();
	}
	
	public boolean getIsBuilding() {
		return isBuilding;
	}
	
	public void setIsBuilding(boolean isBuilding) {
		this.isBuilding = isBuilding;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
	}
	
	public String getJobNameOnly() {
		return jobNameOnly;
	}
	
	public void setJobNameOnly(String jobNameOnly) {
		this.jobNameOnly = jobNameOnly;
	}
	
	public BuildWithDetailsAggregator getLast() {
		return last;
	}
	
	public void setLast(BuildWithDetailsAggregator last) {
		this.last = last;
	}
	
	public BuildWithDetailsAggregator getPrevious() {
		return previous;
	}
	
	public void setPrevious(BuildWithDetailsAggregator previous) {
		this.previous = previous;
	}
	
}
