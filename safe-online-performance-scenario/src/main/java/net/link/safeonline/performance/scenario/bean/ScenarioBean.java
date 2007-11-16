/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import net.link.safeonline.performance.drivers.ProfileDriver;
import net.link.safeonline.performance.scenario.ScenarioRemote;
import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;

/**
 * @author mbillemo
 * 
 */
@Stateless
@RemoteBinding(jndiBinding = "SafeOnline/ScenarioBean")
public class ScenarioBean implements ScenarioRemote {

	private static final Log LOG = LogFactory.getLog(ScenarioBean.class);

	private List<ProfileDriver> drivers;

	public List<JFreeChart> execute(String hostname) {

		Scenario scenario = new BasicScenario();
		this.drivers = scenario.prepare(hostname);

		// Execute the scenario story.
		for (int iteration = 0; iteration < scenario.getIterations(); ++iteration)
			try {
				scenario.execute();
			} catch (Exception e) {
				LOG.error("Test " + iteration + " failed.", e);
			}

		// Generate the resulting statistical information.
		return createGraphs();
	}

	/**
	 * @return the graphs of the statistics collected during the execution of
	 *         this scenario.
	 */
	private List<JFreeChart> createGraphs() {

		// List of charts generated as a result of this scenario.
		List<JFreeChart> charts = new ArrayList<JFreeChart>();
		CategoryAxis catAxis;
		ValueAxis valueAxis;

		// Dataset for a Box Chart of method timings per drivers.
		DefaultBoxAndWhiskerCategoryDataset driversMethodSet = new DefaultBoxAndWhiskerCategoryDataset();
		DefaultBoxAndWhiskerCategoryDataset driversRequestSet = new DefaultBoxAndWhiskerCategoryDataset();
		Map<String, Map<String, List<Long>>> driversMethods = new HashMap<String, Map<String, List<Long>>>();

		// Collect data from drivers.
		for (ProfileDriver driver : this.drivers) {

			// Dataset for a Bar Chart of method timings per iteration.
			int iterations = driver.getProfileData().size();
			DefaultXYDataset memoryData = new DefaultXYDataset();
			DefaultCategoryDataset timingData = new DefaultCategoryDataset();
			DefaultCategoryDataset errorsData = new DefaultCategoryDataset();
			DefaultCategoryDataset speedData = new DefaultCategoryDataset();
			Map<String, List<Long>> driverMethods = new HashMap<String, List<Long>>();
			driversMethods.put(driver.getTitle(), driverMethods);
			double[][] beforeMemorySet = new double[2][iterations];
			double[][] afterMemorySet = new double[2][iterations];

			for (Integer i = 0; i < iterations; ++i) {

				ProfileData data = driver.getProfileData().get(i);
				Throwable error = driver.getProfileError().get(i);
				Double speed = driver.getProfileSpeed().get(i);

				// If there's profile data for this iteration..
				if (null != data) {
					for (Map.Entry<String, Long> measurement : data
							.getMeasurements().entrySet()) {
						String method = measurement.getKey();
						Long timing = measurement.getValue();

						// Collect Iteration Timing Chart Data.
						if (!ProfileData.isRequestKey(method))
							timingData.addValue(timing, method, i);

						// Collect Method Timing Chart Data.
						if (!driverMethods.containsKey(method))
							driverMethods.put(method, new ArrayList<Long>());
						driverMethods.get(method).add(timing);
					}

					// Add Request Time at the end.
					// (so it's on the bottom of the chart's legend).
					long requestTime = data.getMeasurements().get(
							ProfileData.REQUEST_DELTA_TIME);
					long beforeMemory = data.getMeasurements().get(
							ProfileData.REQUEST_FREE_MEM);
					long afterMemory = data.getMeasurements().get(
							ProfileData.REQUEST_USED_MEM)
							+ beforeMemory;
					timingData.addValue(requestTime, "Request Time", i);
					afterMemorySet[0][i] = i;
					beforeMemorySet[0][i] = i;
					afterMemorySet[1][i] = afterMemory;
					beforeMemorySet[1][i] = beforeMemory;
				}
				memoryData.addSeries("Memory Before", beforeMemorySet);
				memoryData.addSeries("Memory After", afterMemorySet);

				// If there's an exception for this iteration..
				if (null != error) {
					while (error.getCause() != null)
						error = error.getCause();

					StackTraceElement errorSource = error.getStackTrace()[0];
					String errorClass = ProfileData.compressSignature(error
							.getClass().getName());
					String errorSourceClass = ProfileData
							.compressSignature(errorSource.getClassName());
					String message = String.format("%s: %s (%s:%d)",
							errorClass, error.getMessage(), errorSourceClass,
							errorSource.getLineNumber());
					errorsData.addValue(1, message, i);
				}

				// If there's a speed for this iteration..
				if (null != speed)
					speedData.addValue(speed, "Requests per Second", i);
			}

			// Bar Charts.
			catAxis = new CategoryAxis("Iterations");
			valueAxis = new NumberAxis("Time Elapsed");
			CategoryPlot timingPlot = new CategoryPlot(timingData, catAxis,
					valueAxis, new StackedAreaRenderer());
			JFreeChart timingChart = new JFreeChart("Timing for: "
					+ driver.getTitle(), timingPlot);

			catAxis = new CategoryAxis("Iterations");
			valueAxis = new NumberAxis("Used Memory");
			XYPlot memoryPlot = new XYPlot(memoryData, new NumberAxis(catAxis
					.getLabel()), valueAxis, new XYDifferenceRenderer());
			JFreeChart memoryChart = new JFreeChart("Memory Usage for: "
					+ driver.getTitle(), memoryPlot);

			catAxis = new CategoryAxis("Iterations");
			valueAxis = new NumberAxis("Exceptions");
			CategoryPlot errorsPlot = new CategoryPlot(errorsData, catAxis,
					valueAxis, new AreaRenderer());
			JFreeChart errorsChart = new JFreeChart("Exceptions for: "
					+ driver.getTitle(), errorsPlot);

			catAxis = new CategoryAxis("Iterations");
			valueAxis = new NumberAxis("Requests Per Second");
			CategoryPlot speedPlot = new CategoryPlot(speedData, catAxis,
					valueAxis, new AreaRenderer());
			JFreeChart speedChart = new JFreeChart("Speed for: "
					+ driver.getTitle(), speedPlot);

			// Image.
			charts.add(speedChart);
			charts.add(timingChart);
			charts.add(memoryChart);
			charts.add(errorsChart);
		}

		// Create Box-and-Whisker objects from Method Timing Data.
		for (String driverTitle : driversMethods.keySet())
			for (Map.Entry<String, List<Long>> driverData : driversMethods.get(
					driverTitle).entrySet()) {
				String methodName = driverData.getKey();
				List<Long> methodTimings = driverData.getValue();

				if (ProfileData.REQUEST_DELTA_TIME.equals(methodName))
					driversRequestSet.add(BoxAndWhiskerCalculator
							.calculateBoxAndWhiskerStatistics(methodTimings),
							"Request Time", driverTitle);

				else if (!ProfileData.isRequestKey(methodName))
					driversMethodSet.add(BoxAndWhiskerCalculator
							.calculateBoxAndWhiskerStatistics(methodTimings),
							methodName, driverTitle);
			}

		// Box Charts.
		catAxis = new CategoryAxis("Methods");
		valueAxis = new NumberAxis("Time Elapsed");
		CategoryPlot timingPlot = new CategoryPlot(driversMethodSet, catAxis,
				valueAxis, new BoxAndWhiskerRenderer());
		JFreeChart methodChart = new JFreeChart("Method Timings per Driver",
				timingPlot);

		catAxis = new CategoryAxis("Methods");
		valueAxis = new NumberAxis("Time Elapsed");
		CategoryPlot requestPlot = new CategoryPlot(driversRequestSet, catAxis,
				valueAxis, new BoxAndWhiskerRenderer());
		JFreeChart requestChart = new JFreeChart("Request Timings per Driver",
				requestPlot);

		// Image.
		charts.add(methodChart);
		charts.add(requestChart);

		return charts;
	}

	protected void register(ProfileDriver... profileDrivers) {

		this.drivers.addAll(Arrays.asList(profileDrivers));
	}
}
