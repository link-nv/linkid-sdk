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

import net.link.safeonline.performance.entity.ScenarioTimingEntity;

/**
 * <h2>{@link ScenarioSpeedCorrelationChart}<br>
 * <sub>TODO</sub></h2>
 *
 * <p>
 * </p>
 *
 * <p>
 * <i>Mar 3, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioSpeedCorrelationChart extends AbstractCorrelationChart {

	private LinkedList<Long> activeScenarios;

	/**
	 * Create a new {@link ScenarioSpeedCorrelationChart} instance.
	 */
	public ScenarioSpeedCorrelationChart(int period) {

		super("Correlation: Scenario Speed - Agent Duration",
				"Correlation (-1 ~ 1)", period);

		this.activeScenarios = new LinkedList<Long>();
	}

	/**
	 * Active Scenarios: Amount of scenarios currently running.
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected double getCorrelationX(ScenarioTimingEntity timing) {

		// return timing.getAgentDuration();
		double x = this.activeScenarios.size();
		// this.LOG.debug("X: 1000 * " + this.activeScenarios.size() + " / "
		// + this.period + " = " + x);
		return x;
	}

	/**
	 * Agent Duration: Time (in seconds) of a single scenario execution on the
	 * agent.
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected double getCorrelationY(ScenarioTimingEntity timing) {

		// return timing.getOlasDuration();
		double y = timing.getAgentDuration() / 1000d;
		// this.LOG.debug("Y: " + timing.getAgentDuration() + " / 1000 = " + y);
		return y;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Number getMovingAverage() {

		// Add the end time of the latest timing to the active scenarios list.
		ScenarioTimingEntity current = this.averageData.getLast();
		this.activeScenarios.offer(current.getStart()
				+ current.getAgentDuration());

		// Poll all active scenarios that ended before the start of the current.
		while (this.activeScenarios.peek() < current.getStart())
			this.activeScenarios.poll();

		// Use a static mean for X, not the mean of the current period.
		 if (this.customMeanX == null)
			this.customMeanX = (double) current.getExecution().getWorkers();

		return super.getMovingAverage();
	}
}
