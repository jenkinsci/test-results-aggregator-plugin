package com.jenkins.testresultsaggregator.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jenkins.testresultsaggregator.data.JobResults;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.offbytwo.jenkins.model.BuildWithDetails;

public class Actions {
	
	private BuildWithDetails buildDetails;
	private JobResults jobResults;
	private List<?> actionList;
	// Actions
	public static final String CHANGES = "changes";
	public static final String FAILCOUNT = "failCount";
	public static final String SKIPCOUNT = "skipCount";
	public static final String TOTALCOUNT = "totalCount";
	public static final String JACOCO_BRANCH = "branchCoverage";
	public static final String JACOCO_CLASS = "classCoverage";
	public static final String JACOCO_LINES = "lineCoverage";
	public static final String JACOCO_METHODS = "methodCoverage";
	public static final String JACOCO_INSTRUCTION = "instructionCoverage";
	public static final String SONAR_URL = "sonarqubeDashboardUrl";
	
	public Actions(JobResults jobResults, BuildWithDetails buildDetails) {
		this.buildDetails = buildDetails;
		this.jobResults = jobResults;
		this.actionList = buildDetails.getActions();
	}
	
	public JobResults calculate() {
		if (buildDetails.isBuilding()) {
			jobResults.setStatus(JobStatus.RUNNING.name());
		} else {
			jobResults.setStatus(buildDetails.getResult().name());
		}
		jobResults.setNumber(buildDetails.getNumber());
		jobResults.setUrl(buildDetails.getUrl());
		jobResults.setDuration(buildDetails.getDuration());
		jobResults.setDescription(buildDetails.getDescription());
		jobResults.setTimestamp(buildDetails.getTimestamp());
		jobResults.setNumberOfChanges(buildDetails.getChangeSets().size());
		jobResults.setChangesUrl(buildDetails.getUrl() + "/changes");
		for (Object temp : actionList) {
			HashMap<Object, Object> actions = (HashMap<Object, Object>) temp;
			if (actions.containsKey("_class") && !actions.get("_class").equals("com.jenkins.testresultsaggregator.TestResultsAggregatorTestResultBuildAction")) {
				// Calculate FAIL,SKIP and TOTAL Test Results
				if (actions.containsKey(FAILCOUNT)) {
					jobResults.setFail((Integer) actions.get(FAILCOUNT));
				}
				if (actions.containsKey(SKIPCOUNT)) {
					jobResults.setSkip((Integer) actions.get(SKIPCOUNT));
				}
				if (actions.containsKey(TOTALCOUNT)) {
					jobResults.setTotal((Integer) actions.get(TOTALCOUNT));
				}
				// Jacoco
				if (actions.containsKey(JACOCO_BRANCH)) {
					Map<String, Object> tempMap = (Map<String, Object>) actions.get(JACOCO_BRANCH);
					jobResults.setCcConditions((Integer) tempMap.get("percentage"));
				}
				if (actions.containsKey(JACOCO_CLASS)) {
					Map<String, Object> tempMap = (Map<String, Object>) actions.get(JACOCO_CLASS);
					jobResults.setCcClasses((Integer) tempMap.get("percentage"));
				}
				if (actions.containsKey(JACOCO_LINES)) {
					Map<String, Object> tempMap = (Map<String, Object>) actions.get(JACOCO_LINES);
					jobResults.setCcLines((Integer) tempMap.get("percentage"));
				}
			}
			if (actions.containsKey(JACOCO_METHODS)) {
				Map<String, Object> tempMap = (Map<String, Object>) actions.get(JACOCO_METHODS);
				jobResults.setCcMethods((Integer) tempMap.get("percentage"));
			}
			if (actions.containsKey(SONAR_URL)) {
				jobResults.setSonarUrl((String) actions.get(SONAR_URL));
			}
		}
		// Cobertura
		// ?
		// Calculate Pass Results
		jobResults.setPass(jobResults.getTotal() - Math.abs(jobResults.getFail()) - Math.abs(jobResults.getSkip()));
		// Calculate Percentage
		jobResults.setPercentage(Helper.countPercentage(jobResults));
		return jobResults;
	}
	
}