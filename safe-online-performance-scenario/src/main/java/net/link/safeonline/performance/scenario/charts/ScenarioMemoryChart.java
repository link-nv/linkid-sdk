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
import net.link.safeonline.util.performance.ProfileData;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * <h2>{@link ScenarioMemoryChart}<br>
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
public class ScenarioMemoryChart extends AbstractChart {

	private TimeSeries olasMemory;
	private TimeSeries agentMemory;

	/**
	 * Create a new {@link ScenarioMemoryChart} instance.
	 */
	public ScenarioMemoryChart() {

		super("Scenario Memory");

		this.olasMemory = new TimeSeries("OLAS", FixedMillisecond.class);
		this.agentMemory = new TimeSeries("Agent", FixedMillisecond.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processData(ProfileDataEntity data) {

		FixedMillisecond startTime = new FixedMillisecond(data
				.getScenarioTiming().getStart());

		Long agentMem = data.getScenarioTiming().getStartFreeMem();
		Long olasMem = getMeasurement(data.getMeasurements(),
				ProfileData.REQUEST_START_FREE);

		this.olasMemory.addOrUpdate(startTime, olasMem);
		this.agentMemory.addOrUpdate(startTime, agentMem);
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

		DateAxis timeAxis = new DateAxis("Time");

		TimeSeriesCollection olasSet, agentSet;
		olasSet = new TimeSeriesCollection(this.olasMemory);
		agentSet = new TimeSeriesCollection(this.agentMemory);

		XYPlot olasPlot = new XYPlot(olasSet, timeAxis, new NumberAxis(
				"Available Memory (bytes)"), new XYLineAndShapeRenderer(true,
				false));
		XYPlot agentPlot = new XYPlot(agentSet, timeAxis, new NumberAxis(
				"Available Memory (bytes)"), new XYLineAndShapeRenderer(true,
				false));

		CombinedDomainXYPlot memoryPlot = new CombinedDomainXYPlot(timeAxis);
		memoryPlot.add(olasPlot);
		memoryPlot.add(agentPlot);

		return memoryPlot;
	}
}
