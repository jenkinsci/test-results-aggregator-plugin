package com.jenkins.testresultsaggregator;

import java.io.IOException;
import java.util.Calendar;
import java.util.SortedMap;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.jenkins.testresultsaggregator.helper.GraphHelper;

import hudson.Functions;
import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import jenkins.model.lazy.LazyBuildMixIn;
import jenkins.model.lazy.LazyBuildMixIn.LazyLoadingJob;

/**
 * Action to associate the TestNG reports with the project
 */
public class TestResultsAggregatorProjectAction extends TestResultProjectAction implements ProminentProjectAction {
	
	// For Graphs
	public static final String SUCCESS = "Success";
	public static final String FIXED = "Fixed";
	public static final String FAILED = "Failed";
	public static final String FAILED_KEEP = "KeepFailling";
	public static final String UNSTABLE = "Unstable";
	public static final String UNSTABLE_KEEP = "KeepUnstable";
	public static final String ABORTED = "Aborted";
	public static final String RUNNING = "Running";
	
	// For Jobs
	public static final String JOB_SUCCESS = "SUCCESS";
	public static final String JOB_FIXED = "FIXED";
	public static final String JOB_FAILED = "FAILED";
	public static final String JOB_FAILED_KEEP = "KEEPFAILLING";
	public static final String JOB_UNSTABLE = "UNSTABLE";
	public static final String JOB_UNSTABLE_KEEP = "KEEPUNSTABLE";
	public static final String JOB_ABORTED = "ABORTED";
	public static final String JOB_RUNNING = "RUNNING";
	public static final String JOB_TOTAL = "TOTAL";
	
	// For Tests
	public static final String TEST_TOTAL = "TEST_TOTAL";
	public static final String TEST_TOTAL_DIF = "TEST_TOTAL_DIF";
	public static final String TEST_SUCCESS = "TEST_SUCCESS";
	public static final String TEST_SUCCESS_DIF = "TEST_SUCCESS_DIF";
	public static final String TEST_SKIPPED = "TEST_SKIPPED";
	public static final String TEST_SKIPPED_DIF = "TEST_SKIPPED_DIF";
	public static final String TEST_FAILED = "TEST_FAILED";
	public static final String TEST_FAILED_DIF = "TEST_FAILED_DIF";
	
	// For Code Coverage
	public static final String CC_PACKAGES = "CC_PACKAGES";
	public static final String CC_FILES = "CC_FILES";
	public static final String CC_CLASSES = "CC_CLASSES";
	public static final String CC_METHODS = "CC_METHODS";
	public static final String CC_LINES = "CC_LINES";
	public static final String CC_CONDTITIONALS = "CC_CONDITIONALS";
	
	// For Configuration
	public static final String IGNOREDISABLEDJOBS = "IGNOREDISABLEDJOBS";
	public static final String IGNORENOTFOUNDJOBS = "IGNORENOTFOUNDJOBS";
	public static final String IGNORERUNNINGJOBS = "IGNORERUNNINGJOBS";
	public static final String COMPAREWITHPREVIOUSRUN = "COMPAREWITHPREVIOUSRUN";
	
	public TestResultsAggregatorProjectAction(Job<?, ?> project) {
		super(project);
	}
	
	protected Class<TestResultsAggregatorTestResultBuildAction> getBuildActionClass() {
		return TestResultsAggregatorTestResultBuildAction.class;
	}
	
	public Job<?, ?> getProject() {
		return super.job;
	}
	
	public String getIconFileName() {
		return TestResultsAggregator.ICON_FILE_NAME;
	}
	
	public String getDisplayName() {
		return TestResultsAggregator.DISPLAY_NAME;
	}
	
	public String getGraphNameJobs() {
		return TestResultsAggregator.GRAPH_NAME_JOBS;
	}
	
	public String getGraphNameTests() {
		return TestResultsAggregator.GRAPH_NAME_TESTS;
	}
	
	public String getUrlName() {
		return TestResultsAggregator.URL;
	}
	
	public String getSearchUrl() {
		return TestResultsAggregator.URL;
	}
	
