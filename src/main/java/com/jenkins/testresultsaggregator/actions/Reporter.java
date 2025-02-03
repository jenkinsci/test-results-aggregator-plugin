package com.jenkins.testresultsaggregator.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.TestResultsAggregator.AggregatorProperties;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.ImagesMap;
import com.jenkins.testresultsaggregator.data.ImagesMap.ImageData;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.jenkins.testresultsaggregator.helper.LocalMessages;
import com.jenkins.testresultsaggregator.reports.HTMLReporter;
import com.jenkins.testresultsaggregator.reports.InfluxdbReporter;
import com.jenkins.testresultsaggregator.reports.MailNotification;
import com.jenkins.testresultsaggregator.reports.XMLReporter;

import hudson.FilePath;

public class Reporter {
	
	private PrintStream logger;
	private FilePath workspace;
	private File rootDir;
	private String mailNotificationFrom;
	private Boolean ignoreNotFoundJobs;
	private Boolean ignoreDisabledJobs;
	private Boolean ignoreAbortedJobs;
	private boolean foundAtLeastOneGroupName;
	
	private Set<Job> ignoredDataJobs = new HashSet<>();
	
	public Reporter(PrintStream logger, FilePath workspace, File rootDir, String mailNotificationFrom, Boolean ignoreDisabledJobs, Boolean ignoreNotFoundJobs, Boolean ignoreAbortedJobs) {
		this.logger = logger;
		this.workspace = workspace;
		this.rootDir = rootDir;
		this.mailNotificationFrom = mailNotificationFrom;
		this.ignoreDisabledJobs = ignoreDisabledJobs;
		this.ignoreNotFoundJobs = ignoreNotFoundJobs;
		this.ignoreAbortedJobs = ignoreAbortedJobs;
	}
	
	public void publishResuts(Aggregated aggregated, Properties properties, List<LocalMessages> columns, File rootDirectory) throws IOException, InterruptedException {
		foundAtLeastOneGroupName = false;
		for (Data data : aggregated.getData()) {
			if (!Strings.isNullOrEmpty(data.getGroupName())) {
				foundAtLeastOneGroupName = true;
				break;
			}
		}
		// Generate XML Report
		new XMLReporter(logger, rootDir).generateXMLReport(aggregated, properties);
		// Calculate and Generate Columns
		if (!foundAtLeastOneGroupName) {
			columns.remove(LocalMessages.COLUMN_GROUP);
		}
		Aggregated aggregatedCopy = aggregated.copy();
		if (ignoreDisabledJobs != null && ignoreDisabledJobs) {
			ignoreJobsFromReport(aggregatedCopy.getData(), JobStatus.DISABLED);
		}
		if (ignoreNotFoundJobs != null && ignoreNotFoundJobs) {
			ignoreJobsFromReport(aggregatedCopy.getData(), JobStatus.NOT_FOUND);
		}
		if (ignoreAbortedJobs != null && ignoreAbortedJobs) {
			ignoreJobsFromReport(aggregatedCopy.getData(), JobStatus.ABORTED);
		}
		HTMLReporter htmlReporter = new HTMLReporter(logger, workspace);
		// Generate HTML Reports
		FilePath htmlReport = htmlReporter.createOverview(aggregatedCopy, columns, properties.getProperty(AggregatorProperties.THEME.name()), foundAtLeastOneGroupName);
		FilePath htmlReportIgnoredDataJobs = htmlReporter.createIgnoredData(ignoredDataJobs, properties.getProperty(AggregatorProperties.THEME.name()));
		// Generate Body message
		String bodyText = generateMailBody(htmlReport.read());
		String bodyTextIgnored = generateMailBody(htmlReportIgnoredDataJobs.read());
		// Calculate attachments
		Map<String, ImageData> images = resolveImages(bodyText);
		MailNotification mailNotification = new MailNotification(logger, aggregatedCopy.getData(), workspace, rootDirectory);
		// Generate and Send Mail report
		String subjectText = generateMailSubject(properties.getProperty(AggregatorProperties.SUBJECT_PREFIX.name()), aggregatedCopy);
		try {
			mailNotification.send(
					properties.getProperty(AggregatorProperties.RECIPIENTS_LIST.name()),
					properties.getProperty(AggregatorProperties.RECIPIENTS_LIST_CC.name()),
					properties.getProperty(AggregatorProperties.RECIPIENTS_LIST_BCC.name()),
					mailNotificationFrom,
					subjectText,
					bodyText,
					images,
					properties.getProperty(AggregatorProperties.TEXT_BEFORE_MAIL_BODY.name()),
					properties.getProperty(AggregatorProperties.TEXT_AFTER_MAIL_BODY.name()));
			//
			mailNotification.sendIgnoredData(
					properties.getProperty(AggregatorProperties.RECIPIENTS_LIST_IGNORED.name()),
					mailNotificationFrom,
					"Test Results Aggregator Ignored Jobs",
					bodyTextIgnored,
					properties.getProperty(AggregatorProperties.TEXT_BEFORE_MAIL_BODY.name()),
					properties.getProperty(AggregatorProperties.TEXT_AFTER_MAIL_BODY.name()));
		} catch (Exception ex) {
			throw new IOException(ex);
		}
		new InfluxdbReporter(logger).post(aggregatedCopy,
				properties.getProperty(AggregatorProperties.INFLUXDB_URL.name()),
				properties.getProperty(AggregatorProperties.INFLUXDB_TOKEN.name()),
				properties.getProperty(AggregatorProperties.INFLUXDB_BUCKET.name()),
				properties.getProperty(AggregatorProperties.INFLUXDB_ORG.name()));
	}
	
