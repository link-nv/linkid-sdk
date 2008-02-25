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

import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.util.performance.ProfileData;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * <h2>{@link OLASTimeChart}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Feb 22, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class OLASTimeChart extends AbstractChart {

	private Map<String, XYSeries> driverSets;
	private XYSeries overhead;

	/**
	 * Create a new {@link OLASTimeChart} instance.
	 */
	public OLASTimeChart() {

		super("Scenario Duration");

		this.driverSets = new HashMap<String, XYSeries>();
		this.overhead = new XYSeries("Agent Overhead", true, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process(DriverProfileEntity profile, ProfileDataEntity data) {

		XYSeries driverSet = getDriverSet(profile);

		Long startTime = data.getScenarioTiming().getStart();
		Long agentTime = data.getScenarioTiming().getAgentDuration();
		Long requestTime = getMeasurement(data.getMeasurements(),
				ProfileData.REQUEST_DELTA_TIME);

		driverSet.addOrUpdate(startTime, requestTime);
		this.overhead.addOrUpdate(startTime, agentTime - requestTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[][] render(int dataPoints) {

		DateAxis timeAxis = new DateAxis("Time");
		NumberAxis valueAxis = new NumberAxis("Duration (ms)");

		DefaultTableXYDataset scenarioDuration = new DefaultTableXYDataset();
		for (XYSeries driverSet : this.driverSets.values())
			scenarioDuration.addSeries(driverSet);

		XYPlot durationPlot = new XYPlot();
		durationPlot.setDataset(scenarioDuration);
		durationPlot.setDomainAxis(timeAxis);
		durationPlot.setRangeAxis(valueAxis);
		durationPlot.setRenderer(new StackedXYAreaRenderer2());

		JFreeChart durationChart = new JFreeChart(durationPlot);

		return new byte[][] { getImage(durationChart, dataPoints) };
	}

	private XYSeries getDriverSet(DriverProfileEntity profile) {

		XYSeries driverSet = this.driverSets.get(profile.getDriverName());
		if (driverSet == null)
			this.driverSets.put(profile.getDriverName(),
					driverSet = new XYSeries(profile.getDriverName(), true,
							false));

		return driverSet;
	}
}
