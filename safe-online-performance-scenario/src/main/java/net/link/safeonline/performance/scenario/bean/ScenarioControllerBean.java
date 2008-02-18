/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
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

	private static final Log LOG = LogFactory.getLog(ScenarioControllerBean.class);

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

	@Resource(name = "activeScenario")
	private String activeScenario;

	/**
	 * {@inheritDoc}
	 */
	@TransactionTimeout(SCENARIO_EXECUTION_TIMEOUT)
	public void execute(int executionId) throws Exception {

		ExecutionEntity execution = this.executionService
				.getExecution(executionId);
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
	public int prepare(ExecutionMetadata metaData) {

		// Start 'activeScenario' by default, unless a specific scenario has
		// been requested in the request metadata.
		if (metaData.getScenarioName() == null)
			metaData.setScenarioName(this.activeScenario);

		// Create the execution and fill it up with metadata.
		ExecutionEntity execution = this.executionService.addExecution(metaData
				.getScenarioName(), metaData.getAgents(),
				metaData.getWorkers(), metaData.getStartTime(), metaData
						.getDuration(), metaData.getHostname());
		createScenario(execution.getScenarioName()).prepare(execution, null);

		return execution.getId();
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
	public Set<Integer> getExecutions() {

		return this.executionService.getExecutions();
	}

	/**
	 * {@inheritDoc}
	 */
	public ExecutionMetadata getExecutionMetadata(int executionId) {

		ExecutionEntity execution = this.executionService
				.getExecution(executionId);

		return ExecutionMetadata.createResponse(execution.getId(), execution
				.getScenarioName(),
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
	public Map<String, byte[][]> createCharts(int executionId) {

		// ExecutionEntity execution = this.ctx.getBusinessObject(
		// ScenarioController.class).loadExecution(executionId);
		LOG.debug("START:");
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
			Set<ProfileDataEntity> profileData = this.profileDataService
					.getProfileData(profile);

			// Invalid / empty driver profiles.
			if (profile.getDriverName() == null
					|| profileData.isEmpty()) {
				LOG.warn("Invalid/empty driver profile: '"
						+ profile.getDriverName() + "'!");
				continue;
			}

			// Dataset for a Bar Chart of method timings per iteration.
			Map<String, List<Long>> driverMethods = new HashMap<String, List<Long>>();
			driversMethods.put(profile.getDriverName(), driverMethods);
			Map<String, XYSeries> timingSet = new LinkedHashMap<String, XYSeries>();
			DefaultCategoryDataset errorsSet = new DefaultCategoryDataset();
			XYSeries requestSet = new XYSeries("Request Time", true, false);
			XYSeries afterMemorySet = new XYSeries("Memory After", true, false);
			XYSeries beforeMemorySet = new XYSeries("Memory Before", true,
					false);

			for (ProfileDataEntity data : profileData) {

				if (data.getMeasurements() == null || data.getStartTime() == 0)
					continue;

				// Increment startTime as long as there are other X values in
				// requestSet with the same value.
				long startTime = data.getStartTime();
				for (boolean search = true; search;) {
					search = false;

					for (Object item : requestSet.getItems())
						if (item instanceof XYDataItem)
							if (((XYDataItem) item).getX().equals(startTime)) {
								startTime++;
								search = true;
								break;
							}
				}

				// Process the statistics.
				long requestTime = data
						.getMeasurement(ProfileData.REQUEST_DELTA_TIME);
				long beforeMemory = data
						.getMeasurement(ProfileData.REQUEST_START_FREE);
				long afterMemory = data
						.getMeasurement(ProfileData.REQUEST_END_FREE);

				try {
					beforeMemorySet.add(startTime, beforeMemory);
					afterMemorySet.add(startTime, afterMemory);
					requestSet.add(startTime, requestTime);
				} catch (SeriesException e) {
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

						timingSet.get(method).add(startTime, timing);
					}

					// Collect Method Timing Chart Data.
					if (!driverMethods.containsKey(method))
						driverMethods.put(method, new ArrayList<Long>());
					driverMethods.get(method).add(timing);
				}
			}

			for (DriverExceptionEntity error : this.driverExceptionService
					.getProfileErrors(profile))
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
			memoryData.addSeries(afterMemorySet);
			memoryData.addSeries(beforeMemorySet);
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
		List<TimeSeriesCollection> scenarioSpeedSets = new ArrayList<TimeSeriesCollection>();
		SortedSet<AgentTimeEntity> agentTimes = this.executionService
				.getExecutionTimes(execution);
		if (agentTimes.isEmpty())
			LOG.warn("No scenario start times available.");

		else
			// Calculate moving averages from the scenario starts for 2 periods.
			for (int period : MOVING_AVERAGE_PERIODS) {
				TimeSeries timeSeries = new TimeSeries("Period: " + period
						/ 1000 + "s", FixedMillisecond.class);
				scenarioSpeedSets.add(new TimeSeriesCollection(timeSeries));

				for (AgentTimeEntity agentTime : agentTimes) {
					SortedSet<AgentTimeEntity> futureTimes = agentTimes
							.tailSet(agentTime);

					double count = 0;
					for (AgentTimeEntity futureTime : futureTimes) {
						if (futureTime.getStart() > agentTime.getStart()
								+ period)
							break;

						count++;
					}

					timeSeries.addOrUpdate(new FixedMillisecond(agentTime
							.getStart()), 1000 * count / period);
				}
			}

		// Create the agent memory data.
		TimeSeries startAgentMemory = new TimeSeries("Before",
				FixedMillisecond.class);
		TimeSeries endAgentMemory = new TimeSeries("After",
				FixedMillisecond.class);
		TimeSeriesCollection agentMemorySet = new TimeSeriesCollection();
		// agentMemorySet.addSeries(startAgentMemory);
		agentMemorySet.addSeries(endAgentMemory);
		if (!agentTimes.isEmpty())
			for (AgentTimeEntity agentTime : agentTimes) {
				startAgentMemory.add(
						new FixedMillisecond(agentTime.getStart()), agentTime
								.getStartFreeMem());
				endAgentMemory.add(new FixedMillisecond(agentTime.getStart()),
						agentTime.getEndFreeMem());
			}

		// Create moving averages off the agent memory usage.
		TimeSeriesCollection agentAverageMemory = MovingAverage
				.createMovingAverage(agentMemorySet, "; period: 60s", 60000,
						60000);

		// Create scenario execution time data.
		XYSeries olasDuration = new XYSeries("OLAS", true, false);
		XYSeries agentDuration = new XYSeries("Overhead", true, false);
		DefaultTableXYDataset scenarioDuration = new DefaultTableXYDataset();
		DefaultTableXYDataset olasScenarioDuration = new DefaultTableXYDataset();
		if (!agentTimes.isEmpty()) {
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

			for (DriverProfileEntity profile : execution.getProfiles()) {
				XYSeries profileDuration = new XYSeries(
						profile.getDriverName(), true, false);
				scenarioDuration.addSeries(profileDuration);

				for (ProfileDataEntity data : this.profileDataService
						.getProfileData(profile)) {
					long driverDuration = data
							.getMeasurement(ProfileData.REQUEST_DELTA_TIME);

					profileDuration.addOrUpdate(data.getScenarioStart(),
							driverDuration);
				}
			}
		}
		scenarioDuration.addSeries(agentDuration);
		olasScenarioDuration.addSeries(olasDuration);

		// Create moving averages off the OLAS durations.
		List<XYDataset> olasAverageDurations = new ArrayList<XYDataset>();
		for (int period : MOVING_AVERAGE_PERIODS)
			olasAverageDurations.add(MovingAverage.createMovingAverage(
					olasScenarioDuration, "; period: " + period / 1000 + "s",
					period, period));

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
		Map<String, byte[][]> charts = new LinkedHashMap<String, byte[][]>();
		charts.put("Scenario Duration: OLAS", new byte[][] { getImage(
				olasChart, 1000, 1000) });
		charts
				.put("Scenario Duration: OLAS -- Moving Average",
						new byte[][] { getImage(olasAverageDurationsChart,
								1000, 1000) });
		charts.put("Scenario Duration: AGENT", new byte[][] { getImage(
				durationChart, 1000, 1000) });
		charts.put("Scenario Memory: AGENT", new byte[][] { getImage(
				agentMemoryChart, 1000, 1000) });
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

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ExecutionEntity loadExecution(int executionId) {

		return loadEntity(this.executionService.getExecution(executionId),
				new HashSet<Object>());
	}

	/**
	 * Retrieve and fully load all fields of the given object.
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private <E> E loadEntity(E entity, Set<Object> cache) {

		if (entity == null
				|| !(entity.getClass().getName().startsWith("net.link") || entity instanceof Collection)
				|| cache.contains(entity))
			return entity;

		if (entity instanceof Collection)
			for (Object entry : (Collection<?>) entity)
				loadMethods(entry, cache);
		else
			loadMethods(entity, cache);

		return entity;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private void loadMethods(Object entity, Set<Object> cache) {

		for (Method method : entity.getClass().getMethods())
			try {
				// Only proceed for getters with no parameters.
				if (!method.getName().startsWith("get")
						|| method.getParameterTypes().length > 0)
					continue;

				method.setAccessible(true);
				cache.add(loadEntity(method.invoke(entity), cache));
			}

			catch (IllegalArgumentException e) {
				LOG.error(e);
			} catch (IllegalAccessException e) {
				LOG.error(e);
			} catch (InvocationTargetException e) {
				LOG.error(e);
			}
	}

	private static byte[] getImage(JFreeChart chart, int width, int height) {

		try {
			chart.setBackgroundPaint(Color.white);
			return encoder.encode(chart.createBufferedImage(width, height));
		} catch (IOException e) {
			return null;
		}
	}

	private static long getFreeMemory() {

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
