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

import net.link.safeonline.performance.entity.ProfileDataEntity;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 * <h2>{@link ScenarioCorrelationChart}<br>
 * <sub>TODO</sub></h2>
 *
 * <p>
 * </p>
 *
 * <p>
 * <i>Mar 1, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioCorrelationChart extends AbstractChart {

	private TimeSeries correlation;

	/**
	 * Create a new {@link ScenarioCorrelationChart} instance.
	 */
	public ScenarioCorrelationChart() {

		super("Scenario Performance Correlation");

		this.correlation = new TimeSeries("Speed", FixedMillisecond.class);
	}

	/**
	 * TODO
	 *
	 * {@inheritDoc}
	 */
	public void process(ProfileDataEntity data) {

		Long startTime = data.getScenarioTiming().getStart();

		this.correlation.addOrUpdate(new FixedMillisecond(startTime), 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XYPlot getPlot() {

		DateAxis timeAxis = new DateAxis("Time");

		XYDataset correlationSet = new TimeSeriesCollection(this.correlation);

		return new XYPlot(correlationSet, timeAxis, new NumberAxis(
				"Correlation (1 <> -1)"), new XYLineAndShapeRenderer(true,
				false));
	}
}
