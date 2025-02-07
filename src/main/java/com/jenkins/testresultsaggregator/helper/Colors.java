package com.jenkins.testresultsaggregator.helper;

import java.awt.Color;

public class Colors {
	
	public static final Color BLACK = Color.BLACK;
	public static final Color SUCCESS = new Color(8, 150, 8);// Color.GREEN;
	public static final Color ABORTED = new Color(115, 115, 115);// Color.DARK_GRAY;
	public static final Color DISABLED = new Color(115, 115, 115);// Color.DARK_GRAY;
	public static final Color FAILED = new Color(245, 15, 25);// Color.RED;
	public static final Color RUNNING = new Color(0, 25, 235);// Color.BLUE;
	public static final Color UNSTABLE = new Color(255, 205, 10); // Color.ORANGE;
	public static final Color HEADER = Color.WHITE;
	public static final Color FOOTER = Color.WHITE;
	public static final Color FOOTER_TEXT = Color.BLACK;
	public static final Color LINE_SEPERATOR = new Color(204, 204, 179);
	public static final Color JOB_NAME_URL = new Color(0, 0, 0);
	
	private static String getHTMLColorString(Color color) {
		String red = Integer.toHexString(color.getRed());
		String green = Integer.toHexString(color.getGreen());
		String blue = Integer.toHexString(color.getBlue());
		return "#" +
				(red.length() == 1 ? "0" + red : red) +
				(green.length() == 1 ? "0" + green : green) +
				(blue.length() == 1 ? "0" + blue : blue);
	}
	
	public static String html(Color color) {
		return getHTMLColorString(color);
	}
	
	public static String htmlBlack() {
		return getHTMLColorString(BLACK);
	}
	
	public static String htmlSUCCESS() {
		return getHTMLColorString(SUCCESS);
	}
	
	public static String htmlABORTED() {
		return getHTMLColorString(ABORTED);
	}
	
	public static String htmlDISABLED() {
		return getHTMLColorString(DISABLED);
	}
	
	public static String htmlFAILED() {
		return getHTMLColorString(FAILED);
	}
	
	public static String htmlRUNNING() {
		return getHTMLColorString(RUNNING);
	}
	
	public static String htmlUNSTABLE() {
		return getHTMLColorString(UNSTABLE);
	}
	
	public static String htmlHEADER() {
		return getHTMLColorString(HEADER);
	}
	
	public static String htmlFOOTER() {
		return getHTMLColorString(FOOTER);
	}
	
	public static String htmlFOOTERTEXT() {
		return getHTMLColorString(FOOTER_TEXT);
	}
	
	public static String htmlJOB_NAME_URL() {
		return getHTMLColorString(JOB_NAME_URL);
	}
	
	public static String htmlLINESEPERATOR() {
		return getHTMLColorString(LINE_SEPERATOR);
	}
}
