package com.jenkins.testresultsaggregator.helper;

import java.awt.Color;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.jenkins.testresultsaggregator.TestResultsAggregator;
import com.jenkins.testresultsaggregator.TestResultsAggregatorProjectAction;
import com.jenkins.testresultsaggregator.TestResultsAggregatorTestResultBuildAction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;

/**
 * Helper class for trend graph generation
 */
@SuppressFBWarnings(value = "EQ_DOESNT_OVERRIDE_EQUALS", justification = "BarRenderer subclasses do not seem to need to override it")
public class GraphHelper {
	
	/**
	 * Do not instantiate GraphHelper.
	 */
	private GraphHelper() {
	}
	
	public static void redirectWhenGraphUnsupported(StaplerResponse rsp, StaplerRequest req) throws IOException {
		rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
	}
	
	public static JFreeChart createChartJob(final StaplerRequest req, CategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createStackedAreaChart(
				null, // chart title
				null, // unused
				"Jobs Count", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // urls
		);
		
		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		final LegendTitle legend = chart.getLegend();
		legend.setPosition(RectangleEdge.RIGHT);
		
		chart.setBackgroundPaint(Color.white);
		
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setForegroundAlpha(0.8f);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		
		CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);
		
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		StackedAreaRenderer ar = new StackedAreaRenderer2() {
			@Override
			public String generateURL(CategoryDataset dataset, int row, int column) {
				NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
				String path = req.getParameter("rel");
				return (path == null ? "" : path) + label.getRun().getNumber() + "/" + TestResultsAggregator.URL + "/";
			}
			
			@Override
			public String generateToolTip(CategoryDataset dataset, int row, int column) {
				NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
				TestResultsAggregatorTestResultBuildAction report = label.getRun().getAction(TestResultsAggregatorTestResultBuildAction.class);
				if (report == null) {
					// there are no results associated with this build
					return "";
				}
				if (TestResultsAggregatorProjectAction.SUCCESS.equalsIgnoreCase(dataset.getRowKey(row).toString())
						|| TestResultsAggregatorProjectAction.FIXED.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getSuccess() + report.getFixed()) + " " + TestResultsAggregatorProjectAction.SUCCESS;
				} else if (TestResultsAggregatorProjectAction.ABORTED.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getAborted()) + " " + TestResultsAggregatorProjectAction.ABORTED;
				} else if (TestResultsAggregatorProjectAction.FAILED.equalsIgnoreCase(dataset.getRowKey(row).toString())
						|| TestResultsAggregatorProjectAction.FAILED_KEEP.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getFailCount() + report.getFailKeepCount()) + " " + TestResultsAggregatorProjectAction.FAILED;
				} else if (TestResultsAggregatorProjectAction.RUNNING.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getRunning()) + " " + TestResultsAggregatorProjectAction.RUNNING;
				} else if (TestResultsAggregatorProjectAction.UNSTABLE.equalsIgnoreCase(dataset.getRowKey(row).toString())
						|| TestResultsAggregatorProjectAction.UNSTABLE_KEEP.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getUnstableCount() + report.getUnstableKeepCount()) + " " + TestResultsAggregatorProjectAction.UNSTABLE;
				} else if (TestResultsAggregatorProjectAction.DISABLED.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getDisabled() + " " + TestResultsAggregatorProjectAction.DISABLED);
				} else {
					return "";
				}
			}
		};
		plot.setRenderer(ar);
		// Set Custom Colors
		for (int i = 0; i < dataset.getRowCount(); i++) {
			if (TestResultsAggregatorProjectAction.SUCCESS.equalsIgnoreCase(dataset.getRowKey(i).toString())
					|| TestResultsAggregatorProjectAction.FIXED.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.SUCCESS); // Success
			} else if (TestResultsAggregatorProjectAction.ABORTED.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.ABORTED); // Aborted
			} else if (TestResultsAggregatorProjectAction.FAILED.equalsIgnoreCase(dataset.getRowKey(i).toString())
					|| TestResultsAggregatorProjectAction.FAILED_KEEP.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.FAILED); // Failed
			} else if (TestResultsAggregatorProjectAction.RUNNING.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.RUNNING); // Running
			} else if (TestResultsAggregatorProjectAction.UNSTABLE.equalsIgnoreCase(dataset.getRowKey(i).toString())
					|| TestResultsAggregatorProjectAction.UNSTABLE_KEEP.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.UNSTABLE); // Unstable
			} else if (TestResultsAggregatorProjectAction.DISABLED.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.DISABLED); // DISABLED
			}
		}
		plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
		return chart;
	}
	
	public static JFreeChart createChartTests(final StaplerRequest req, CategoryDataset dataset) {
		final JFreeChart chart = ChartFactory.createStackedAreaChart(
				null, // chart title
				null, // unused
				"Test Count", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // urls
		);
		
		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		final LegendTitle legend = chart.getLegend();
		legend.setPosition(RectangleEdge.RIGHT);
		
		chart.setBackgroundPaint(Color.white);
		
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setForegroundAlpha(0.8f);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		
		CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);
		
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		StackedAreaRenderer ar = new StackedAreaRenderer2() {
			@Override
			public String generateURL(CategoryDataset dataset, int row, int column) {
				NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
				String path = req.getParameter("rel");
				return (path == null ? "" : path) + label.getRun().getNumber() + "/" + TestResultsAggregator.URL + "/";
			}
			
			@Override
			public String generateToolTip(CategoryDataset dataset, int row, int column) {
				NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
				TestResultsAggregatorTestResultBuildAction report = label.getRun().getAction(TestResultsAggregatorTestResultBuildAction.class);
				if (report == null) {
					// there are no results associated with this build
					return "";
				}
				if (TestResultsAggregatorProjectAction.SUCCESS.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getSuccessTTests()) + " " + TestResultsAggregatorProjectAction.SUCCESS;
				} else if (TestResultsAggregatorProjectAction.FAILED.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getFailedTTests()) + " " + TestResultsAggregatorProjectAction.FAILED;
				} else if (TestResultsAggregatorProjectAction.UNSTABLE.equalsIgnoreCase(dataset.getRowKey(row).toString())) {
					return String.valueOf(report.getSkippedTTests()) + " " + TestResultsAggregatorProjectAction.UNSTABLE;
				} else {
					return "";
				}
			}
		};
		plot.setRenderer(ar);
		// Set Custom Colors
		for (int i = 0; i < dataset.getRowCount(); i++) {
			if (TestResultsAggregatorProjectAction.SUCCESS.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.SUCCESS); // Success
			} else if (TestResultsAggregatorProjectAction.FAILED.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.FAILED); // Failed
			} else if (TestResultsAggregatorProjectAction.UNSTABLE.equalsIgnoreCase(dataset.getRowKey(i).toString())) {
				ar.setSeriesPaint(i, Colors.UNSTABLE); // Unstable
			}
		}
		plot.setInsets(new RectangleInsets(0, 0, 0, 2.0));
		return chart;
	}
}