	private void ignoreJobsFromReport(List<Data> list, JobStatus status) {
		for (Data tempData : list) {
			List<Job> tempList = new ArrayList<>();
			for (Job temp : tempData.getJobs()) {
				if (temp.getResults() != null && !status.name().equalsIgnoreCase(temp.getResults().getStatus())) {
					tempList.add(temp);
				} else if (temp.getLast() != null && temp.getLast().isBuilding()) {
					tempList.add(temp);
				} else if (temp.getLast() != null && !status.name().equalsIgnoreCase(temp.getLast().getResults().getStatus())) {
					tempList.add(temp);
				} else {
					ignoredDataJobs.add(temp);
				}
			}
			tempData.setJobs(tempList);
		}
	}
	
	private String generateMailBody(InputStream inputStream) throws IOException {
		BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		buf.close();
		String body = sb.toString();
		// Fix Images
		Set<String> setImageID = ImagesMap.getImages().keySet();
		for (String contentId : setImageID) {
			body = body.replaceAll(ImagesMap.getImages().get(contentId).getFileName(), ImagesMap.getImages().get(contentId).getCid());
		}
		return body;
	}
	
	private Map<String, ImageData> resolveImages(String bodyText) {
		Map<String, ImageData> images = new HashMap<>();
		Set<String> setImageID = ImagesMap.getImages().keySet();
		for (String contentId : setImageID) {
			if (bodyText.contains(ImagesMap.getImages().get(contentId).getCid())) {
				images.put(contentId, ImagesMap.getImages().get(contentId));
			}
		}
		return images;
	}
	
	private String generateMailSubject(String subjectPrefix, Aggregated aggregated) {
		String subject = subjectPrefix;
		if (aggregated.getSuccessJobs() > 0 || aggregated.getFixedJobs() > 0) {
			subject += " " + LocalMessages.RESULTS_SUCCESS.toString() + " : " + (aggregated.getSuccessJobs() + aggregated.getFixedJobs());
		}
		if (aggregated.getFailedJobs() > 0 || aggregated.getKeepFailJobs() > 0) {
			subject += " " + LocalMessages.RESULTS_FAILED.toString() + " : " + (aggregated.getFailedJobs() + aggregated.getKeepFailJobs());
		}
		if (aggregated.getUnstableJobs() > 0 || aggregated.getKeepUnstableJobs() > 0) {
			subject += " " + LocalMessages.RESULTS_UNSTABLE.toString() + " : " + (aggregated.getUnstableJobs() + aggregated.getKeepUnstableJobs());
		}
		if (aggregated.getAbortedJobs() > 0 && !ignoreAbortedJobs) {
			subject += " " + LocalMessages.RESULTS_ABORTED.toString() + " : " + aggregated.getAbortedJobs();
		}
		if (aggregated.getRunningJobs() > 0) {
			subject += " " + LocalMessages.RESULTS_RUNNING.toString() + " : " + aggregated.getRunningJobs();
		}
		return subject;
	}
	
}
