package com.jenkins.testresultsaggregator.reports;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import com.google.common.base.Strings;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.exceptions.UnprocessableEntityException;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.jenkins.testresultsaggregator.helper.LocalMessages;

public class InfluxdbReporter {
	
	private PrintStream logger;
	private static InfluxDBClient INFLXUDB_CLIENT;
	private static WriteApi writeApi;
	private static ArrayList<StringBuffer> errorPosting = new ArrayList<>();
	
	public InfluxdbReporter(PrintStream logger) {
		this.logger = logger;
	}
	
	public void post(Aggregated aggregated, String url, String token, String bucket, String org, boolean configurationChanges) throws IOException, InterruptedException {
		if (!Strings.isNullOrEmpty(url) && !Strings.isNullOrEmpty(token) && !Strings.isNullOrEmpty(bucket) && !Strings.isNullOrEmpty(org)) {
			logger.println(LocalMessages.POST.toString() + " " + LocalMessages.INFLUXDB.toString());
			createClient(url, token);
			Instant timeNow = Instant.now();
			//
			for (Data data : aggregated.getData()) {
				for (Job job : data.getJobs()) {
					// Post Job data
					if (job.getLast() != null) {
						Instant time = Instant.ofEpochMilli(job.getLast().getTimestamp());
						Point jobData = Point.measurement(job.getJobName() + "#" + job.getLast().getBuildNumber())
								.time(time, WritePrecision.S)
								.addTag("Jenkins Job Name", job.getJobName())
								.addTag("Build", Integer.toString(job.getLast().getBuildNumber()))
								.addTag("Name", job.getJobNameFromFriendlyName())
								.addTag("Url", job.getUrl())
								.addTag("Group", data.getGroupName())
								.addTag("Status", job.getResults().getStatusAdvanced())
								.addTag("Total Tests", Integer.toString(job.getResults().getTotal()))
								.addTag("Pass", Integer.toString(job.getResults().getPass()))
								.addTag("Fail", Integer.toString(job.getResults().getFail()))
								.addTag("Skip", Integer.toString(job.getResults().getSkip()))
								.addTag("Duration", Long.toString(job.getResults().getDuration()))
								.addTag("Last_Update", timeNow.toString())
								.addField("Result", job.getResults().getStatusAdvanced());
						if (JobStatus.RUNNING.name().equalsIgnoreCase(job.getResults().getStatusAdvanced()) || job.getResults().getStatusAdvanced().contains("*")) {
							Instant jobTimeStamp = Instant.ofEpochMilli(job.getLast().getTimestamp());
							Instant expectedToFinish = jobTimeStamp.plusMillis(job.getLast().getEstimatedDuration());
							long diffInMillis = ChronoUnit.MILLIS.between(timeNow, expectedToFinish);
							if (diffInMillis > 0) {
								jobData.addTag("EstimatedDuration", Long.toString(diffInMillis));
							} else {
								jobData.addTag("EstimatedDuration", "0");
							}
						} else {
							jobData.addTag("EstimatedDuration", "0");
						}
						send(jobData, bucket, org, errorPosting);
					} else {
						logger.println("Jenkins Job Name" + job.getJobName() + " has no last build data");
					}
					Thread.sleep(200);
				}
			}
			for (Data data : aggregated.getData()) {
				// Post group data
				if (!Strings.isNullOrEmpty(data.getGroupName())) {
					Double jobPercentage = data.getReportGroup().getPercentageForJobs();
					Double testPercentage = data.getReportGroup().getPercentageForTests();
					Point groupData = Point.measurement("GroupData")
							.time(timeNow, WritePrecision.S)
							.addTag("GroupName", data.getGroupName())
							.addTag("GroupStatus", data.getReportGroup().getStatus())
							.addField("Result", data.getReportGroup().getStatus());
					if (jobPercentage > 0) {
						groupData.addTag("GroupJobPercentage", jobPercentage.toString());
					}
					if (testPercentage > 0) {
						groupData.addTag("GroupTestPercentage", testPercentage.toString());
					}
					if (data.getReportGroup().getJobRunning() > 0 || data.getReportGroup().getJobRunningReportPrevious() > 0) {
						groupData.addTag("Running", "blue");
					}
					send(groupData, bucket, org, errorPosting);
				}
			}
			// Post last update date time
			Point pointJenkinsJob = Point.measurement("TestResultsAggregator")
					.time(timeNow, WritePrecision.S)
					.addField("Last_Update", timeNow.toString());
			send(pointJenkinsJob, bucket, org, errorPosting);
			/*if (!Strings.isNullOrEmpty(errorPosting.toString())) {
			logger.println("ERROR " + errorPosting.toString());
			}*/
			logger.println(LocalMessages.FINISHED.toString() + " " + LocalMessages.INFLUXDB.toString());
		}
	}
	
	private void createClient(String url, String token) throws IOException, InterruptedException {
		INFLXUDB_CLIENT = InfluxDBClientFactory.create(url, token.toCharArray());
		Boolean ping = INFLXUDB_CLIENT.ping();
		int retries = 1;
		while (retries < 4 && (ping == null || !ping)) {
			logger.println("Retry to get Grafana connection " + retries);
			Thread.sleep(2000);
			ping = INFLXUDB_CLIENT.ping();
			retries++;
		}
		if (ping == null || !ping) {
			throw new IOException("Status from " + url + " is ");
		}
	}
	
	public static void send(Point point, String bucket, String org, ArrayList<StringBuffer> errorPosting) {
		if (writeApi == null) {
			WriteOptions options = WriteOptions.builder()
					.flushInterval(3000)
					.bufferLimit(10000)
					.maxRetries(3)
					.retryInterval(1000)
					.batchSize(30)
					.maxRetryDelay(500)
					.maxRetryTime(5000)
					.build();
			writeApi = INFLXUDB_CLIENT.makeWriteApi(options);
			writeApi.listenEvents(WriteErrorEvent.class, event -> {
				if (event.getThrowable() instanceof UnprocessableEntityException) {
					errorPosting.add(new StringBuffer().append("Points are beyond retention policy, check InfuxDB configuration"));
				}
			});
		}
		writeApi.writePoint(bucket, org, point);
	}
}
