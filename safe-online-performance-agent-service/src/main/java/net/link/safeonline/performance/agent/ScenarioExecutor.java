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
package net.link.safeonline.performance.agent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import net.link.safeonline.performance.scenario.ScenarioLocal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link ScenarioExecutor} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 8, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioExecutor extends Thread {

	static final Log LOG = LogFactory.getLog(ScenarioExecutor.class);

	private AgentService agentService;
	private String hostname;
	private Integer workers;
	private Long duration;

	public ScenarioExecutor(String hostname, Integer workers, Long duration,
			AgentService agentService) {

		this.hostname = hostname;
		this.workers = workers;
		this.duration = duration;
		this.agentService = agentService;
	}

	@Override
	public void run() {

		try {
			// Find the scenario bean.
			final ScenarioLocal scenarioBean = (ScenarioLocal) new InitialContext()
					.lookup(ScenarioLocal.BINDING);

			// Setup the scenario.
			final int execution = scenarioBean.prepare(this.hostname);

			// Create a pool of threads that execute scenario beans.
			long until = System.currentTimeMillis() + this.duration;
			ScheduledExecutorService pool = Executors
					.newScheduledThreadPool(this.workers);
			for (int i = 0; i < this.workers; ++i)
				pool.scheduleWithFixedDelay(new Runnable() {
					public void run() {

						try {
							LOG.debug(">>> Scenario start.");
							scenarioBean.execute(execution);
							LOG.debug("<<< Scenario end.");
						} catch (Throwable e) {
							LOG.error("!!! Scenario error.", e);
						}
					}
				}, 0, 100, TimeUnit.MILLISECONDS);

			// Sleep this thread until the specified duration has elapsed.
			while (System.currentTimeMillis() < until)
				try {
					Thread.sleep(until - System.currentTimeMillis());
				} catch (InterruptedException e) {
				}

			// Shut down and wait for active scenarios to complete.
			pool.shutdown();
			try {
				while (!pool.awaitTermination(1, TimeUnit.SECONDS))
					Thread.yield();
			} catch (InterruptedException e) {
			}

			// Generate the resulting statistical information.
			this.agentService.setCharts(scenarioBean.createGraphs(execution));

			// Notify the agent service of the scenario completion.
			this.agentService.actionCompleted(true);
		}

		catch (Exception e) {
			this.agentService.setError(e);
			this.agentService.actionCompleted(false);
			LOG.error("Processing Scenario Execution Failed", e);
		}
	}
}
