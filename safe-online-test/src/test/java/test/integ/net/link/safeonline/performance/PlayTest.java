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

import javax.persistence.NoResultException;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.MeasurementEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.util.performance.ProfileData;

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

		this.DB_HOST = "sebeco-dev-12";
		this.SHOW_SQL = false;
	}

	/**
	 * Create a new {@link PlayTest} instance.
	 */
	@SuppressWarnings("unchecked")
	public PlayTest() throws Exception {

		// ExecutionEntity execution = getLatestExecution();
		ExecutionEntity execution = this.executionService
				.getExecution(new Date(1204796106 * 1000l));

		DriverProfileEntity profile = this.executionService.getProfiles(
				execution.getStartTime()).iterator().next();

		long start = System.currentTimeMillis();
		long dataDuration = (Long) this.em.createNamedQuery(
				ProfileDataEntity.getExecutionDuration).setParameter("profile",
				profile).getSingleResult();
		long dataStart = (Long) this.em.createNamedQuery(
				ProfileDataEntity.getExecutionStart).setParameter("profile",
				profile).getSingleResult();
		dataStart = 1204796453397l;

		int period = (int) Math.ceil((double) dataDuration / DATA_POINTS);

		// System.err.println(" - 0: " + (System.currentTimeMillis() - start));

		long point = 0;
		System.out.println("duration: " + dataDuration);
		System.out.println("period: " + period);
		// for (long point = 0; point * period < dataDuration; ++point) {
		// start = System.currentTimeMillis();
		// System.out.format("checking %.2f%%...", 100d * point * period
		// / dataDuration);
		// System.out.println();

		ScenarioTimingEntity timing;

		List<Object> ttt = this.em.createQuery(
				"SELECT t.startTime, t.agentDuration, t.olasDuration"
						+ "    FROM ProfileDataEntity d"
						+ "        JOIN d.scenarioTiming t"
						+ "        JOIN d.measurements m"
						+ "    WHERE d.profile = :profile"
						+ "        AND t.startTime >= :start"
						+ "        AND t.startTime < :stop").setParameter(
				"profile", profile).setParameter("start",
				dataStart + point * period).setParameter("stop",
				dataStart + (point + 1) * period).getResultList();

		QuickTest.printResults("Timings", ttt);

		try {
			timing = (ScenarioTimingEntity) this.em.createNamedQuery(
					ProfileDataEntity.getScenarioTiming).setParameter(
					"execution", profile.getExecution()).setParameter("start",
					dataStart + point * period).setParameter("stop",
					dataStart + (point + 1) * period).getSingleResult();
		} catch (NoResultException e) {
			return;
		}

		// System.out.print(" " + timing.getStart() + "...");
		// System.err.println(" - 1: " + (System.currentTimeMillis() -
		// start));
		// start = System.currentTimeMillis();

		ttt = this.em
				.createQuery(
						"SELECT d.scenarioTiming.startTime, d.scenarioTiming.agentDuration, d.scenarioTiming.olasDuration, m.measurement, m.duration"
								+ "    FROM ProfileDataEntity d          "
								+ "        JOIN d.measurements m         "
								+ "    WHERE d.profile = :profile        "
								+ "        AND d.scenarioTiming.startTime >= :start      "
								+ "        AND d.scenarioTiming.startTime < :stop      "
								+ "        AND m.measurement = :measurement")
				.setParameter("profile", profile).setParameter("start",
						dataStart + point * period).setParameter("stop",
						dataStart + (point + 1) * period).setParameter(
						"measurement", ProfileData.REQUEST_DELTA_TIME)
				.getResultList();

		QuickTest.printResults("Data", ttt);

		List<MeasurementEntity> measurements = this.em.createNamedQuery(
				ProfileDataEntity.createAverage).setParameter("profile",
				profile).setParameter("start", dataStart + point * period)
				.setParameter("stop", dataStart + (point + 1) * period)
				.getResultList();

		ProfileDataEntity profileDataEntity = new ProfileDataEntity(profile,
				timing);

		for (MeasurementEntity measurement : measurements) {
			measurement.setProfileData(profileDataEntity);
			profileDataEntity.getMeasurements().add(measurement);
			if (measurement.getMeasurement().equals(
					ProfileData.REQUEST_DELTA_TIME)) {
				if (timing.getAgentDuration() < measurement.getDuration())
					System.err.print("  req (" + measurement.getDuration()
							+ ") > agent (" + timing.getAgentDuration() + ")!");

				break;
			}
		}

		// System.err.println(" - 2: " + (System.currentTimeMillis() -
		// start));
		// start = System.currentTimeMillis();

		// System.out.println(" done.");
		// }
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
