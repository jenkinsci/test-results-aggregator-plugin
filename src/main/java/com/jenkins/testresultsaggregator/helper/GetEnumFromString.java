package com.jenkins.testresultsaggregator.helper;

public class GetEnumFromString {
	
	public GetEnumFromString() {
		
	}
	
	public static <T extends Enum<T>> T get(Class<T> c, String string) {
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.trim());
			} catch (Exception ex) {
			}
		}
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.toLowerCase().trim());
			} catch (Exception ex) {
			}
		}
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.toUpperCase().trim());
			} catch (Exception ex) {
			}
		}
		return null;
	}
}
