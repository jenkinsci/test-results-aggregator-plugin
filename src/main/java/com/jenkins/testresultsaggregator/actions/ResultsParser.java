package com.jenkins.testresultsaggregator.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jenkins.testresultsaggregator.TestResultsAggregatorProjectAction;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.jenkins.testresultsaggregator.data.JobWithDetailsAggregator;
import com.jenkins.testresultsaggregator.data.Results;
import com.jenkins.testresultsaggregator.reports.XMLReporter;

import hudson.FilePath;

public class ResultsParser {
	
	public ResultsParser() {
	}
	
	public Aggregated parse(FilePath[] paths) {
		if (null == paths) {
			return new Aggregated();
		}
		Aggregated finalResults = new Aggregated();
		finalResults.setResults(new Results());
		for (FilePath path : paths) {
			File file = new File(path.getRemote());
			if (!file.isFile()) {
				continue;
			} else {
				try {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					org.w3c.dom.Document doc = dBuilder.parse(file);
					doc.getDocumentElement().normalize();
					NodeList nList = doc.getElementsByTagName(XMLReporter.ROOT);
					if (nList != null && nList.getLength() == 1) {
						Node aggregated = nList.item(0);
						Node results = null;
						Node jobs = null;
						Node config = null;
						// Resolve Results and Job XML elements
						for (int i = 0; i < aggregated.getChildNodes().getLength(); i++) {
							if (aggregated.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
								if (XMLReporter.RESULTS.equalsIgnoreCase(aggregated.getChildNodes().item(i).getNodeName())) {
									results = aggregated.getChildNodes().item(i);
								} else if (XMLReporter.JOBS.equalsIgnoreCase(aggregated.getChildNodes().item(i).getNodeName())) {
									jobs = aggregated.getChildNodes().item(i);
								} else if (XMLReporter.CONFIG.equalsIgnoreCase(aggregated.getChildNodes().item(i).getNodeName())) {
									config = aggregated.getChildNodes().item(i);
								}
							}
						}
						// Read Config
						readConfig(finalResults, config);
						// Read Results
						readResults(finalResults, results);
						// Read Jobs
						readJobs(finalResults, jobs);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return finalResults;
	}
	
	private int getInteger(Node currentNodeResults) {
		try {
			return Integer.parseInt(currentNodeResults.getTextContent());
		} catch (Exception ex) {
			
		}
		return 0;
	}
	
	private String getString(Node currentNodeResults) {
		try {
			return currentNodeResults.getTextContent();
		} catch (Exception ex) {
			
		}
		return "";
	}
	
	private Long getLong(Node currentNodeResults) {
		try {
			return Long.parseLong(currentNodeResults.getTextContent());
		} catch (Exception ex) {
			
		}
		return 0L;
	}
	
	private Boolean getBoolean(Node currentNodeResults) {
		return Boolean.parseBoolean(currentNodeResults.getTextContent());
	}
	
	private void readResults(Aggregated finalResults, Node results) {
		if (results != null) {
			for (int i = 0; i < results.getChildNodes().getLength(); i++) {
				Node currentNodeResults = results.getChildNodes().item(i);
				if (currentNodeResults.getNodeType() == Node.ELEMENT_NODE) {
					if (!currentNodeResults.getNodeName().startsWith("#")) {
						if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_ABORTED)) {
							finalResults.setAbortedJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_FAILED)) {
							finalResults.setFailedJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_FAILED_KEEP)) {
							finalResults.setKeepFailJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_RUNNING)) {
							finalResults.setRunningJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_SUCCESS)) {
							finalResults.setSuccessJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_FIXED)) {
							finalResults.setFixedJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_UNSTABLE)) {
							finalResults.setUnstableJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_UNSTABLE_KEEP)) {
							finalResults.setKeepUnstableJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.JOB_DISABLED)) {
							finalResults.setDisabledJobs(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_TOTAL)) {
							finalResults.getResults().setTotal(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_TOTAL_DIF)) {
							finalResults.getResults().setTotalDif(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SUCCESS)) {
							finalResults.getResults().setPass(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SUCCESS_DIF)) {
							finalResults.getResults().setPassDif(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SKIPPED)) {
							finalResults.getResults().setSkip(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SKIPPED_DIF)) {
							finalResults.getResults().setSkipDif(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_FAILED)) {
							finalResults.getResults().setFail(getInteger(currentNodeResults));
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_FAILED_DIF)) {
							finalResults.getResults().setFailDif(getInteger(currentNodeResults));
						}
					}
				}
			}
		}
	}
	
	private void readConfig(Aggregated finalResults, Node results) {
		if (results != null) {
			for (int i = 0; i < results.getChildNodes().getLength(); i++) {
				Node currentNodeResults = results.getChildNodes().item(i);
				if (currentNodeResults.getNodeType() == Node.ELEMENT_NODE) {
					if (!currentNodeResults.getNodeName().startsWith("#")) {
						if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.IGNOREDISABLEDJOBS)) {
							try {
								finalResults.setIgnoreDisabledJobs(getBoolean(currentNodeResults));
							} catch (Exception ex) {
								finalResults.setIgnoreDisabledJobs(null);
							}
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.IGNORENOTFOUNDJOBS)) {
							try {
								finalResults.setIgnoreNotFoundJobs(getBoolean(currentNodeResults));
							} catch (Exception ex) {
								finalResults.setIgnoreDisabledJobs(null);
							}
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.IGNORERUNNINGJOBS)) {
							try {
								finalResults.setIgnoreRunningJobs(getBoolean(currentNodeResults));
							} catch (Exception ex) {
								finalResults.setIgnoreDisabledJobs(null);
							}
						} else if (currentNodeResults.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.COMPAREWITHPREVIOUSRUN)) {
							try {
								finalResults.setCompareWithPreviousRun(getBoolean(currentNodeResults));
							} catch (Exception ex) {
								finalResults.setIgnoreDisabledJobs(null);
							}
						}
					}
				}
			}
		}
	}
	
	private void readJobs(Aggregated finalResults, Node jobs) {
		if (jobs != null) {
			List<Job> dataJobs = new ArrayList<>();
			List<Data> data = new ArrayList<>();
			data.add(new Data("", dataJobs));
			finalResults.setData(data);
			for (int i = 0; i < jobs.getChildNodes().getLength(); i++) {
				Node currentNodeResults = jobs.getChildNodes().item(i);
				if (XMLReporter.JOB.equalsIgnoreCase(currentNodeResults.getNodeName())) {
					Job dataJob = new Job("", "");
					dataJobs.add(dataJob);
					dataJob.setJob(new JobWithDetailsAggregator());
					dataJob.setResults(new Results());
					for (int j = 0; j < currentNodeResults.getChildNodes().getLength(); j++) {
						Node nodeItem = currentNodeResults.getChildNodes().item(j);
						if (!nodeItem.getNodeName().startsWith("#")) {
							if (nodeItem.getNodeName().equalsIgnoreCase(XMLReporter.NAME)) {
								dataJob.setJobName(getString(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(XMLReporter.STATUS)) {
								dataJob.getResults().setStatus(JobStatus.getFromString(getString(nodeItem)));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(XMLReporter.STATUS_ADV)) {
								dataJob.getResults().setStatusAdvanced(getString(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(XMLReporter.DURATION)) {
								dataJob.getResults().setDuration(getLong(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(XMLReporter.IS_RUNNING)) {
								dataJob.getResults().setBuilding(getBoolean(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(XMLReporter.BUILD)) {
								try {
									dataJob.getResults().setNumber(Integer.parseInt(getString(nodeItem)));
								} catch (Exception ex) {
									
								}
							} else if (nodeItem.getNodeName().equalsIgnoreCase(XMLReporter.URL)) {
								try {
									dataJob.setUrl(getString(nodeItem));
									dataJob.getResults().setUrl(getString(nodeItem));
								} catch (Exception ex) {
									
								}
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SUCCESS)) {
								dataJob.getResults().setPass(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SUCCESS_DIF)) {
								dataJob.getResults().setPassDif(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SKIPPED)) {
								dataJob.getResults().setSkip(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_SKIPPED_DIF)) {
								dataJob.getResults().setSkipDif(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_FAILED)) {
								dataJob.getResults().setFail(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_FAILED_DIF)) {
								dataJob.getResults().setFailDif(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_TOTAL)) {
								dataJob.getResults().setTotal(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.TEST_TOTAL_DIF)) {
								dataJob.getResults().setTotalDif(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.CC_PACKAGES)) {
								dataJob.getResults().setCcPackages(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.CC_FILES)) {
								dataJob.getResults().setCcFiles(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.CC_CLASSES)) {
								dataJob.getResults().setCcClasses(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.CC_METHODS)) {
								dataJob.getResults().setCcMethods(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.CC_LINES)) {
								dataJob.getResults().setCcLines(getInteger(nodeItem));
							} else if (nodeItem.getNodeName().equalsIgnoreCase(TestResultsAggregatorProjectAction.CC_CONDTITIONALS)) {
								dataJob.getResults().setCcConditions(getInteger(nodeItem));
							}
						}
					}
				}
			}
		}
	}
}
