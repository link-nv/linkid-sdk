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

import java.util.LinkedList;
import java.util.Queue;

import net.link.safeonline.performance.entity.ProfileDataEntity;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * <h2>{@link ScenarioSpeedChart}<br>
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
public class ScenarioSpeedChart extends AbstractChart {

	private TimeSeries speed;
	private Queue<ProfileDataEntity> speedData;
	private long period;

	/**
	 * Create a new {@link ScenarioSpeedChart} instance.
	 */
	public ScenarioSpeedChart(long period) {

		super("Scenario Speed");

		this.period = period;
		this.speed = new TimeSeries("Period: " + period + "ms");
		this.speedData = new LinkedList<ProfileDataEntity>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void process(ProfileDataEntity data) {

		Long currentTime = data.getScenarioTiming().getStart();
		this.speedData.offer(data);

		// Poll off outdated data (more than a period old).
		Long baseTime;
		while (true) {
			baseTime = this.speedData.peek().getScenarioTiming().getStart();
			if (currentTime - this.period <= baseTime)
				break;

			this.speedData.poll();
		}

		// Multiply hits by 1000 and divide by period to obtain hits/s.
		this.speed.addOrUpdate(new FixedMillisecond(baseTime), 1000d
				* this.speedData.size() / this.period);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[][] render(int dataPoints) {

		DateAxis timeAxis = new DateAxis("Time");

		TimeSeriesCollection speedSet;
		speedSet = new TimeSeriesCollection(this.speed);

		XYPlot speedPlot = new XYPlot(speedSet, timeAxis, new NumberAxis(
				"Speed (#/s)"), new XYLineAndShapeRenderer(true, false));

		JFreeChart speedChart = new JFreeChart(speedPlot);
		return new byte[][] { getImage(speedChart, dataPoints) };
	}
}
