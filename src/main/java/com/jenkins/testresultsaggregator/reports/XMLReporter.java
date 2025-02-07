package com.jenkins.testresultsaggregator.reports;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Properties;

import com.jenkins.testresultsaggregator.TestResultsAggregator.AggregatorProperties;
import com.jenkins.testresultsaggregator.TestResultsAggregatorProjectAction;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.jenkins.testresultsaggregator.helper.LocalMessages;

public class XMLReporter {
	public static final String REPORT_XML_FILE = "aggregated.xml";
	private PrintStream logger;
	private File workspace;
	public static final String S = "<";
	public static final String SE = "</";
	public static final String E = ">";
	public static final String TAB = "\t";
	
	public static final String ROOT = "AGGREGATED";
	public static final String RESULTS = "RESULTS";
	public static final String CONFIG = "CONFIG";
	public static final String JOBS = "JOBS";
	public static final String JOB = "JOB";
	public static final String NAME = "NAME";
	public static final String STATUS = "STATUS";
	public static final String STATUS_ADV = "STATUS_ADV";
	public static final String DURATION = "DURATION";
	public static final String URL = "URL";
	public static final String BUILD = "BUILD";
	public static final String IS_RUNNING = "IS_RUNNING";
	
	public XMLReporter(PrintStream logger, File rootDir) {
		this.logger = logger;
		this.workspace = rootDir;
	}
	
	public void generateXMLReport(Aggregated aggregated, Properties properties) {
		try {
			logger.print(LocalMessages.GENERATE.toString() + " " + LocalMessages.XML_REPORT.toString());
			String fileName = workspace.getAbsolutePath() + System.getProperty("file.separator") + REPORT_XML_FILE;
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println(S + ROOT + E);
			// Config
			writer.println(TAB + S + CONFIG + E);
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.IGNOREDISABLEDJOBS, properties.getProperty(AggregatorProperties.IGNORE_DISABLED_JOBS.name())));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.IGNORENOTFOUNDJOBS, properties.getProperty(AggregatorProperties.IGNORE_NOTFOUND_JOBS.name())));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.IGNORERUNNINGJOBS, properties.getProperty(AggregatorProperties.IGNORE_RUNNING_JOBS.name())));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.COMPAREWITHPREVIOUSRUN, properties.getProperty(AggregatorProperties.COMPARE_WITH_PREVIOUS_RUN.name())));
			writer.println(TAB + SE + CONFIG + E);
			// Results
			writer.println(TAB + S + RESULTS + E);
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_SUCCESS, aggregated.getSuccessJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_FIXED, aggregated.getFixedJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_ABORTED, aggregated.getAbortedJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_FAILED, aggregated.getFailedJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_FAILED_KEEP, aggregated.getKeepFailJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_RUNNING, aggregated.getRunningJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_UNSTABLE, aggregated.getUnstableJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_UNSTABLE_KEEP, aggregated.getKeepUnstableJobs()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.JOB_DISABLED, aggregated.getDisabledJobs()));
			
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_TOTAL, aggregated.getResults().getTotal()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_TOTAL_DIF, aggregated.getResults().getTotalDif()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SUCCESS, aggregated.getResults().getPass()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SUCCESS_DIF, aggregated.getResults().getPassDif()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_FAILED, aggregated.getResults().getFail()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_FAILED_DIF, aggregated.getResults().getFailDif()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SKIPPED, aggregated.getResults().getSkip()));
			writer.println(TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SKIPPED_DIF, aggregated.getResults().getSkipDif()));
			writer.println(TAB + SE + RESULTS + E);
			
			writer.println(TAB + S + JOBS + E);
			for (Data data : aggregated.getData()) {
				for (Job dataJob : data.getJobs()) {
					if (dataJob.getJob() != null) {
						writer.println(TAB + TAB + S + JOB + E);
						writer.println(TAB + TAB + TAB + xmlTag(NAME, dataJob.getJobName()));
						if (dataJob.getResults() != null) {
							writer.println(TAB + TAB + TAB + xmlTag(STATUS, dataJob.getResults().getStatus()));
							writer.println(TAB + TAB + TAB + xmlTag(STATUS_ADV, dataJob.getResults().getStatusAdvanced()));
							writer.println(TAB + TAB + TAB + xmlTag(DURATION, dataJob.getResults().getDuration()));
							writer.println(TAB + TAB + TAB + xmlTag(IS_RUNNING, dataJob.getResults().isBuilding()));
							if (JobStatus.DISABLED == dataJob.getResults().getStatus()) {
								jobStatus(writer, dataJob, dataJob.getUrl(), null, false);
							} else if (JobStatus.NOT_FOUND == dataJob.getResults().getStatus()) {
								jobStatus(writer, dataJob, null, null, false);
							} else if (JobStatus.NO_LAST_BUILD_DATA == dataJob.getResults().getStatus()) {
								jobStatus(writer, dataJob, null, null, false);
							} else {
								jobStatus(writer, dataJob, dataJob.getUrl(), dataJob.getResults().getNumber(), true);
							}
						} else {
							jobStatus(writer, dataJob, dataJob.getUrl(), dataJob.getLast().getBuildNumber(), true);
						}
						writer.println(TAB + TAB + SE + JOB + E);
					}
				}
			}
			writer.println(TAB + SE + JOBS + E);
			writer.println(SE + ROOT + E);
			writer.close();
			logger.println(LocalMessages.FINISHED.toString() + " " + LocalMessages.XML_REPORT.toString());
		} catch (IOException e) {
			logger.println("");
			logger.printf(LocalMessages.ERROR_OCCURRED.toString() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private String xmlTag(String tag, Object content) {
		if (content != null) {
			return "<" + tag + ">" + content + "</" + tag + ">";
		}
		return "<" + tag + "></" + tag + ">";
	}
	
	private void jobStatus(PrintWriter writer, Job dataJob, String url, Integer build, boolean found) {
		writer.println(TAB + TAB + TAB + xmlTag(URL, url));
		writer.println(TAB + TAB + TAB + xmlTag(BUILD, build));
		if (found && dataJob.getResults() != null) {
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_TOTAL, dataJob.getResults().getTotal()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_TOTAL_DIF, dataJob.getResults().getTotalDif()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SUCCESS, dataJob.getResults().getPass()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SUCCESS_DIF, dataJob.getResults().getPassDif()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SKIPPED, dataJob.getResults().getSkip()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SKIPPED_DIF, dataJob.getResults().getSkipDif()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_FAILED, dataJob.getResults().getFail()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_FAILED_DIF, dataJob.getResults().getFailDif()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_PACKAGES, dataJob.getResults().getCcPackages()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_FILES, dataJob.getResults().getCcFiles()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_CLASSES, dataJob.getResults().getCcClasses()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_METHODS, dataJob.getResults().getCcMethods()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_LINES, dataJob.getResults().getCcLines()));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_CONDTITIONALS, dataJob.getResults().getCcConditions()));
		} else {
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_TOTAL, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SUCCESS, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_SKIPPED, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.TEST_FAILED, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_PACKAGES, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_FILES, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_CLASSES, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_METHODS, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_LINES, 0));
			writer.println(TAB + TAB + TAB + xmlTag(TestResultsAggregatorProjectAction.CC_CONDTITIONALS, 0));
		}
	}
}
