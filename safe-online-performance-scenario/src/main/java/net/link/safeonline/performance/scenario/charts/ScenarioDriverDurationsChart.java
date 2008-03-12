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

import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.util.performance.ProfileData;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * <h2>{@link ScenarioDriverDurationsChart}<br>
 * <sub>A chart module that renders a detail of driver activity.</sub></h2>
 *
 * <p>
 * </p>
 *
 * <p>
 * <i>Mar 4, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioDriverDurationsChart extends AbstractChart {

	private Map<String, Map<String, XYSeries>> driverMaps;

	/**
	 * Create a new {@link ScenarioDriverDurationsChart} instance.
	 */
	public ScenarioDriverDurationsChart() {

		super("Scenario Driver Duration");

		this.driverMaps = new HashMap<String, Map<String, XYSeries>>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processData(ProfileDataEntity data) {

		// Process all method (non-request) measurements.
		long sum_methodTime = 0;
		for (MeasurementEntity measurement : data.getMeasurements()) {
			if (ProfileData.isRequestKey(measurement.getMeasurement()))
				continue;

			XYSeries measurementSet = getMeasurementSet(measurement);

			Long startTime = data.getScenarioTiming().getStart();
			Long duration = measurement.getDuration();
			sum_methodTime += duration;

			measurementSet.addOrUpdate(startTime, duration);
		}

		// Process the request time measurement.
		// Subtract total time spent in method measurements.
		for (MeasurementEntity measurement : data.getMeasurements())
			if (ProfileData.REQUEST_DELTA_TIME.equals(measurement
					.getMeasurement())) {
				XYSeries measurementSet = getMeasurementSet(measurement);

				Long startTime = data.getScenarioTiming().getStart();
				Long duration = measurement.getDuration();
				long overhead = duration - sum_methodTime;

				if (overhead > 0)
					measurementSet.addOrUpdate(startTime, overhead);

				break;
			}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDataProcessed() {

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XYPlot getPlot() {

		if (isEmpty())
			return null;

		ValueAxis domainAxis = getAxis();
		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);

		for (Map.Entry<String, Map<String, XYSeries>> driverSet : this.driverMaps
				.entrySet()) {
			NumberAxis valueAxis = new NumberAxis(driverSet.getKey() + " (ms)");
			DefaultTableXYDataset driverMeasurements = new DefaultTableXYDataset();

			for (XYSeries measurements : driverSet.getValue().values())
				driverMeasurements.addSeries(measurements);

			plot.add(new XYPlot(driverMeasurements, domainAxis, valueAxis,
					new StackedXYBarRenderer()));
		}

		return plot;
	}

	private boolean isEmpty() {

		for (Map<String, XYSeries> data : this.driverMaps.values())
			if (!isEmpty(data))
				return false;

		return true;
	}

	private XYSeries getMeasurementSet(MeasurementEntity measurement) {

		String profile = measurement.getProfileData().getProfile()
				.getDriverName();

		Map<String, XYSeries> driverMap = this.driverMaps.get(profile);
		if (driverMap == null)
			this.driverMaps.put(profile,
					driverMap = new HashMap<String, XYSeries>());

		XYSeries measurementSet = driverMap.get(measurement.getMeasurement());
		if (measurementSet == null)
			driverMap.put(measurement.getMeasurement(),
					measurementSet = new XYSeries(measurement.getMeasurement(),
							true, false));

		return measurementSet;
	}
}
