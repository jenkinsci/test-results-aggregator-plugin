package com.jenkins.testresultsaggregator.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.offbytwo.jenkins.model.BuildWithDetails;

public class BuildWithDetailsAggregator extends BuildWithDetails {
	
	private Integer buildNumber;
	private JobResults results;
	
	@JsonIgnore
	private List<Object> artifacts;
	
	@JsonIgnore
	private String consoleOutputText;
	
	@JsonIgnore
	private String consoleOutputHtml;
	
	@JsonIgnore
	private String builtOn;
	
	@JsonIgnore
	private List<Object> culprits;
	
	public BuildWithDetailsAggregator() {
		
	}
	
	public Integer getBuildNumber() {
		if (buildNumber == null) {
			buildNumber = getNumber();
		}
		return buildNumber;
	}
	
	public void setBuildNumber(Integer buildNumber) {
		this.buildNumber = buildNumber;
	}
	
	public JobResults getResults() {
		return results;
	}
	
	public void setResults(JobResults results) {
		this.results = results;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((buildNumber == null) ? 0 : buildNumber.hashCode());
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BuildWithDetailsAggregator other = (BuildWithDetailsAggregator) obj;
		if (buildNumber == null) {
			if (other.buildNumber != null)
				return false;
		} else if (!buildNumber.equals(other.buildNumber))
			return false;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		return true;
	}
	
}