	public void doGraphJob(final StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (newGraphNotNeeded(req, rsp)) {
			return;
		}
		final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
		populateDataSetBuilderJobs(dataSetBuilder);
		new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
			protected JFreeChart createGraph() {
				return GraphHelper.createChartJob(req, dataSetBuilder.build());
			}
		}.doPng(req, rsp);
	}
	
	public void doGraphTests(final StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (newGraphNotNeeded(req, rsp)) {
			return;
		}
		final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
		populateDataSetBuilderTest(dataSetBuilder);
		new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
			protected JFreeChart createGraph() {
				return GraphHelper.createChartTests(req,
						dataSetBuilder.build());
			}
		}.doPng(req, rsp);
	}
	
	public String getUpUrl() {
		return Functions.getNearestAncestorUrl(Stapler.getCurrentRequest(), job) + '/';
	}
	
	private boolean newGraphNotNeeded(final StaplerRequest req, StaplerResponse rsp) {
		Calendar t = getProject().getLastCompletedBuild().getTimestamp();
		return req.checkIfModified(t, rsp);
	}
	
	public void doGraphMapJob(final StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (newGraphNotNeeded(req, rsp)) {
			return;
		}
		final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
		populateDataSetBuilderJobs(dataSetBuilder);
		new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
			protected JFreeChart createGraph() {
				return GraphHelper.createChartJob(req, dataSetBuilder.build());
			}
		}.doMap(req, rsp);
	}
	
	public void doGraphMapTests(final StaplerRequest req, StaplerResponse rsp) throws IOException {
		if (newGraphNotNeeded(req, rsp)) {
			return;
		}
		final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
		populateDataSetBuilderTest(dataSetBuilder);
		new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
			protected JFreeChart createGraph() {
				return GraphHelper.createChartTests(req, dataSetBuilder.build());
			}
		}.doMap(req, rsp);
	}
	
	public boolean isGraphActive() {
		Run<?, ?> build = getProject().getLastBuild();
		// in order to have a graph, we must have at least two points.
		int numPoints = 0;
		while (numPoints < 2) {
			if (build == null) {
				return false;
			}
			if (build.getAction(getBuildActionClass()) != null) {
				numPoints++;
			}
			build = build.getPreviousBuild();
		}
		return true;
	}
	
	public TestResultsAggregatorTestResultBuildAction getLastCompletedBuildAction() {
		for (Run<?, ?> build = getProject().getLastCompletedBuild(); build != null; build = build.getPreviousCompletedBuild()) {
			final TestResultsAggregatorTestResultBuildAction action = build.getAction(getBuildActionClass());
			if (action != null) {
				return action;
			}
		}
		return null;
	}
	
	protected void populateDataSetBuilderJobs(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {
		if (!(job instanceof LazyBuildMixIn.LazyLoadingJob)) {
			return;
		}
		SortedMap<Integer, Run<?, ?>> loadedBuilds = (SortedMap<Integer, Run<?, ?>>) ((LazyLoadingJob<?, ?>) job).getLazyBuildMixIn()._getRuns().getLoadedBuilds();
		for (Run<?, ?> build : loadedBuilds.values()) {
			ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
			TestResultsAggregatorTestResultBuildAction action = build.getAction(getBuildActionClass());
			Result result = build.getResult();
			if (result == null || result.isWorseThan(Result.FAILURE)) {
				// We don't want to add aborted or builds with no results into the graph
				continue;
			}
			if (action != null) {
				dataset.add(action.getSuccess() + action.getFixed(), SUCCESS, label);
				dataset.add(action.getFailCount() + action.getFailKeepCount(), FAILED, label);
				dataset.add(action.getUnstableCount() + action.getUnstableKeepCount(), UNSTABLE, label);
				dataset.add(action.getAborted(), ABORTED, label);
				dataset.add(action.getRunning(), RUNNING, label);
			}
		}
	}
	
	protected void populateDataSetBuilderTest(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset) {
		if (!(job instanceof LazyBuildMixIn.LazyLoadingJob)) {
			return;
		}
		SortedMap<Integer, Run<?, ?>> loadedBuilds = (SortedMap<Integer, Run<?, ?>>) ((LazyLoadingJob<?, ?>) job).getLazyBuildMixIn()._getRuns().getLoadedBuilds();
		for (Run<?, ?> build : loadedBuilds.values()) {
			ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
			TestResultsAggregatorTestResultBuildAction action = build.getAction(getBuildActionClass());
			Result result = build.getResult();
			if (result == null || result.isWorseThan(Result.FAILURE)) {
				// We don't want to add aborted or builds with no results into the graph
				continue;
			}
			if (action != null) {
				dataset.add(action.getSuccessTTests(), SUCCESS, label);
				dataset.add(action.getFailedTTests(), FAILED, label);
				dataset.add(action.getSkippedTTests(), UNSTABLE, label);
			}
		}
	}
	
	public int getGraphWidth() {
		return 500;
	}
	
	public int getGraphHeight() {
		return 200;
	}
}
