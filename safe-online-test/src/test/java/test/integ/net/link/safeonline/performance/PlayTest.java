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
package test.integ.net.link.safeonline.performance;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;

/**
 * <h2>{@link PlayTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Mar 3, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class PlayTest extends AbstractDataTest {

	private static final int DATA_POINTS = 800;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {

		this.SHOW_SQL = false;
	}

	/**
	 * Create a new {@link PlayTest} instance.
	 */
	@SuppressWarnings("unchecked")
	public PlayTest() throws Exception {

		ExecutionEntity execution = getLatestExecution();
		DriverProfileEntity profile = this.executionService.getProfiles(
				execution.getStartTime()).iterator().next();

		long start = System.currentTimeMillis();
		long dataDuration = (Long) this.em.createNamedQuery(
				ProfileDataEntity.getExecutionDuration).setParameter("profile",
				profile).getSingleResult();
		long dataStart = (Long) this.em.createNamedQuery(
				ProfileDataEntity.getExecutionStart).setParameter("profile",
				profile).getSingleResult();

		int period = (int) Math.ceil((double) dataDuration / DATA_POINTS);

		System.err.println(" - 0: " + (System.currentTimeMillis() - start));

		for (long point = 0; point * period < dataDuration; ++point) {

			start = System.currentTimeMillis();
			List<ScenarioTimingEntity> result = this.em.createNamedQuery(
					ProfileDataEntity.getScenarioTiming).setParameter("start",
					dataStart + point * period).setParameter("stop",
					dataStart + (point + 1) * period).setMaxResults(1)
					.getResultList();

			System.err.println(" - 1: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();

			List<MeasurementEntity> measurements = this.em.createNamedQuery(
					ProfileDataEntity.createAverage).setParameter("profile",
					profile).setParameter("start", dataStart + point * period)
					.setParameter("stop", dataStart + (point + 1) * period)
					.getResultList();

			System.err.println(" - 2: " + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
	}

	/**
	 * Get the most recent execution.
	 */
	private ExecutionEntity getLatestExecution() {

		Date executionId = new TreeSet<Date>(this.executionService
				.getExecutions()).last();

		return this.executionService.getExecution(executionId);
	}

	public static void main(String[] args) throws Exception {

		new PlayTest();
	}
}
