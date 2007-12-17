/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.drivers.DriverException;
import net.link.safeonline.performance.drivers.ProfileDriver;
import net.link.safeonline.performance.scenario.ScenarioRemote;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleAnchor;

/**
 * @author mbillemo
 */
@Stateless
@RemoteBinding(jndiBinding = "SafeOnline/ScenarioBean")
public class ScenarioBean implements ScenarioRemote {

	static final Log LOG = LogFactory.getLog(ScenarioBean.class);

	private ImageEncoder encoder;
	private DateFormat timeFormat = DateFormat.getTimeInstance();
	private List<ProfileDriver<? extends MessageAccessor>> drivers;
	LinkedList<Long> scenarioStart = new LinkedList<Long>();

	/**
	 * Create a new ScenarioBean instance.
	 */
	public ScenarioBean() {

		this.encoder = ImageEncoderFactory.newInstance("png", 0.9f, true);
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<byte[]> execute(String hostname, int workers, long duration) {

		final Scenario scenario = new BasicScenario();
		this.drivers = scenario.prepare(hostname);

		ScheduledExecutorService pool = Executors
				.newScheduledThreadPool(workers);

		// Execute the scenario story.
		long until = System.currentTimeMillis() + duration;
		for (int i = 0; i < workers; ++i)
			pool.scheduleWithFixedDelay(new Runnable() {

				public void run() {

					try {
						ScenarioBean.this.scenarioStart.add(System
								.currentTimeMillis());
						scenario.execute();
					} catch (Exception e) {
						LOG.error("Test failed.", e);
					}
				}
			}, 0, 100, TimeUnit.MILLISECONDS);

		// Sleep this thread until the specified duration has elapsed.
		while (System.currentTimeMillis() < until)
			try {
				Thread.sleep(until - System.currentTimeMillis());
			} catch (InterruptedException e) {
			}

		// Shut down and wait for active scenarios to complete.
		pool.shutdown();
		try {
			while (!pool.awaitTermination(1, TimeUnit.SECONDS))
				Thread.yield();
		} catch (InterruptedException e) {
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
		LinkedList<byte[]> charts = new LinkedList<byte[]>();
		CategoryAxis catAxis;
		ValueAxis valueAxis;

		// Dataset for a Box Chart of method timings per drivers.
		DefaultBoxAndWhiskerCategoryDataset driversMethodSet = new DefaultBoxAndWhiskerCategoryDataset();
		DefaultBoxAndWhiskerCategoryDataset driversRequestSet = new DefaultBoxAndWhiskerCategoryDataset();
		Map<String, Map<String, List<Long>>> driversMethods = new HashMap<String, Map<String, List<Long>>>();

		// Collect data from drivers.
		LOG.debug("BUILDING CHARTS FOR " + this.drivers.size() + " DRIVERS:");
		for (ProfileDriver<? extends MessageAccessor> driver : this.drivers) {

			// Dataset for a Bar Chart of method timings per iteration.
			int iterations = driver.getProfileData().size();
			Map<String, List<Long>> driverMethods = new HashMap<String, List<Long>>();
			driversMethods.put(driver.getTitle(), driverMethods);
			Map<String, XYSeries> timingSet = new HashMap<String, XYSeries>();
			DefaultCategoryDataset errorsSet = new DefaultCategoryDataset();
			XYSeries requestSet = new XYSeries("Request Time", true, false);
			XYSeries afterMemorySet = new XYSeries("Memory After", true, false);
			XYSeries beforeMemorySet = new XYSeries("Memory Before", true,
					false);

			LOG.debug(" + driver: " + driver.getTitle());
			LOG.debug(" + iterations: " + iterations);
			for (Integer i = 0; i < iterations; ++i) {

				LOG.debug(" * iteration: " + i);
				ProfileData data = driver.getProfileData().get(i);
				DriverException error = driver.getProfileError().get(i);

				// See if we have any information to put on the graph at all..
				long startTime = 0;
				if (null != data && null != data.getMeasurements()) {
					startTime = data
							.getMeasurement(ProfileData.REQUEST_START_TIME);
					LOG.debug(String.format(" - request started at: %d",
							startTime));
				}
				if (startTime == 0 && null != error) {
					startTime = error.getOccurredTime();
					LOG.debug(String.format(" - error occurred at: %d",
							startTime));
				}
				if (startTime == 0) {
					LOG.warn(" - couldn't find a time for this iteration");
					continue;
				}

				// General iteration request statistics.
				if (null != data && null != data.getMeasurements()) {
					long requestTime = data
							.getMeasurement(ProfileData.REQUEST_DELTA_TIME);
					long beforeMemory = data
							.getMeasurement(ProfileData.REQUEST_START_FREE);
					long afterMemory = data
							.getMeasurement(ProfileData.REQUEST_END_FREE)
							+ beforeMemory;
					long endTime = startTime + requestTime;

					LOG.debug(String.format(" - duration:  %d", requestTime));
					LOG.debug(String.format(" - beforemem: %d", beforeMemory));
					LOG.debug(String.format(" - aftermem:  %d", afterMemory));
					LOG.debug(String.format(" - calls:     %d", data
							.getMeasurements().size() - 4));
					LOG.debug(String.format(" - call data: %s", data
							.getMeasurements().values().toString()));

					try {
						beforeMemorySet.add(startTime, beforeMemory);
						afterMemorySet.add(startTime, afterMemory);
						requestSet.add(startTime, requestTime);
					} catch (SeriesException e) {
						LOG.warn("Dublicate X at starttime " + startTime
								+ ", or endtime " + endTime, e);
						continue; // Duplicate X value; ignore this one.
					}

					// Per-method iteration statistics.
					for (Map.Entry<String, Long> measurement : data
							.getMeasurements().entrySet()) {

						String method = measurement.getKey();
						Long timing = measurement.getValue();

						// Collect Iteration Timing Chart Data.
						if (!ProfileData.isRequestKey(method)) {
							if (!timingSet.containsKey(method))
								timingSet.put(method, new XYSeries(method,
										true, false));

							timingSet.get(method).add(startTime, timing);
						}

						// Collect Method Timing Chart Data.
						if (!driverMethods.containsKey(method))
							driverMethods.put(method, new ArrayList<Long>());
						driverMethods.get(method).add(timing);
					}
				}

				// If there's an exception for this iteration..
				if (null != error) {
					Throwable cause = error;
					while (cause.getCause() != null)
						cause = cause.getCause();

					LOG.debug(" - error: " + error);
					LOG.debug(" - rootcause: " + cause);

					int errorSourceLine = -1;
					String errorSourceClass = null;
					StackTraceElement errorSource = null;

					if (cause.getStackTrace().length > 0) {
						errorSource = cause.getStackTrace()[0];
						errorSourceClass = ProfileData
								.compressSignature(errorSource.getClassName());
						errorSourceLine = errorSource.getLineNumber();
					}

					String errorClass = ProfileData.compressSignature(cause
							.getClass().getName());
					String message = String.format("%s: %s (%s:%d)",
							errorClass, cause.getMessage(), errorSourceClass,
							errorSourceLine);

					errorsSet.addValue((Number) 1, message, this.timeFormat
							.format(startTime));
				}
			}

			// Calculate averages.
			double speedAvg = 0, requestAvg = 0, memoryAvg = 0;
			for (Object item : requestSet.getItems())
				if (item instanceof XYDataItem)
					requestAvg += ((XYDataItem) item).getY().doubleValue();
			for (Object item : beforeMemorySet.getItems())
				if (item instanceof XYDataItem)
					memoryAvg += ((XYDataItem) item).getY().doubleValue();
			for (Object item : afterMemorySet.getItems())
				if (item instanceof XYDataItem)
					memoryAvg += ((XYDataItem) item).getY().doubleValue();
			requestAvg /= requestSet.getItemCount();
			memoryAvg /= beforeMemorySet.getItemCount()
					+ afterMemorySet.getItemCount();
			LOG.debug("speed avg: " + speedAvg);
			LOG.debug("request avg: " + requestAvg);
			LOG.debug("memory avg: " + memoryAvg);

			// Convert XY data into XY Datasets and discard the temporary data.
			DefaultTableXYDataset requestData = new DefaultTableXYDataset();
			DefaultTableXYDataset timingData = new DefaultTableXYDataset();
			DefaultTableXYDataset memoryData = new DefaultTableXYDataset();
			requestData.addSeries(requestSet);
			memoryData.addSeries(beforeMemorySet);
			memoryData.addSeries(afterMemorySet);
			for (XYSeries timingSeries : timingSet.values())
				timingData.addSeries(timingSeries);
			requestSet = beforeMemorySet = afterMemorySet = null;
			timingSet = null;

			// Driver Charts.
			DateAxis timeAxis = new DateAxis("Time");
			NumberAxis timingAxis = new NumberAxis("Time Elapsed (ms)");
			NumberAxis memoryAxis = new NumberAxis("Available Memory (bytes)");
			NumberAxis errorAxis = new NumberAxis("Exceptions");

			CombinedDomainXYPlot timingAndMemoryPlot = new CombinedDomainXYPlot(
					timeAxis);
			if (memoryData.getItemCount() > 0) {
				XYPlot memoryPlot = new XYPlot(memoryData, timeAxis,
						memoryAxis, new XYDifferenceRenderer());

				ValueMarker marker = new ValueMarker(memoryAvg);
				marker.setLabel("Average");
				marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				memoryPlot.addRangeMarker(marker);
				timingAndMemoryPlot.add(memoryPlot);
			}
			if (timingData.getItemCount() + requestData.getItemCount() > 0) {
				XYPlot timingPlot = new XYPlot();
				timingPlot.setDataset(timingData);
				timingPlot.setDataset(1, requestData);
				timingPlot.setDomainAxis(timeAxis);
				timingPlot.setRangeAxis(timingAxis);
				timingPlot.setRenderer(new StackedXYAreaRenderer2());
				timingPlot.setRenderer(1, new XYAreaRenderer2());

				ValueMarker marker = new ValueMarker(requestAvg);
				marker.setLabel("Average");
				marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				timingPlot.addRangeMarker(marker);
				timingAndMemoryPlot.add(timingPlot);
			}

			JFreeChart statisticsChart = new JFreeChart("Statistics for: "
					+ driver.getTitle(), timingAndMemoryPlot);
			charts.add(getImage(statisticsChart, 1000, 1000));

			if (errorsSet.getRowCount() > 0) {
				CategoryPlot errorsPlot = new CategoryPlot(errorsSet,
						new CategoryAxis("Occurance"), errorAxis,
						new BarRenderer());

				JFreeChart errorsChart = new JFreeChart("Errors for: "
						+ driver.getTitle(), errorsPlot);
				charts.add(getImage(errorsChart, 1000, 150));
			}
		}

		// Create the scenario speed data.
		List<DefaultTableXYDataset> scenarioSpeedSets = new ArrayList<DefaultTableXYDataset>();

		// Calculate moving averages from the scenario starts for 3 periods.
		for (int period : new int[] { 1000, 60000, 36000000 }) {
			XYSeries scenarioSpeedSeries = new XYSeries("Period Of " + period
					/ 1000 + "s", false, false);
			DefaultTableXYDataset scenarioSpeedSet = new DefaultTableXYDataset();
			scenarioSpeedSet.addSeries(scenarioSpeedSeries);
			scenarioSpeedSets.add(scenarioSpeedSet);

			for (long time = this.scenarioStart.getFirst(); time < this.scenarioStart
					.getLast(); time += period) {

				// Count the amount of scenarios ${period} ms after ${time}.
				double count = 0;
				for (long start : this.scenarioStart)
					if (start >= time && start < time + period)
						count++;

				scenarioSpeedSeries.add(time, count / period);
			}

			try {
				scenarioSpeedSeries.add((double) this.scenarioStart.getLast(),
						1d / period);
			} catch (SeriesException e) {
			}
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

		// Scenario Charts.
		DateAxis timeAxis = new DateAxis("Time");
		CombinedDomainXYPlot speedPlot = new CombinedDomainXYPlot(timeAxis);
		for (DefaultTableXYDataset scenarioSpeedSet : scenarioSpeedSets)
			speedPlot
					.add(new XYPlot(scenarioSpeedSet, timeAxis, new NumberAxis(
							"Speed (#/s)"), new XYLineAndShapeRenderer()));
		JFreeChart speedChart = new JFreeChart("Request Timings per Driver",
				speedPlot);

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
		charts.addFirst(getImage(methodChart, 1000, 1000));
		charts.addFirst(getImage(requestChart, 1000, 1000));
		charts.addFirst(getImage(speedChart, 1000, 1000));

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

	protected void register(
			ProfileDriver<? extends MessageAccessor>... profileDrivers) {

		this.drivers.addAll(Arrays.asList(profileDrivers));
	}
}
