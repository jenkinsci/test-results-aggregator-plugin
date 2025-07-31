package com.jenkins.testresultsaggregator.data;

public enum JobStatus {
	
	ABORTED ("1"),
	FAILURE ("2"),
	STILL_FAILING ("3"),
	UNSTABLE ("4"),
	STILL_UNSTABLE ("5"),
	RUNNING ("6"),
	FIXED ("7"),
	SUCCESS ("8"),
	DISABLED ("9"),
	NOT_FOUND ("10"),
	NO_LAST_BUILD_DATA ("11"),
	RUNNING_REPORT_PREVIOUS ("12"),
	FOUND ("13");
	
	private String myLocator;
	
	private JobStatus(String locator) {
		myLocator = locator;
	}
	
	public int getPriority() {
		return Integer.parseInt(myLocator);
	}
	
	public static JobStatus getFromString(String text) {
		if (text.endsWith("*")) {
			text = text.replace("*", "");
		}
		return JobStatus.valueOf(text);
	}
}
