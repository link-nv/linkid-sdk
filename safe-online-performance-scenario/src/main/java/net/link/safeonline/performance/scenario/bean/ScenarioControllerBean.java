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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.scenario.ExecutionMetadata;
import net.link.safeonline.performance.scenario.Scenario;
import net.link.safeonline.performance.scenario.ScenarioController;
import net.link.safeonline.performance.scenario.script.RegisteredScripts;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.util.performance.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.TransactionTimeout;
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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleAnchor;

/**
 * <h2>{@link ScenarioControllerBean}<br>
 * <sub>This bean is the heart of the scenario application.</sub></h2>
 *
 * <p>
 * We take care of preparing scenario execution and launching a single scenario
 * run. As these methods are called, entity objects are updated with state that
 * can later be used to graph out the progress of the scenario execution.<br>
 * <br>
 * Charts are also generated in this bean.<br>
 * Currently, we create charts for:
 * <ul>
 * <li>The time spent in the OLAS performance filter.</li>
 * <li>A component-based overview of where all the time needed to execute a
 * scenario was spent.</li>
 * <li>A component-based overview of where all the time needed to execute a
 * driver request was spent.</li>
 * <li>Evolution of available memory on the agent.</li>
 * <li>Evolution of available memory on OLAS.</li>
 * <li>A box-and-whisker chart comparing driver performance.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Stateless
@Local(ScenarioController.class)
@LocalBinding(jndiBinding = ScenarioController.BINDING)
public class ScenarioControllerBean implements ScenarioController {

	/*
	 * Timeout values for long running methods (in seconds).
	 */
	private static final int CHARTING_TIMEOUT = 5 * 60 * 60; // 5h
	private static final int SCENARIO_EXECUTION_TIMEOUT = 10 * 60; // 10m

	/*
	 * Periods for the moving average charts (in milliseconds).
	 */
	private static final int[] MOVING_AVERAGE_PERIODS = new int[] {
			60 * 60 * 1000, 60 * 1000 }; // 1h, 1m

	private static final Log LOG = LogFactory
			.getLog(ScenarioControllerBean.class);
	private static final int DATA_POINTS = 1000;

	private static MBeanServerConnection rmi;
	private static DateFormat timeFormat = DateFormat.getTimeInstance();
	private static ImageEncoder encoder = ImageEncoderFactory.newInstance(
			"png", 0.9f, true);

	static {
		try {
			rmi = (MBeanServerConnection) getInitialContext().lookup(
					"jmx/invoker/RMIAdaptor");
		} catch (NamingException e) {
			LOG.error("JMX unavailable.", e);
		}
	}

	@EJB
	private ExecutionService executionService;

	@EJB
	private ProfileDataService profileDataService;

	@EJB
	private DriverExceptionService driverExceptionService;

	@Resource
	SessionContext ctx;

	/**
	 * {@inheritDoc}
	 */
	@TransactionTimeout(SCENARIO_EXECUTION_TIMEOUT)
	public void execute(Date startTime) throws Exception {

		ExecutionEntity execution = this.executionService
				.getExecution(startTime);
		AgentTimeEntity agentTime = this.executionService.start(execution);
		agentTime.setStartMemory(getFreeMemory());

		Scenario scenario = createScenario(execution.getScenarioName());
		scenario.prepare(execution, agentTime);

		try {
			scenario.run();
		} finally {
			agentTime.stop();
			agentTime.setEndMemory(getFreeMemory());

			execution.dirtySpeed();
		}
	}

	/**
	 * Create an instance of the given scenario.
	 */
	private Scenario createScenario(String scenario) {

		try {
			return (Scenario) Thread.currentThread().getContextClassLoader()
					.loadClass(scenario).newInstance();
		} catch (Exception e) {
			LOG.debug("Configured scenario '" + scenario
					+ "' cannot be created.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Date prepare(ExecutionMetadata metaData) {

		// Create the execution and fill it up with metadata.
		ExecutionEntity execution = this.executionService.addExecution(metaData
				.getScenarioName(), metaData.getAgents(),
				metaData.getWorkers(), metaData.getStartTime(), metaData
						.getDuration(), metaData.getHostname());
		createScenario(execution.getScenarioName()).prepare(execution, null);

		return execution.getStartTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getScenarios() {

		Set<String> scenarios = new HashSet<String>();
		for (Class<? extends Scenario> scenario : RegisteredScripts
				.getRegisteredScenarios())
			scenarios.add(scenario.getName());

		return scenarios;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Date> getExecutions() {

		return this.executionService.getExecutions();
	}

	/**
	 * {@inheritDoc}
	 */
	public ExecutionMetadata getExecutionMetadata(Date startTime) {

		ExecutionEntity execution = this.executionService
				.getExecution(startTime);

		return ExecutionMetadata.createResponse(execution.getScenarioName(),
				getDescription(execution.getScenarioName()), execution
						.getAgents(), execution.getWorkers(), execution
						.getStartTime(), execution.getDuration(), execution
						.getHostname(), execution.getSpeed());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription(String scenario) {

		return createScenario(scenario).getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionTimeout(CHARTING_TIMEOUT)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Map<String, byte[][]> createCharts(Date executionStartTime) {

		ExecutionEntity execution = this.executionService
				.getExecution(executionStartTime);
		Set<DriverProfileEntity> profiles = execution.getProfiles();

		// List of charts generated as a result of this scenario.
		CategoryAxis catAxis;
		ValueAxis valueAxis;

		// Dataset for a Box Chart of method timings per drivers.
		DefaultBoxAndWhiskerCategoryDataset driversMethodSet = new DefaultBoxAndWhiskerCategoryDataset();
		DefaultBoxAndWhiskerCategoryDataset driversRequestSet = new DefaultBoxAndWhiskerCategoryDataset();
		Map<String, Map<String, List<Long>>> driversMethods = new HashMap<String, Map<String, List<Long>>>();
		DefaultTableXYDataset scenarioDuration = new DefaultTableXYDataset();

		// Collect data from drivers.
		LOG.debug("BUILDING CHARTS FOR " + profiles.size() + " DRIVERS:");
		Map<String, List<byte[]>> driversCharts = new LinkedHashMap<String, List<byte[]>>(
				profiles.size());
		int _profile = 0;
		long start = System.currentTimeMillis();
		for (DriverProfileEntity profile : profiles) {
			LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
					+ " seconds");
			start = System.currentTimeMillis();
			LOG.debug("Profile: " + ++_profile + " / " + profiles.size() + ": "
					+ profile.getDriverName());

			Set<ProfileDataEntity> profileData = this.profileDataService
					.getProfileData(profile, DATA_POINTS);

			// Invalid / empty driver profiles.
			if (profile.getDriverName() == null || profileData.isEmpty()) {
				LOG.debug("Invalid/empty driver profile: '"
						+ profile.getDriverName() + "'!");
				continue;
			}

			// Dataset for a Bar Chart of method timings per iteration.
			Map<String, List<Long>> driverMethods = new HashMap<String, List<Long>>();
			driversMethods.put(profile.getDriverName(), driverMethods);
			Map<String, XYSeries> timingSet = new LinkedHashMap<String, XYSeries>();
			DefaultCategoryDataset errorsSet = new DefaultCategoryDataset();
			XYSeries afterMemorySet = new XYSeries("Memory", true, false);
			XYSeries requestSet = new XYSeries(profile.getDriverName(), true,
					false);
			scenarioDuration.addSeries(requestSet);

			int _data = 0;
			for (ProfileDataEntity data : profileData)
				try {
					LOG.debug("  -> Data: " + ++_data + " / "
							+ profileData.size());
					Set<MeasurementEntity> measurements = data
							.getMeasurements();

					// Process the statistics.
					long startTime = data.getScenarioStart();
					long requestTime = getMeasurement(measurements,
							ProfileData.REQUEST_DELTA_TIME);
					long afterMemory = getMeasurement(measurements,
							ProfileData.REQUEST_END_FREE);

					afterMemorySet.addOrUpdate(startTime, afterMemory);
					requestSet.addOrUpdate(startTime, requestTime);

					// Per-method iteration statistics.
					for (MeasurementEntity measurement : measurements) {

						String method = measurement.getMeasurement();
						Long timing = measurement.getDuration();

						// Collect Iteration Timing Chart Data.
						if (!ProfileData.isRequestKey(method)) {
							if (!timingSet.containsKey(method))
								timingSet.put(method, new XYSeries(method,
										true, false));

							timingSet.get(method)
									.addOrUpdate(startTime, timing);
						}

						// Collect Method Timing Chart Data.
						if (!driverMethods.containsKey(method))
							driverMethods.put(method, new ArrayList<Long>());
						driverMethods.get(method).add(timing);
					}
				} catch (SeriesException e) {
					// Duplicate X value; ignore this one.
				} catch (NoSuchElementException e) {
					LOG.debug("No start time found!");
				}

			for (DriverExceptionEntity error : this.driverExceptionService
					.getProfileErrors(profile))
				errorsSet.addValue((Number) 1, error.getMessage(), timeFormat
						.format(error.getOccurredTime()));

			// Calculate averages.
			LOG.debug("  -> Averages.");
			double requestAvg = 0, memoryAvg = 0;
			for (Object item : requestSet.getItems())
				if (item instanceof XYDataItem
						&& ((XYDataItem) item).getY() != null)
					requestAvg += ((XYDataItem) item).getY().doubleValue();
			for (Object item : afterMemorySet.getItems())
				if (item instanceof XYDataItem
						&& ((XYDataItem) item).getY() != null)
					memoryAvg += ((XYDataItem) item).getY().doubleValue();
			requestAvg /= requestSet.getItemCount();
			memoryAvg /= afterMemorySet.getItemCount();

			// Convert XY data into XY Datasets and discard the temporary data.
			LOG.debug("  -> Convert types.");
			DefaultTableXYDataset requestData = new DefaultTableXYDataset();
			DefaultTableXYDataset timingData = new DefaultTableXYDataset();
			DefaultTableXYDataset memoryData = new DefaultTableXYDataset();
			requestData.addSeries(requestSet);
			memoryData.addSeries(afterMemorySet);
			for (XYSeries timingSeries : timingSet.values())
				timingData.addSeries(timingSeries);
			requestSet = afterMemorySet = null;
			timingSet = null;

			// Driver Charts.
			LOG.debug("  -> Driver Charts.");
			DateAxis timeAxis = new DateAxis("Time");
			NumberAxis timingAxis = new NumberAxis("Time Elapsed (ms)");
			NumberAxis memoryAxis = new NumberAxis("Available Memory (bytes)");
			NumberAxis errorAxis = new NumberAxis("Exceptions");

			CombinedDomainXYPlot timingAndMemoryPlot = new CombinedDomainXYPlot(
					timeAxis);
			if (memoryData.getItemCount() > 0) {
				XYPlot memoryPlot = new XYPlot(memoryData, timeAxis,
						memoryAxis, new XYLineAndShapeRenderer(true, false));

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
			driverCharts.add(getImage(statisticsChart));

			if (errorsSet.getRowCount() > 0) {
				CategoryPlot errorsPlot = new CategoryPlot(errorsSet,
						new CategoryAxis("Occurance"), errorAxis,
						new BarRenderer());

				JFreeChart errorsChart = new JFreeChart("Exceptions:",
						errorsPlot);
				driverCharts.add(getImage(errorsChart, 150));
			}
		}

		// Allow GCing.
		profiles = null;

		// Create the scenario speed data.
		LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		LOG.debug("Scenario Speed.");
		List<TimeSeriesCollection> scenarioSpeedSets = new ArrayList<TimeSeriesCollection>();
		SortedSet<AgentTimeEntity> agentTimes = this.executionService
				.getExecutionTimes(execution);
		if (agentTimes.isEmpty())
			LOG.debug("No scenario start times available.");

		else
			// Calculate moving averages from the scenario starts for 2 periods.
			for (int period : MOVING_AVERAGE_PERIODS) {
				TimeSeries timeSeries = new TimeSeries("Period: " + period
						/ 1000 + "s", FixedMillisecond.class);
				scenarioSpeedSets.add(new TimeSeriesCollection(timeSeries));

				long startTime = agentTimes.first().getStart();
				for (AgentTimeEntity agentTime : agentTimes) {

					// Skip until enough values in past for one period.
					if (agentTime.getStart() - startTime < period)
						continue;

					double count = 0;
					for (AgentTimeEntity pastTime : agentTimes) {
						if (agentTime.getStart() - pastTime.getStart() > period)
							continue; // Ignore values before period.
						if (pastTime.getStart() == agentTime.getStart())
							break; // Stop counting at current time.

						count++;
					}

					timeSeries.addOrUpdate(new FixedMillisecond(agentTime
							.getStart()), 1000 * count / period);
				}
			}

		// Create the agent memory data.
		LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		LOG.debug("Agent Memory.");
		TimeSeries endAgentMemory = new TimeSeries("After",
				FixedMillisecond.class);
		TimeSeriesCollection agentMemorySet = new TimeSeriesCollection();
		agentMemorySet.addSeries(endAgentMemory);
		if (!agentTimes.isEmpty())
			for (AgentTimeEntity agentTime : agentTimes)
				endAgentMemory.add(new FixedMillisecond(agentTime.getStart()),
						agentTime.getEndFreeMem());

		// Create moving averages off the agent memory usage.
		LOG.debug("  -> Moving Avg.");
		TimeSeriesCollection agentAverageMemory = MovingAverage
				.createMovingAverage(agentMemorySet, "; period: 60s", 60000,
						60000);

		// Create scenario execution time data.
		LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		LOG.debug("Scenario Execution Time.");
		XYSeries olasDuration = new XYSeries("OLAS", true, false);
		XYSeries agentDuration = new XYSeries("Overhead", true, false);
		DefaultTableXYDataset olasScenarioDuration = new DefaultTableXYDataset();
		if (!agentTimes.isEmpty())
			for (AgentTimeEntity agentTime : agentTimes) {
				Long time = agentTime.getStart();
				Long olas = agentTime.getOlasDuration();
				Long agent = agentTime.getAgentDuration();

				if (olas == null)
					olas = 0l;
				if (agent == null)
					agent = 0l;

				olasDuration.addOrUpdate(time, olas);
				agentDuration.addOrUpdate(time, agent - olas);
			}
		scenarioDuration.addSeries(agentDuration);
		olasScenarioDuration.addSeries(olasDuration);

		// Create moving averages off the OLAS durations.
		LOG.debug("  -> Moving Avg.");
		List<XYDataset> olasAverageDurations = new ArrayList<XYDataset>();
		for (int period : MOVING_AVERAGE_PERIODS)
			olasAverageDurations.add(MovingAverage.createMovingAverage(
					olasScenarioDuration, "; period: " + period / 1000 + "s",
					period, period));

		// Create Box-and-Whisker objects from Method Timing Data.
		LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		LOG.debug("Box & Whisker.");
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

		// Plots.
		LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		LOG.debug("Plots.");
		DateAxis timeAxis = new DateAxis("Time");
		CombinedDomainXYPlot speedPlot = new CombinedDomainXYPlot(timeAxis);
		for (TimeSeriesCollection scenarioSpeedSet : scenarioSpeedSets)
			if (scenarioSpeedSet.getItemCount(0) > 0)
				speedPlot.add(new XYPlot(scenarioSpeedSet, timeAxis,
						new NumberAxis("Speed (#/s)"),
						new XYLineAndShapeRenderer(true, false)));
		JFreeChart speedChart = new JFreeChart(speedPlot);

		timeAxis = new DateAxis("Time");
		CombinedDomainXYPlot olasAverageDurationsPlot = new CombinedDomainXYPlot(
				timeAxis);
		for (XYDataset olasAverageDuration : olasAverageDurations)
			if (olasAverageDuration.getItemCount(0) > 0)
				olasAverageDurationsPlot.add(new XYPlot(olasAverageDuration,
						timeAxis, new NumberAxis("Duration (ms)"),
						new XYLineAndShapeRenderer(true, false)));
		JFreeChart olasAverageDurationsChart = new JFreeChart(
				olasAverageDurationsPlot);

		timeAxis = new DateAxis("Time");
		valueAxis = new NumberAxis("Duration (ms)");
		XYPlot olasPlot = new XYPlot();
		olasPlot.setDataset(olasScenarioDuration);
		olasPlot.setDomainAxis(timeAxis);
		olasPlot.setRangeAxis(valueAxis);
		olasPlot.setRenderer(new XYLineAndShapeRenderer(true, false));
		JFreeChart olasChart = new JFreeChart(olasPlot);

		timeAxis = new DateAxis("Time");
		valueAxis = new NumberAxis("Duration (ms)");
		XYPlot durationPlot = new XYPlot();
		durationPlot.setDataset(scenarioDuration);
		durationPlot.setDomainAxis(timeAxis);
		durationPlot.setRangeAxis(valueAxis);
		durationPlot.setRenderer(new StackedXYAreaRenderer2());
		JFreeChart durationChart = new JFreeChart(durationPlot);

		timeAxis = new DateAxis("Time");
		valueAxis = new NumberAxis("Available Memory (bytes)");
		XYPlot agentMemoryPlot = new XYPlot(agentAverageMemory, timeAxis,
				valueAxis, new XYLineAndShapeRenderer(true, false));
		JFreeChart agentMemoryChart = new JFreeChart(agentMemoryPlot);

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
		LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		LOG.debug("Charts.");
		Map<String, byte[][]> charts = new LinkedHashMap<String, byte[][]>();
		charts.put("Scenario Duration: OLAS",
				new byte[][] { getImage(olasChart) });
		charts.put("Scenario Duration: OLAS -- Moving Average",
				new byte[][] { getImage(olasAverageDurationsChart) });
		charts.put("Scenario Duration: AGENT",
				new byte[][] { getImage(durationChart) });
		charts.put("Scenario Memory: AGENT",
				new byte[][] { getImage(agentMemoryChart) });
		charts.put("Scenario Execution Speed",
				new byte[][] { getImage(speedChart) });
		charts.put("Request Duration per Driver",
				new byte[][] { getImage(requestChart) });
		charts.put("Method Duration per Driver",
				new byte[][] { getImage(methodChart) });
		for (Map.Entry<String, List<byte[]>> driverCharts : driversCharts
				.entrySet())
			charts.put(driverCharts.getKey(), driverCharts.getValue().toArray(
					new byte[0][0]));

		LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		LOG.debug("All done.");

		return charts;
	}

	private Long getMeasurement(Set<MeasurementEntity> measurements, String type)
			throws NoSuchElementException {

		for (MeasurementEntity e : measurements)
			if (type.equals(e.getMeasurement()))
				return e.getDuration();

		throw new NoSuchElementException("Element " + type
				+ " could not be found.");
	}

	private byte[] getImage(JFreeChart chart) {

		return getImage(chart, DATA_POINTS);
	}

	private byte[] getImage(JFreeChart chart, int height) {

		try {
			chart.setBackgroundPaint(Color.white);
			return encoder.encode(chart
					.createBufferedImage(DATA_POINTS, height));
		} catch (IOException e) {
			return null;
		}
	}

	private long getFreeMemory() {

		try {
			return (Long) rmi.getAttribute(new ObjectName(
					"jboss.system:type=ServerInfo"), "FreeMemory");
		} catch (Exception e) {
			LOG.error("Failed to read in free memory through JMX.", e);
		}

		return -1;
	}

	private static InitialContext getInitialContext() throws NamingException {

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "localhost:1099");

		return new InitialContext(environment);
	}
}
