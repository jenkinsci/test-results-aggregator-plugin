package com.jenkins.testresultsaggregator.helper;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import com.google.common.base.Strings;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.data.JobResults;
import com.jenkins.testresultsaggregator.data.JobStatus;
import com.jenkins.testresultsaggregator.data.Results;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

public class Helper {
	
	public static String encodeValue(String value) throws UnsupportedEncodingException, MalformedURLException {
		return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20").trim();
	}
	
	public static String getTimeStamp(Long timeStamp) {
		if (timeStamp == null) {
			return "";
		} else {
			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), TimeZone.getDefault().toZoneId());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss:SSS");
			return date.format(formatter);
		}
	}
	
	public static String getTimeStamp(String outOfDateResults, Long timeStamp) {
		if (timeStamp == null) {
			return "";
		} else {
			int outOfDate = Integer.parseInt(outOfDateResults) * 3600;
			LocalDateTime today = LocalDateTime.now();
			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), TimeZone.getDefault().toZoneId());
			Duration d = Duration.between(date, today);
			long currentHours = d.getSeconds() / 3600;
			long currentMin = d.getSeconds() / 60;
			long bDours = currentHours % 24;
			long bDays = currentHours / 24;
			if (d.getSeconds() > outOfDate) {
				if (bDays > 0) {
					if (bDays >= 2) {
						return colorize(bDays + "d" + " ago", Colors.FAILED);
					} else {
						return colorize(bDays + "d:" + bDours + "h ago", Colors.FAILED);
					}
				} else {
					return colorize(bDours + "h ago", Colors.FAILED);
				}
			}
			if (bDays > 0) {
				if (bDays == 1) {
					return bDays + "d:" + bDours + "h ago";
				}
				return bDays + "d:" + bDours + "h ago";
			} else if (bDours == 0) {
				return currentMin + "m ago";
			} else {
				return currentHours + "h ago";
			}
		}
	}
	
	public static String getNumber(int value) {
		if (value < 0) {
			return Integer.toString(value);
		} else if (value > 0) {
			return Integer.toString(value);
		} else {
			return "";
		}
	}
	
	public static String urlNumberofChanges(String url, String number) {
		if (!number.isEmpty()) {
			return "<a href = '" + url + "'>" + number + "</a>";
		}
		return "";
	}
	
	public static String colorizeResultStatus(String result) {
		if (result != null) {
			if (result.startsWith(JobStatus.SUCCESS.name())) {
				return colorize(result, Colors.SUCCESS);
			} else if (result.startsWith(JobStatus.FAILURE.name())) {
				return colorize(result, Colors.FAILED);
			} else if (result.startsWith(JobStatus.STILL_FAILING.name())) {
				return colorize(result, Colors.FAILED);
			} else if (result.startsWith(JobStatus.FIXED.name())) {
				return colorize(result, Colors.SUCCESS);
			} else if (result.startsWith(JobStatus.UNSTABLE.name())) {
				return colorize(result, Colors.UNSTABLE);
			} else if (result.startsWith(JobStatus.ABORTED.name())) {
				return colorize(result, Colors.ABORTED);
			} else if (result.startsWith(JobStatus.STILL_UNSTABLE.name())) {
				return colorize(result, Colors.UNSTABLE);
			} else if (result.startsWith(JobStatus.RUNNING.name())) {
				return colorize(result, Colors.RUNNING);
			}
		}
		
		return result;
	}
	
	public static Double countPercentage(Results results) {
		String percentage;
		if (results != null && results.getTotal() > 0) {
			try {
				percentage = singDoubleSingle((double) (results.getPass() + results.getSkip()) * 100 / results.getTotal());
				if (percentage.equals("100")) {
					return 100D;
				} else if (!Strings.isNullOrEmpty(percentage)) {
					percentage = percentage.replace(",", ".");
				}
				return Double.valueOf(percentage);
			} catch (Exception ex) {
				
			}
		}
		return -1D;
	}
	
	public static Double countPercentage(JobResults results) {
		String percentage;
		if (results != null && results.getTotal() != 0) {
			try {
				percentage = singDoubleSingle((double) (results.getPass() + results.getSkip()) * 100 / results.getTotal());
				if (percentage.equals("100")) {
					return 100D;
				} else if (!Strings.isNullOrEmpty(percentage)) {
					percentage = percentage.replace(",", ".");
				}
				return Double.valueOf(percentage);
			} catch (Exception ex) {
				
			}
		}
		return -1D;
	}
	
	public static String countPercentage(int pass, int total) {
		return countPercentageD(pass, total).toString();
	}
	
	public static Double countPercentageD(int pass, int total) {
		Results results = new Results();
		results.setPass(pass);
		results.setSkip(0);
		results.setTotal(total);
		return countPercentage(results);
	}
	
	public static String colorizePercentage(Double percentageDouble, Integer fontSize, String jobStatus) {
		Color color = null;
		String percentageString = "";
		if (JobStatus.RUNNING.toString().equalsIgnoreCase(jobStatus)) {
			color = Colors.RUNNING;
		}
		if (percentageDouble >= 100) {
			percentageString = "100";
		} else if (percentageDouble == 0) {
			percentageString = "0";
		} else {
			percentageString = percentageDouble.toString();
		}
		if (color == null) {
			if (percentageDouble == 100) {
				color = Colors.SUCCESS;
			} else if (percentageDouble >= 95) {
				color = Colors.UNSTABLE;
			} else {
				color = Colors.FAILED;
			}
		}
		return colorize(percentageString + "%", color, fontSize);
	}
	
	public static String singDoubleSingle(double value) {
		if (value == 0) {
			return "0";
		} else {
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.DOWN);
			if (Math.abs(value) < 0.005) {
				return "";
			} else if (Math.abs(value) == 0) {
				return "";
			} else if (value < 0.00 || value > 0) {
				return df.format(value);
			} else {
				return "";
			}
		}
	}
	
	public static FilePath createFolder(FilePath filePath, String folder, boolean delete) throws IOException, InterruptedException {
		FilePath fp;
		if (filePath.isRemote()) {
			VirtualChannel channel = filePath.getChannel();
			if (filePath.child(folder).exists() && delete) {
				filePath.child(folder).deleteRecursive();
			}
			fp = new FilePath(channel, filePath.child(folder).getRemote());
		} else {
			if (filePath.child(folder).exists() && delete) {
				filePath.child(folder).deleteRecursive();
			}
			fp = new FilePath(new File(filePath.getRemote(), folder));
		}
		fp.mkdirs();
		return fp;
	}
	
	public static FilePath createFile(FilePath filePath, String filename) throws IOException, InterruptedException {
		FilePath fp;
		if (filePath.isRemote()) {
			VirtualChannel channel = filePath.getChannel();
			fp = new FilePath(channel, filePath.getRemote() + File.separator + filename);
		} else {
			fp = new FilePath(new File(filePath.getRemote() + File.separator + filename));
		}
		return fp;
	}
	
	public static String reportTestDiffs(String status, Color color, long curr, long diff) {
		boolean still = false;
		if (status != null && status.startsWith("STILL")) {
			still = true;
		}
		if (diff == 0 && curr == 0) {
			return "";
		} else if (diff == 0 && curr > 0) {
			if (still) {
				color = null;
			}
			return colorize2(curr, color);
		} else {
			if (curr == 0) {
				if (diff > 0) {
					return "+" + colorize2(diff, color);
				} else {
					return "" + diff;
				}
			} else {
				if (diff > 0 && diff == curr) {
					if (still) {
						color = null;
					}
					return colorize2(curr, color);
				} else if (diff > 0 && diff != curr) {
					return curr + "(+" + colorize2(diff, color) + ")";
				} else {
					return curr + "(" + diff + ")";
				}
				
			}
		}
	}
	
	public static String diff(long prev, long curr, boolean list) {
		return diff(prev, curr, null, list);
	}
	
	public static String diff(long prev, long curr, String name) {
		return diff(prev, curr, name, null, false, false);
	}
	
	public static String diff(long prev, long curr, String name, boolean list) {
		return diff(prev, curr, name, null, list, false);
	}
	
	public static String diff(long prev, long curr, String name, Color color, boolean list, boolean percentage) {
		String namePrefix = null;
		String text = null;
		if (Strings.isNullOrEmpty(name)) {
			// Empty name
			name = "";
		}
		if (!Strings.isNullOrEmpty(name)) {
			namePrefix = name + ": ";
		} else {
			namePrefix = name;
		}
		if (color != null) {
			text = colorize(namePrefix, color);
		} else {
			text = namePrefix;
		}
		String percentageIcon = "";
		if (percentage) {
			percentageIcon = "%";
		}
		if (list) {
			if (prev == curr) {
				return "<li>" + text + curr + "</li>";
			} else if (prev < curr) {
				return "<li>" + text + curr + colorize("(+" + (curr - prev) + ")", Colors.BLACK) + "</li>";
			} else { // if (a < b)
				return "<li>" + text + curr + colorize("(-" + (prev - curr) + ")", Colors.BLACK) + "</li>";
			}
		} else {
			if (prev == curr) {
				if (curr == 0) {
					return "";
				} else {
					return text + colorize(curr, Colors.BLACK) + percentageIcon;
				}
			} else if (prev < curr) {
				if (curr == 0) {
					return text + colorize("+" + (curr - prev), Colors.BLACK) + percentageIcon;
				} else if (prev == 0) {
					return text + colorize(curr, color) + percentageIcon;
				} else {
					return text + colorize(curr, color) + colorize("(+" + (curr - prev) + ")", Colors.BLACK) + percentageIcon;
				}
			} else { // if (a < b)
				if (curr == 0) {
					return text + colorize("-" + (prev - curr), Colors.BLACK) + percentageIcon;
				} else {
					return text + colorize(curr, color) + colorize("(-" + (prev - curr) + ")", Colors.BLACK) + percentageIcon;
				}
			}
		}
	}
	
	public static String colorize(String text, Color color) {
		return colorize(text, color, null);
	}
	
	public static String colorize(String text, Color color, Integer font) {
		if (color == null) {
			color = Colors.BLACK;
		}
		if (font != null) {
			if (!Strings.isNullOrEmpty(text)) {
				return "<font style='font-size: " + font + "px; color:" + Colors.html(color) + "'>" + text + "</font>";
			}
		} else {
			if (!Strings.isNullOrEmpty(text)) {
				return "<font style='color:" + Colors.html(color) + "'>" + text + "</font>";
			}
		}
		return text;
	}
	
	public static String colorize(Long text, Color color) {
		if (color == null || text == 0) {
			color = Colors.BLACK;
		}
		return "<font color='" + Colors.html(color) + "'>" + text + "</font>";
	}
	
	public static String colorize2(Long text, Color color) {
		if (color == null || text == 0) {
			return Long.toString(text);
		}
		return "<font color='" + Colors.html(color) + "'>" + text + "</font>";
	}
	
	public static String duration(Long millis) {
		Duration duration = Duration.of(millis, ChronoUnit.MILLIS);
		long durationInSeconds = duration.getSeconds();
		long hours = durationInSeconds / 3600;
		long minutes = (durationInSeconds % 3600) / 60;
		String hoursString = "";
		String minString = "";
		if (hours < 10) {
			hoursString = "0" + hours;
		} else {
			hoursString = Long.toString(hours);
		}
		if (minutes < 10) {
			minString = "0" + minutes;
		} else {
			minString = Long.toString(minutes);
		}
		if (hours == 0 && minutes == 0) {
			return null;
		} else if (hours == 0) {
			return "00:" + minString;
		} else {
			return hoursString + ":" + minString;
		}
	}
	
	public static String calculateStatusJob(Job job) {
		boolean reportPrevious = false;
		if (job.getResults() != null && JobStatus.RUNNING_REPORT_PREVIOUS.name().equalsIgnoreCase(job.getResults().getStatus())) {
			reportPrevious = true;
		} else if (job.getPrevious() == null || (job.getPrevious() != null && job.getPrevious().getResults() == null)) {
			return calculateStatus(reportPrevious, job.getLast().getResults().getStatus(), null);
		}
		return calculateStatus(reportPrevious, job.getLast().getResults().getStatus(), job.getPrevious().getResults().getStatus());
	}
	
	public static String calculateStatus(boolean reportPrevious, String currentResult, String previousResult) {
		if (previousResult == null) {
			return currentResult;
		} else {
			if (reportPrevious) {
				return currentResult + "*";
			} else if (JobStatus.SUCCESS.name().equals(currentResult) && JobStatus.SUCCESS.name().equals(previousResult)) {
				return JobStatus.SUCCESS.name();
			} else if (JobStatus.SUCCESS.name().equals(currentResult) && JobStatus.FAILURE.name().equals(previousResult)) {
				return JobStatus.FIXED.name();
			} else if (JobStatus.SUCCESS.name().equals(currentResult) && JobStatus.UNSTABLE.name().equals(previousResult)) {
				return JobStatus.FIXED.name();
			} else if (JobStatus.UNSTABLE.name().equals(currentResult) && JobStatus.UNSTABLE.name().equals(previousResult)) {
				return JobStatus.STILL_UNSTABLE.name();
			} else if (JobStatus.FAILURE.name().equals(currentResult) && JobStatus.FAILURE.name().equals(previousResult)) {
				return JobStatus.STILL_FAILING.name();
			} else if (currentResult != null && currentResult.contains("*")) {
				return currentResult.replace("*", "");
			} else {
				return currentResult;
			}
		}
	}
	
	public static Double resolvePercentage(String percentage) {
		if (Strings.isNullOrEmpty(percentage)) {
			return -1D;
		} else {
			try {
				Double doublePercentage = Double.valueOf(percentage);
				if (doublePercentage >= 100) {
					return 100D;
				}
				return doublePercentage;
			} catch (NumberFormatException ex) {
			}
		}
		return -1D;
	}
	
}
