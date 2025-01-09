package com.jenkins.testresultsaggregator.reports;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;

import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.data.ImagesMap;
import com.jenkins.testresultsaggregator.data.Job;
import com.jenkins.testresultsaggregator.helper.Colors;
import com.jenkins.testresultsaggregator.helper.Helper;
import com.jenkins.testresultsaggregator.helper.LocalMessages;

import hudson.FilePath;

public class HTMLReporter {
	
	public static final String FOLDER = "html";
	
	private static final String OVERVIEW_HTML_FILE = "index.html";
	private static final String OVERVIEW_JELLY_FILE = "htmlreport.jelly";
	
	private static final String IGNORED_DATA_HTML_FILE = "ignoredData.html";
	private static final String IGNORED_DATA_JELLY_FILE = "ignoredData.jelly";
	
	private PrintStream logger;
	private FilePath workspace;
	
	public HTMLReporter(PrintStream logger, FilePath workspace) {
		this.logger = logger;
		this.workspace = workspace;
	}
	
	public FilePath createOverview(Aggregated aggregated, List<LocalMessages> columns, String theme, boolean showGroups) throws IOException, InterruptedException {
		logger.print(LocalMessages.GENERATE.toString() + " " + LocalMessages.HTML_REPORT.toString());
		FilePath directory = Helper.createFolder(workspace, FOLDER, true);
		FilePath file = Helper.createFile(directory, OVERVIEW_HTML_FILE);
		JellyContext context = new JellyContext();
		// Variables
		context.setVariable("name", "Test Result Aggregator");
		context.setVariable("showGroups", showGroups);
		context.setVariable("columns", columns);
		context.setVariable("aggregated", aggregated);
		// Themes light and dark
		context.setVariable("theme", theme);
		// Header & footer color
		context.setVariable("headerColor", Colors.htmlHEADER());
		context.setVariable("footerColor", Colors.htmlFOOTER());
		context.setVariable("footerTextColor", Colors.htmlFOOTERTEXT());
		// Line Separator color
		context.setVariable("lineSeperatorcolor", Colors.htmlLINESEPERATOR());
		XMLOutput xmlOutput = XMLOutput.createXMLOutput(file.write());
		URL template = HTMLReporter.class.getResource("/" + OVERVIEW_JELLY_FILE);
		JellyContext jellyContext = null;
		try {
			jellyContext = context.runScript(template, xmlOutput);
			xmlOutput.endDocument();
		} catch (JellyException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		xmlOutput.flush();
		xmlOutput.close();
		if (jellyContext != null) {
			jellyContext.clear();
		}
		// Copy Images
		copyImages(directory);
		logger.println(LocalMessages.FINISHED.toString() + " " + LocalMessages.HTML_REPORT.toString());
		return file;
	}
	
	private void copyImages(FilePath directory) throws IOException, InterruptedException {
		Set<String> setImageID = ImagesMap.getImages().keySet();
		for (String contentId : setImageID) {
			copyStream(ImagesMap.getImages().get(contentId).getSourceInPlugin(), ImagesMap.getImages().get(contentId).getFileName(), directory);
		}
	}
	
	protected void copyStream(String sourceFile, String destinationFile, FilePath directory) throws IOException, InterruptedException {
		InputStream inputUrl = HTMLReporter.class.getResource(sourceFile).openStream();
		// Create Destination File
		Helper.createFile(directory, destinationFile).copyFrom(inputUrl);
	}
	
	public FilePath createIgnoredData(Set<Job> ignoredDataJobs, String theme) throws IOException, InterruptedException {
		logger.print(LocalMessages.GENERATE.toString() + " " + LocalMessages.HTML_REPORT.toString() + " " + LocalMessages.IGNORE.toString());
		FilePath directory = Helper.createFolder(workspace, FOLDER, false);
		FilePath file = Helper.createFile(directory, IGNORED_DATA_HTML_FILE);
		JellyContext context = new JellyContext();
		// Variables
		context.setVariable("name", "Test Result Aggregator");
		// Themes light and dark
		context.setVariable("theme", theme);
		context.setVariable("columns", new ArrayList<LocalMessages>(Arrays.asList(LocalMessages.COLUMN_JOB, LocalMessages.COLUMN_JOB_STATUS)));
		context.setVariable("ignoredDataJobs", ignoredDataJobs);
		// Header & footer color
		context.setVariable("headerColor", Colors.htmlHEADER());
		context.setVariable("footerColor", Colors.htmlFOOTER());
		context.setVariable("footerTextColor", Colors.htmlFOOTERTEXT());
		// Line Seperator color
		context.setVariable("lineSeperatorcolor", Colors.htmlLINESEPERATOR());
		XMLOutput xmlOutput = XMLOutput.createXMLOutput(file.write());
		URL template = HTMLReporter.class.getResource("/" + IGNORED_DATA_JELLY_FILE);
		JellyContext jellyContext = null;
		try {
			jellyContext = context.runScript(template, xmlOutput);
			xmlOutput.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		xmlOutput.flush();
		xmlOutput.close();
		if (jellyContext != null) {
			jellyContext.clear();
		}
		logger.println(LocalMessages.FINISHED.toString() + " " + LocalMessages.HTML_REPORT.toString());
		return file;
	}
}
