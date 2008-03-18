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

import net.link.safeonline.performance.entity.ScenarioTimingEntity;

/**
 * <h2>{@link AbstractCorrelationChart}<br>
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
public abstract class AbstractCorrelationChart extends
		AbstractMovingAverageChart {

	protected Double customMeanX, customMeanY;

	/**
	 * Create a new {@link AbstractCorrelationChart} instance.
	 */
	public AbstractCorrelationChart(String title, String rangeAxisName,
			int period) {

		super(title, rangeAxisName, period);
	}

	/**
	 * @return The value of the first dimension of the correlation for the given
	 *         {@link ScenarioTimingEntity}.
	 */
	protected abstract double getCorrelationX(Long startTime);

	/**
	 * @return The value of the second dimension of the correlation for the
	 *         given {@link ScenarioTimingEntity}.
	 */
	protected abstract double getCorrelationY(Long startTime);

	/**
	 * Calculate the correlation coefficient for our current period's data.
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected Number getMovingAverage() {

		double sum_sq_x = 0, sum_sq_y = 0, sum_coproduct = 0;
		double mean_x = this.customMeanX != null ? this.customMeanX
				: getCorrelationX(this.averageTimes.getFirst());
		double mean_y = this.customMeanY != null ? this.customMeanY
				: getCorrelationY(this.averageTimes.getFirst());

		int i = 1;
		for (Long startTime : this.averageTimes) {
			if (startTime == this.averageTimes.getFirst())
				continue;

			double sweep = (i - 1d) / i;
			double delta_x = getCorrelationX(startTime) - mean_x;
			double delta_y = getCorrelationY(startTime) - mean_y;

			sum_sq_x += delta_x * delta_x * sweep;
			sum_sq_y += delta_y * delta_y * sweep;
			sum_coproduct += delta_x * delta_y * sweep;

			if (this.customMeanX == null)
				mean_x += delta_x / i;
			if (this.customMeanY == null)
				mean_y += delta_y / i;

			++i;
		}

		int n = i;
		double pop_sd_x = Math.sqrt(sum_sq_x / n);
		double pop_sd_y = Math.sqrt(sum_sq_y / n);
		double cov_x_y = sum_coproduct / n;

		double correlation = cov_x_y / (pop_sd_x * pop_sd_y);
		return correlation;
	}
}