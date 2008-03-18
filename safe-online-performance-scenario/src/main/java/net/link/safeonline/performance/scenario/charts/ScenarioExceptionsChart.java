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

import net.link.safeonline.performance.entity.DriverExceptionEntity;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * <h2>{@link ScenarioExceptionsChart}<br>
 * <sub>TODO</sub></h2>
 *
 * <p>
 * </p>
 *
 * <p>
 * <i>Feb 22, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioExceptionsChart extends AbstractChart {

	private Map<String, Map<String, TimeSeries>> errorMaps;

	/**
	 * Create a new {@link ScenarioExceptionsChart} instance.
	 */
	public ScenarioExceptionsChart() {

		super("Scenario Errors");

		this.errorMaps = new HashMap<String, Map<String, TimeSeries>>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processError(DriverExceptionEntity error) {

		TimeSeries errorSet = getErrorSet(error);

		FixedMillisecond startTime = new FixedMillisecond(error
				.getOccurredTime());

		errorSet.addOrUpdate(startTime, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isErrorProcessed() {

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XYPlot getPlot() {

		if (isEmpty())
			return null;

		ValueAxis domainAxis = new DateAxis("Time");
		CombinedDomainXYPlot errorPlot = new CombinedDomainXYPlot(domainAxis);

		for (Map.Entry<String, Map<String, TimeSeries>> driver : this.errorMaps
				.entrySet()) {

			TimeSeriesCollection errorCollection = new TimeSeriesCollection();
			for (TimeSeries errorSet : driver.getValue().values())
				errorCollection.addSeries(errorSet);

			errorPlot.add(new XYPlot(errorCollection, null, new NumberAxis(
					driver.getKey() + " (errors)"), new XYBarRenderer()));
		}

		return errorPlot;
	}

	private boolean isEmpty() {

		for (Map<String, TimeSeries> data : this.errorMaps.values())
			if (!isEmpty(data))
				return false;

		return true;
	}

	private TimeSeries getErrorSet(DriverExceptionEntity error) {

		String profile = error.getProfile().getDriverName();

		Map<String, TimeSeries> driverMap = this.errorMaps.get(profile);
		if (driverMap == null)
			this.errorMaps.put(profile,
					driverMap = new HashMap<String, TimeSeries>());

		TimeSeries errorSet = driverMap.get(error.getMessage());
		if (errorSet == null)
			driverMap.put(error.getMessage(), errorSet = new TimeSeries(error
					.getMessage(), FixedMillisecond.class));

		return errorSet;
	}
}
