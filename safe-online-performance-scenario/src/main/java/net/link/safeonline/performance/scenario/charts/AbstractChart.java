/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.scenario.charts;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.util.performance.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * <h2>{@link AbstractChart}<br>
 * <sub>The basis of chart generators.</sub></h2>
 *
 * <p>
 * This class implements several helper methods that will be very convenient in
 * generating and rendering charts.
 * </p>
 *
 * <p>
 * <i>Feb 22, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public abstract class AbstractChart implements Chart {

	final Log LOG = LogFactory.getLog(getClass());

	private static DateFormat timeFormat = DateFormat.getTimeInstance();
	private static ImageEncoder encoder = ImageEncoderFactory.newInstance(
			"png", 0.9f, true);

	private final ExecutionService executionService = getService(
			ExecutionService.class, ExecutionService.BINDING);
	private final ProfileDataService profileDataService = getService(
			ProfileDataService.class, ProfileDataService.BINDING);
	private final DriverExceptionService driverExceptionService = getService(
			DriverExceptionService.class, DriverExceptionService.BINDING);

	protected String title;
	private boolean linked;
	private List<AbstractChart> links;

	public AbstractChart(String title) {

		this.title = title;
		this.linked = false;
		this.links = new ArrayList<AbstractChart>();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTitle() {

		return this.title;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[][] render(int dataPoints) {

		// Don't render when linked.
		if (this.linked)
			return null;

		XYPlot plot = getPlot();
		if (!this.links.isEmpty()) {
			XYPlot basePlot = plot;
			CombinedDomainXYPlot combinedPlot;

			plot = combinedPlot = new CombinedDomainXYPlot(basePlot
					.getDomainAxis());

			combinedPlot.add(basePlot);

			for (AbstractChart link : this.links)
				combinedPlot.add(link.getPlot());
		}

		else {
			XYDataset set = plot.getDataset();
			if (set != null)
				for (int i = 0; i < set.getSeriesCount(); ++i) {
					double sum = 0;
					for (int j = 0; j < set.getItemCount(i); ++j)
						sum += set.getYValue(i, j);

					ValueMarker marker = new ValueMarker(sum
							/ set.getItemCount(i));
					marker.setLabel("Average " + i + "                ");
					marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
					plot.addRangeMarker(marker);
				}
		}

		JFreeChart chart = new JFreeChart(plot);
		return new byte[][] { getImage(chart, dataPoints) };
	}

	/**
	 * Implement this method to generate the plot that depicts the chart your
	 * module generates.
	 */
	protected abstract XYPlot getPlot();

	/**
	 * {@inheritDoc}
	 */
	public void processData(ProfileDataEntity data) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void processError(DriverExceptionEntity error) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void processTiming(ScenarioTimingEntity data) {
	}

	/**
	 * Link this plot with the given other plots.<br>
	 * <br>
	 * The other plots will all use the domain axis of this plot and will not
	 * generate a chart of their own. A single chart will be generated for all
	 * plots linked to this one.<br>
	 * <br>
	 * <b>NOTE: Do not chain link plots, links do not work recursively. All
	 * plots beyond the first level of the chain will no longer be visible in
	 * the charts.</b>
	 */
	public void linkWith(AbstractChart... charts) {

		for (AbstractChart chart : charts) {
			chart.isLinked();
			this.links.add(chart);
		}
	}

	private void isLinked() {

		this.linked = true;
	}

	protected Long getMeasurement(Set<MeasurementEntity> measurements,
			String type) throws NoSuchElementException {

		for (MeasurementEntity e : measurements)
			if (type.equals(e.getMeasurement()))
				return e.getDuration();

		throw new NoSuchElementException("Element " + type
				+ " could not be found.");
	}

	protected byte[] getImage(JFreeChart chart, int width) {

		return getImage(chart, width, width);
	}

	protected byte[] getImage(JFreeChart chart, int width, int height) {

		try {
			chart.setBackgroundPaint(Color.white);
			return encoder.encode(chart.createBufferedImage(width, height));
		} catch (IOException e) {
			return null;
		}
	}

	<S> S getService(Class<S> service, String binding) {

		try {
			InitialContext initialContext = new InitialContext();
			return service.cast(initialContext.lookup(binding));
		} catch (NoInitialContextException e) {
			this.LOG.warn("Initial context not set up; "
					+ "assuming we're not running in an "
					+ "enterprise container");

			try {
				return service.cast(Class.forName(
						service.getName().replaceFirst("\\.([^\\.]*)$",
								".bean.$1Bean")).newInstance());
			} catch (InstantiationException ee) {
				this.LOG.error("Couldn't create service " + service + " at "
						+ binding, ee);
				throw new RuntimeException(ee);
			} catch (IllegalAccessException ee) {
				this.LOG.error("Couldn't access service " + service + " at "
						+ binding, ee);
				throw new RuntimeException(ee);
			} catch (ClassNotFoundException ee) {
				this.LOG.error("Couldn't find service "
						+ service.getName().replaceFirst("\\.([^\\.]*)$",
								".bean.$1Bean") + " at " + binding, ee);
				throw new RuntimeException(ee);
			}
		} catch (NamingException e) {
			this.LOG.error("Couldn't find service " + service + " at "
					+ binding, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Map<String, byte[][]> _createCharts(Date executionStartTime,
			int dataPoints) {

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
		this.LOG.debug("BUILDING CHARTS FOR " + profiles.size() + " DRIVERS:");
		Map<String, List<byte[]>> driversCharts = new LinkedHashMap<String, List<byte[]>>(
				profiles.size());
		int _profile = 0;
		long start = System.currentTimeMillis();
		for (DriverProfileEntity profile : profiles) {
			this.LOG.debug("Took " + (System.currentTimeMillis() - start)
					/ 1000f + " seconds");
			start = System.currentTimeMillis();
			this.LOG.debug("Profile: " + ++_profile + " / " + profiles.size()
					+ ": " + profile.getDriverName());

			List<ProfileDataEntity> profileData = this.profileDataService
					.getProfileData(profile, dataPoints);

			// Invalid / empty driver profiles.
			if (profile.getDriverName() == null || profileData.isEmpty()) {
				this.LOG.debug("Invalid/empty driver profile: '"
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
					this.LOG.debug("  -> Data: " + ++_data + " / "
							+ profileData.size());
					Set<MeasurementEntity> measurements = data
							.getMeasurements();

					// Process the statistics.
					long startTime = data.getScenarioTiming().getStart();
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
					this.LOG.debug("No start time found!");
				}

			for (DriverExceptionEntity error : this.driverExceptionService
					.getAllProfileErrors(profile))
				errorsSet.addValue((Number) 1, error.getMessage(), timeFormat
						.format(error.getOccurredTime()));

			// Calculate averages.
			this.LOG.debug("  -> Averages.");
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
			this.LOG.debug("  -> Convert types.");
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
			this.LOG.debug("  -> Driver Charts.");
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
			driverCharts.add(getImage(statisticsChart, dataPoints));

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
		this.LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		this.LOG.debug("Scenario Speed.");
		List<TimeSeriesCollection> scenarioSpeedSets = new ArrayList<TimeSeriesCollection>();
		SortedSet<ScenarioTimingEntity> agentTimes = new TreeSet<ScenarioTimingEntity>();
		// this.executionService .getExecutionTimes(execution);
		if (agentTimes.isEmpty())
			this.LOG.debug("No scenario start times available.");

		else
			// Calculate moving averages from the scenario starts for 2 periods.
			for (int period : new int[] { 3600000 }) {
				TimeSeries timeSeries = new TimeSeries("Period: " + period
						/ 1000 + "s", FixedMillisecond.class);
				scenarioSpeedSets.add(new TimeSeriesCollection(timeSeries));

				long startTime = agentTimes.first().getStart();
				for (ScenarioTimingEntity agentTime : agentTimes) {

					// Skip until enough values in past for one period.
					if (agentTime.getStart() - startTime < period)
						continue;

					double count = 0;
					for (ScenarioTimingEntity pastTime : agentTimes) {
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
		this.LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		this.LOG.debug("Agent Memory.");
		TimeSeries endAgentMemory = new TimeSeries("After",
				FixedMillisecond.class);
		TimeSeriesCollection agentMemorySet = new TimeSeriesCollection();
		agentMemorySet.addSeries(endAgentMemory);
		if (!agentTimes.isEmpty())
			for (ScenarioTimingEntity agentTime : agentTimes)
				endAgentMemory.add(new FixedMillisecond(agentTime.getStart()),
						agentTime.getEndFreeMem());

		// Create moving averages off the agent memory usage.
		this.LOG.debug("  -> Moving Avg.");
		TimeSeriesCollection agentAverageMemory = MovingAverage
				.createMovingAverage(agentMemorySet, "; period: 60s", 60000,
						60000);

		// Create scenario execution time data.
		this.LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		this.LOG.debug("Scenario Execution Time.");
		XYSeries olasDuration = new XYSeries("OLAS", true, false);
		XYSeries agentDuration = new XYSeries("Overhead", true, false);
		DefaultTableXYDataset olasScenarioDuration = new DefaultTableXYDataset();
		if (!agentTimes.isEmpty())
			for (ScenarioTimingEntity agentTime : agentTimes) {
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
		this.LOG.debug("  -> Moving Avg.");
		List<XYDataset> olasAverageDurations = new ArrayList<XYDataset>();
		for (int period : new int[] { 3600000 })
			olasAverageDurations.add(MovingAverage.createMovingAverage(
					olasScenarioDuration, "; period: " + period / 1000 + "s",
					period, period));

		// Create Box-and-Whisker objects from Method Timing Data.
		this.LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		this.LOG.debug("Box & Whisker.");
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
		this.LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		this.LOG.debug("Plots.");
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
		this.LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		start = System.currentTimeMillis();
		this.LOG.debug("Charts.");
		Map<String, byte[][]> charts = new LinkedHashMap<String, byte[][]>();
		charts.put("Scenario Duration: OLAS", new byte[][] { getImage(
				olasChart, dataPoints) });
		charts
				.put("Scenario Duration: OLAS -- Moving Average",
						new byte[][] { getImage(olasAverageDurationsChart,
								dataPoints) });
		charts.put("Scenario Duration: AGENT", new byte[][] { getImage(
				durationChart, dataPoints) });
		charts.put("Scenario Memory: AGENT", new byte[][] { getImage(
				agentMemoryChart, dataPoints) });
		charts.put("Scenario Execution Speed", new byte[][] { getImage(
				speedChart, dataPoints) });
		charts.put("Request Duration per Driver", new byte[][] { getImage(
				requestChart, dataPoints) });
		charts.put("Method Duration per Driver", new byte[][] { getImage(
				methodChart, dataPoints) });
		for (Map.Entry<String, List<byte[]>> driverCharts : driversCharts
				.entrySet())
			charts.put(driverCharts.getKey(), driverCharts.getValue().toArray(
					new byte[0][0]));

		this.LOG.debug("Took " + (System.currentTimeMillis() - start) / 1000f
				+ " seconds");
		this.LOG.debug("All done.");

		return charts;
	}
}
