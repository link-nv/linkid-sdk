/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.io.IOException;
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
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * @author mbillemo
 * 
 */
@Stateless
@RemoteBinding(jndiBinding = "SafeOnline/ScenarioBean")
public class ScenarioBean implements ScenarioRemote {

	private static final Log LOG = LogFactory.getLog(ScenarioBean.class);

	private List<ProfileDriver> drivers;

	private ImageEncoder encoder;

	/**
	 * Create a new ScenarioBean instance.
	 */
	public ScenarioBean() {

		this.encoder = ImageEncoderFactory.newInstance("png", 0.9f, true);
	}

	public List<byte[]> execute(String hostname) {

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
	private List<byte[]> createGraphs() {

		// List of charts generated as a result of this scenario.
		List<byte[]> charts = new ArrayList<byte[]>();
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
			Map<String, List<Long>> driverMethods = new HashMap<String, List<Long>>();
			driversMethods.put(driver.getTitle(), driverMethods);
			Map<String, XYSeries> timingSet = new HashMap<String, XYSeries>();
			Map<String, XYSeries> errorsSet = new HashMap<String, XYSeries>();
			XYSeries speedsSet = new XYSeries("Speed", true, false);
			XYSeries afterMemorySet = new XYSeries("Memory After", true, false);
			XYSeries beforeMemorySet = new XYSeries("Memory Before", true,
					false);

			for (Integer i = 0; i < iterations; ++i) {

				ProfileData data = driver.getProfileData().get(i);
				Throwable error = driver.getProfileError().get(i);
				Double speed = driver.getProfileSpeed().get(i);

				// If there's profile data for this iteration..
				if (null != data && null != data.getMeasurements()) {
					for (Map.Entry<String, Long> measurement : data
							.getMeasurements().entrySet()) {
						String method = measurement.getKey();
						Long timing = measurement.getValue();

						// Collect Iteration Timing Chart Data.
						if (!ProfileData.isRequestKey(method)) {
							if (!timingSet.containsKey(method))
								timingSet.put(method, new XYSeries(method,
										true, false));

							timingSet.get(method).add(i, timing);
						}

						// Collect Method Timing Chart Data.
						if (!driverMethods.containsKey(method))
							driverMethods.put(method, new ArrayList<Long>());
						driverMethods.get(method).add(timing);
					}

					// Add Request Time at the end.
					// (so it's on the bottom of the chart's legend).
					double requestTime = data
							.getMeasurement(ProfileData.REQUEST_DELTA_TIME);
					double beforeMemory = data
							.getMeasurement(ProfileData.REQUEST_FREE_MEM);
					double afterMemory = data
							.getMeasurement(ProfileData.REQUEST_USED_MEM)
							+ beforeMemory;
					LOG.debug("Chart " + driver.getTitle() + ", Iteration " + i
							+ ": " + beforeMemory + " -> " + afterMemory);
					// TODO: timingData.addValue(requestTime, "Request Time",
					// i);
					beforeMemorySet.add((double) i, beforeMemory);
					afterMemorySet.add((double) i, afterMemory);
				}

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

					if (!errorsSet.containsKey(message))
						errorsSet.put(message, new XYSeries(message, true,
								false));

					errorsSet.get(message).add((double) i, 1);
				}

				// If there's a speed for this iteration..
				if (null != speed)
					speedsSet.add(i, speed);
			}

			// Convert XY data into XY Datasets and discard the temporary data.
			DefaultTableXYDataset speedsData = new DefaultTableXYDataset();
			DefaultTableXYDataset timingData = new DefaultTableXYDataset();
			DefaultTableXYDataset memoryData = new DefaultTableXYDataset();
			DefaultTableXYDataset errorsData = new DefaultTableXYDataset();
			speedsData.addSeries(speedsSet);
			memoryData.addSeries(beforeMemorySet);
			memoryData.addSeries(afterMemorySet);
			for (XYSeries timingSeries : timingSet.values())
				timingData.addSeries(timingSeries);
			for (XYSeries errorsSeries : errorsSet.values())
				errorsData.addSeries(errorsSeries);
			beforeMemorySet = afterMemorySet = null;
			timingSet = null;

			// Bar Charts.
			NumberAxis iterationAxis = new NumberAxis("Iterations");
			NumberAxis speedsAxis = new NumberAxis("Requests Per Second");
			NumberAxis timingAxis = new NumberAxis("Time Elapsed");
			NumberAxis memoryAxis = new NumberAxis("Used Memory");
			NumberAxis errorsAxis = new NumberAxis("Exceptions");

			XYPlot speedsPlot = new XYPlot(speedsData, iterationAxis,
					speedsAxis, new XYAreaRenderer2());
			XYPlot timingPlot = new XYPlot(timingData, iterationAxis,
					timingAxis, new StackedXYAreaRenderer2());
			XYPlot memoryPlot = new XYPlot(memoryData, iterationAxis,
					memoryAxis, new XYDifferenceRenderer());
			XYPlot errorsPlot = new XYPlot(errorsData, iterationAxis,
					errorsAxis, new XYAreaRenderer2());

			CombinedDomainXYPlot timingAndMemoryPlot = new CombinedDomainXYPlot(
					iterationAxis);
			timingAndMemoryPlot.add(speedsPlot);
			timingAndMemoryPlot.add(memoryPlot);
			timingAndMemoryPlot.add(timingPlot);
			timingAndMemoryPlot.add(errorsPlot);
			JFreeChart iterationChart = new JFreeChart("Statistics for: "
					+ driver.getTitle(), timingAndMemoryPlot);

			// Image.
			charts.add(getImage(iterationChart, 1000, 2000));
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
		charts.add(getImage(methodChart, 1000, 1000));
		charts.add(getImage(requestChart, 1000, 1000));

		return charts;
	}

	private byte[] getImage(JFreeChart chart, int width, int height) {

		try {
			return this.encoder
					.encode(chart.createBufferedImage(width, height));
		} catch (IOException e) {
			return null;
		}
	}

	protected void register(ProfileDriver... profileDrivers) {

		this.drivers.addAll(Arrays.asList(profileDrivers));
	}
}
