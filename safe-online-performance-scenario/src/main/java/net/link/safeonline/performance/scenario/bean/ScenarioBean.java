/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.StartTimeEntity;
import net.link.safeonline.performance.scenario.Scenario;
import net.link.safeonline.performance.scenario.ScenarioLocal;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.util.performance.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
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
@LocalBinding(jndiBinding = ScenarioLocal.BINDING)
public class ScenarioBean implements ScenarioLocal {

	private static final Log LOG = LogFactory.getLog(ScenarioBean.class);

	private static DateFormat timeFormat = DateFormat.getTimeInstance();
	private static ImageEncoder encoder = ImageEncoderFactory.newInstance(
			"png", 0.9f, true);

	@EJB
	private ExecutionService executionService;

	@Resource(name = "activeScenario")
	private String activeScenario;

	/**
	 * {@inheritDoc}
	 */
	public void execute(int executionId) throws Exception {

		LOG.debug("building scenario: " + this.activeScenario);
		Scenario scenario = createScenario();

		ExecutionEntity execution = this.executionService
				.getExecution(executionId);
		scenario.prepare(execution);

		this.executionService.addStartTime(execution, System
				.currentTimeMillis());
		scenario.run();
	}

	/**
	 * Create an instance of the scenario configured to run in the ejb-jar.
	 */
	private Scenario createScenario() {

		try {
			return (Scenario) Thread.currentThread().getContextClassLoader()
					.loadClass(this.activeScenario).newInstance();
		} catch (Exception e) {
			LOG.debug("Configured scenario '" + this.activeScenario
					+ "' cannot be created.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public int prepare(String hostname) {

		ExecutionEntity execution = this.executionService.addExecution(
				this.activeScenario, hostname);
		createScenario().prepare(execution);

		return execution.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public Double getSpeed(int executionId) {

		ExecutionEntity execution = this.executionService
				.getExecution(executionId);
		TreeSet<StartTimeEntity> sortedStartTimes = new TreeSet<StartTimeEntity>(
				execution.getStartTimes());

		return (double) sortedStartTimes.size()
				/ (sortedStartTimes.last().getTime() - sortedStartTimes.first()
						.getTime());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getScenario(int executionId) {

		ExecutionEntity execution = this.executionService
				.getExecution(executionId);

		return execution.getScenarioName();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, byte[][]> createGraphs(int executionId) {

		ExecutionEntity execution = this.executionService
				.getExecution(executionId);
		Set<DriverProfileEntity> profiles = execution.getProfiles();

		// List of charts generated as a result of this scenario.
		CategoryAxis catAxis;
		ValueAxis valueAxis;

		// Dataset for a Box Chart of method timings per drivers.
		DefaultBoxAndWhiskerCategoryDataset driversMethodSet = new DefaultBoxAndWhiskerCategoryDataset();
		DefaultBoxAndWhiskerCategoryDataset driversRequestSet = new DefaultBoxAndWhiskerCategoryDataset();
		Map<String, Map<String, List<Long>>> driversMethods = new HashMap<String, Map<String, List<Long>>>();

		// Collect data from drivers.
		LOG.debug("BUILDING CHARTS FOR " + profiles.size() + " DRIVERS:");
		Map<String, List<byte[]>> driversCharts = new LinkedHashMap<String, List<byte[]>>(
				profiles.size());
		for (DriverProfileEntity profile : profiles) {

			// Invalid / empty driver profiles.
			if (profile.getDriverName() == null
					|| profile.getProfileData().isEmpty()) {
				LOG.warn("Invalid/empty driver profile: '"
						+ profile.getDriverName() + "'!");
				continue;
			}

			// Dataset for a Bar Chart of method timings per iteration.
			Map<String, List<Long>> driverMethods = new HashMap<String, List<Long>>();
			driversMethods.put(profile.getDriverName(), driverMethods);
			Map<String, XYSeries> timingSet = new HashMap<String, XYSeries>();
			DefaultCategoryDataset errorsSet = new DefaultCategoryDataset();
			XYSeries requestSet = new XYSeries("Request Time", true, false);
			XYSeries afterMemorySet = new XYSeries("Memory After", true, false);
			XYSeries beforeMemorySet = new XYSeries("Memory Before", true,
					false);

			for (ProfileDataEntity data : profile.getProfileData()) {

				if (data.getMeasurements() == null || data.getStartTime() == 0)
					continue;

				// Process the statistics.
				long requestTime = data
						.getMeasurement(ProfileData.REQUEST_DELTA_TIME);
				long beforeMemory = data
						.getMeasurement(ProfileData.REQUEST_START_FREE);
				long afterMemory = data
						.getMeasurement(ProfileData.REQUEST_END_FREE)
						+ beforeMemory;
				long endTime = data.getStartTime() + requestTime;

				try {
					beforeMemorySet.add(data.getStartTime(), beforeMemory);
					afterMemorySet.add(data.getStartTime(), afterMemory);
					requestSet.add(data.getStartTime(), requestTime);
				} catch (SeriesException e) {
					LOG.warn("Dublicate X at starttime " + data.getStartTime()
							+ ", or endtime " + endTime, e);
					continue; // Duplicate X value; ignore this one.
				}

				// Per-method iteration statistics.
				for (MeasurementEntity measurement : data.getMeasurements()) {

					String method = measurement.getMeasurement();
					Long timing = measurement.getDuration();

					// Collect Iteration Timing Chart Data.
					if (!ProfileData.isRequestKey(method)) {
						if (!timingSet.containsKey(method))
							timingSet.put(method, new XYSeries(method, true,
									false));

						timingSet.get(method).add(data.getStartTime(), timing);
					}

					// Collect Method Timing Chart Data.
					if (!driverMethods.containsKey(method))
						driverMethods.put(method, new ArrayList<Long>());
					driverMethods.get(method).add(timing);
				}
			}

			for (DriverExceptionEntity error : profile.getProfileError())
				errorsSet.addValue((Number) 1, error.getMessage(), timeFormat
						.format(error.getOccurredTime()));

			// Calculate averages.
			double requestAvg = 0, memoryAvg = 0;
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

			List<byte[]> driverCharts = new ArrayList<byte[]>();
			driversCharts.put(profile.getDriverName(), driverCharts);

			JFreeChart statisticsChart = new JFreeChart(
					"Method Call Durations:", timingAndMemoryPlot);
			driverCharts.add(getImage(statisticsChart, 1000, 1000));

			if (errorsSet.getRowCount() > 0) {
				CategoryPlot errorsPlot = new CategoryPlot(errorsSet,
						new CategoryAxis("Occurance"), errorAxis,
						new BarRenderer());

				JFreeChart errorsChart = new JFreeChart("Exceptions:",
						errorsPlot);
				driverCharts.add(getImage(errorsChart, 1000, 150));
			}
		}

		// Create the scenario speed data.
		List<DefaultTableXYDataset> scenarioSpeedSets = new ArrayList<DefaultTableXYDataset>();

		// Calculate moving averages from the scenario starts for 3 periods.
		Set<StartTimeEntity> startTimes = execution.getStartTimes();
		if (startTimes == null || startTimes.isEmpty())
			LOG.warn("No scenario start times available.");

		else {
			SortedSet<StartTimeEntity> sortedStartTimes = new TreeSet<StartTimeEntity>(
					startTimes);

			for (int period : new int[] { 60000, 3600000 }) {
				XYSeries scenarioSpeedSeries = new XYSeries("Classes Of "
						+ period / 1000 + "s", false, false);
				DefaultTableXYDataset scenarioSpeedSet = new DefaultTableXYDataset();
				scenarioSpeedSet.addSeries(scenarioSpeedSeries);
				scenarioSpeedSets.add(scenarioSpeedSet);

				Long lastTime = sortedStartTimes.last().getTime();
				for (long time = sortedStartTimes.first().getTime(); time < lastTime; time += period) {

					// Count the amount of scenarios ${period} ms after ${time}.
					double count = 0;
					for (StartTimeEntity start : sortedStartTimes)
						if (null != start && start.getTime() >= time
								&& start.getTime() < time + period)
							count++;

					scenarioSpeedSeries.add(time, 1000 * count / period);
				}

				try {
					scenarioSpeedSeries.add((double) lastTime, 1d / period);
				} catch (SeriesException e) {
				}
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
		DateAxis timeAxis = new DateAxis("Time (ms)");
		CombinedDomainXYPlot speedPlot = new CombinedDomainXYPlot(timeAxis);
		for (DefaultTableXYDataset scenarioSpeedSet : scenarioSpeedSets)
			speedPlot
					.add(new XYPlot(scenarioSpeedSet, timeAxis, new NumberAxis(
							"Speed (#/s)"), new XYLineAndShapeRenderer()));
		JFreeChart speedChart = new JFreeChart(speedPlot);

		catAxis = new CategoryAxis("Methods");
		valueAxis = new NumberAxis("Time Elapsed (ms)");
		CategoryPlot timingPlot = new CategoryPlot(driversMethodSet, catAxis,
				valueAxis, new BoxAndWhiskerRenderer());
		JFreeChart methodChart = new JFreeChart(timingPlot);

		catAxis = new CategoryAxis("Methods");
		valueAxis = new NumberAxis("Time Elapsed (ms)");
		CategoryPlot requestPlot = new CategoryPlot(driversRequestSet, catAxis,
				valueAxis, new BoxAndWhiskerRenderer());
		JFreeChart requestChart = new JFreeChart(requestPlot);

		// Charts.
		Map<String, byte[][]> charts = new LinkedHashMap<String, byte[][]>();
		charts.put("Scenario Execution Speed", new byte[][] { getImage(
				speedChart, 1000, 1000) });
		charts.put("Request Duration per Driver", new byte[][] { getImage(
				requestChart, 1000, 1000) });
		charts.put("Method Duration per Driver", new byte[][] { getImage(
				methodChart, 1000, 1000) });
		for (Map.Entry<String, List<byte[]>> driverCharts : driversCharts
				.entrySet())
			charts.put(driverCharts.getKey(), driverCharts.getValue().toArray(
					new byte[0][0]));

		return charts;
	}

	private byte[] getImage(JFreeChart chart, int width, int height) {

		try {
			chart.setBackgroundPaint(Color.white);
			return encoder.encode(chart.createBufferedImage(width, height));
		} catch (IOException e) {
			return null;
		}
	}
}
