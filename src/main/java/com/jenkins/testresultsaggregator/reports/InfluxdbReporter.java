package com.jenkins.testresultsaggregator.reports;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;

import com.google.common.base.Strings;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.Data;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.helper.LocalMessages;

public class InfluxdbReporter {
	
	private PrintStream logger;
	private static InfluxDBClient INFLXUDB_CLIENT;
	
	public InfluxdbReporter(PrintStream logger) {
		this.logger = logger;
	}
	
	public void post(Aggregated aggregated, String url, String token, String bucket, String org) throws IOException, InterruptedException {
		if (!Strings.isNullOrEmpty(url) && !Strings.isNullOrEmpty(token) && !Strings.isNullOrEmpty(bucket) && !Strings.isNullOrEmpty(org)) {
			logger.println(LocalMessages.POST.toString() + " " + LocalMessages.INFLUXDB.toString());
			createClient(url, token);
			for (Data data : aggregated.getData()) {
				for (Job job : data.getJobs()) {
					Instant timeStamp = Instant.now();
					if (!job.getLast().isBuilding()) {
						// Job has already been completed , is not building post the endTime
						timeStamp = Instant.ofEpochMilli(job.getLast().getDuration());
					}
					Point pointJenkinsJob = Point.measurement("Aggregator")
							.time(timeStamp, WritePrecision.S)
							.addTag("identifier", job.getJobName() + "#" + job.getLast().getNumber())
							.addTag("jobName", job.getJobName())
							.addTag("name", job.getJobNameFromFriendlyName())
							.addTag("url", job.getUrl())
							.addTag("group", data.getGroupName())
							.addTag("status", job.getResults().getStatus())
							.addTag("estimatedDuration", "" + job.getJob().getLastBuild().getQueueId())
							.addTag("status", job.getResults().getStatus())
							.addTag("testTotal", Integer.toString(job.getLast().getResults().getTotal()))
							.addTag("testPass", Integer.toString(job.getLast().getResults().getPass()))
							.addTag("testFail", Integer.toString(job.getLast().getResults().getFail()))
							.addTag("testSkip", Integer.toString(job.getLast().getResults().getSkip()))
							.addField("result", job.getResults().getStatus());
					send(pointJenkinsJob, bucket, org);
					Thread.sleep(200);
				}
			}
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
	
	public static void send(final Point point, String bucket, String org) {
		try (WriteApi writeApi = INFLXUDB_CLIENT.makeWriteApi(WriteOptions.builder()
				.flushInterval(3000)
				.bufferLimit(10000)
				.maxRetries(3)
				.retryInterval(1000)
				.batchSize(30)
				.maxRetryDelay(500)
				.maxRetryTime(5000)
				.build())) {
			writeApi.writePoint(bucket, org, point);
		}
	}
}
