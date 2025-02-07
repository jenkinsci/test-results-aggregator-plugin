package com.jenkins.testresultsaggregator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jenkins.testresultsaggregator.actions.ResultsParser;
import com.jenkins.testresultsaggregator.data.Aggregated;
import com.jenkins.testresultsaggregator.reports.XMLReporter;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import jenkins.tasks.SimpleBuildStep;

/**
 * TestNG build action that exposes the results per build
 *
 * @author nullin
 * @since v1.0
 */
public class TestResultsAggregatorTestResultBuildAction extends AbstractTestResultAction implements Serializable, SimpleBuildStep.LastBuildAction {
	
	private static final long serialVersionUID = 31415926L;
	private static final Logger LOGGER = Logger.getLogger(TestResultsAggregatorTestResultBuildAction.class.getName());
	private transient Reference<Aggregated> aggregatedResults;
	// Jobs
	protected Integer success;
	protected int fixed;
	protected int failed;
	protected int keepfailed;
	protected int unstable;
	protected int keepunstable;
	protected int aborted;
	protected int running;
	private int disabled;
	// Tests
	protected int successTTests;
	protected int failedTTests;
	protected int skippedTTests;
	
	public TestResultsAggregatorTestResultBuildAction(Aggregated aggregatedResults) {
		if (aggregatedResults != null) {
			this.aggregatedResults = new WeakReference<Aggregated>(aggregatedResults);
			count(aggregatedResults);
		}
	}
	
	private void count(Aggregated aggregated) {
		// Job stats
		this.success = aggregated.getSuccessJobs();
		this.fixed = aggregated.getFixedJobs();
		this.failed = aggregated.getFailedJobs();
		this.keepfailed = aggregated.getKeepFailJobs();
		this.aborted = aggregated.getAbortedJobs();
		this.unstable = aggregated.getUnstableJobs();
		this.keepunstable = aggregated.getKeepUnstableJobs();
		this.running = aggregated.getRunningJobs();
		this.disabled = aggregated.getDisabledJobs();
		// Tests stats
		if (aggregated.getResults() != null) {
			this.successTTests = aggregated.getResults().getPass();
			this.failedTTests = aggregated.getResults().getFail();
			this.skippedTTests = aggregated.getResults().getSkip();
		} else {
			this.successTTests = 0;
			this.failedTTests = 0;
			this.skippedTTests = 0;
		}
	}
	
	private void countAndSave(Aggregated aggregatedDTO) {
		int savedSuccessCount = success != null ? success : -1;
		int savedFixedCount = fixed;
		int savedFailedCount = failed;
		int savedFailedKeepCount = keepfailed;
		int savedUnstableCount = unstable;
		int savedUnstableKeepCount = keepunstable;
		int savedAbortedCount = aborted;
		int savedRunningCount = running;
		int savedDisabledCount = disabled;
		
		count(aggregatedDTO);
		if (success != savedSuccessCount || failed != savedFailedCount || unstable != savedUnstableCount || aborted != savedAbortedCount
				|| running != savedRunningCount || fixed != savedFixedCount || keepfailed != savedFailedKeepCount || keepunstable != savedUnstableKeepCount || disabled != savedDisabledCount) {
			try {
				owner.save();
			} catch (IOException x) {
				LOGGER.log(Level.WARNING, "failed to save " + owner, x);
			}
		}
	}
	
	private void countAsNeeded() {
		if (success == null) {
			countAndSave(getResult());
		}
	}
	
	@Override
	public Aggregated getResult() {
		return getResult(super.run);
	}
	
	public Aggregated getResult(Run build) {
		Aggregated tr = aggregatedResults != null ? aggregatedResults.get() : null;
		if (tr == null) {
			tr = loadResults(build);
			countAndSave(tr);
			aggregatedResults = new WeakReference<Aggregated>(tr);
		}
		return tr;
	}
	
	static Aggregated loadResults(Run<?, ?> owner) {
		FilePath resultDirectory = getAggregatedReport(owner);
		FilePath[] paths = null;
		try {
			paths = resultDirectory.list(XMLReporter.REPORT_XML_FILE);
		} catch (Exception e) {
			// do nothing
		}
		if (paths == null) {
			Aggregated aggregatedDTO = new Aggregated();
			aggregatedDTO.setRun(owner);
			return aggregatedDTO;
		}
		ResultsParser parser = new ResultsParser();
		Aggregated aggregatedDTO = parser.parse(paths);
		aggregatedDTO.setRun(owner);
		return aggregatedDTO;
	}
	
	static FilePath getAggregatedReport(Run<?, ?> build) {
		return new FilePath(new File(build.getRootDir(), ""));
	}
	
	@Override
	public String getIconFileName() {
		return TestResultsAggregator.ICON_FILE_NAME;
	}
	
	@Override
	public int getTotalCount() {
		return failed + unstable + success + aborted + running;
	}
	
	public int getFailCount() {
		countAsNeeded();
		return failed;
	}
	
	public int getFailKeepCount() {
		countAsNeeded();
		return keepfailed;
	}
	
	public int getUnstableCount() {
		countAsNeeded();
		return unstable;
	}
	
	public int getUnstableKeepCount() {
		countAsNeeded();
		return keepunstable;
	}
	
	public int getSuccess() {
		countAsNeeded();
		return success;
	}
	
	public int getFixed() {
		countAsNeeded();
		return fixed;
	}
	
	public int getAborted() {
		countAsNeeded();
		return aborted;
	}
	
	public int getRunning() {
		countAsNeeded();
		return running;
	}
	
	public int getSuccessTTests() {
		countAsNeeded();
		return successTTests;
	}
	
	public int getFailedTTests() {
		countAsNeeded();
		return failedTTests;
	}
	
	public int getSkippedTTests() {
		countAsNeeded();
		return skippedTTests;
	}
	
	@Override
	public String getDisplayName() {
		return TestResultsAggregator.DISPLAY_NAME;
	}
	
	@Override
	public String getUrlName() {
		return TestResultsAggregator.URL;
	}
	
	@Override
	public Api getApi() {
		return new Api(getResult());
	}
	
	@Override
	public Collection<? extends Action> getProjectActions() {
		return Collections.singleton(new TestResultsAggregatorProjectAction(run.getParent()));
	}
	
	public int getDisabled() {
		countAsNeeded();
		return disabled;
	}
	
	public void setDisabled(int disabled) {
		this.disabled = disabled;
	}
	
}
