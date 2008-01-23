/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.agent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import net.link.safeonline.performance.console.ScenarioExecution;
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
	private Integer agents;
	private Long duration;

	private ScheduledExecutorService pool;

	private boolean abort;

	public ScenarioExecutor(String hostname, Integer agents, Integer workers,
			Long duration, AgentService agentService) {

		this.hostname = hostname;
		this.workers = workers;
		this.agents = agents;
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
			this.pool = Executors.newScheduledThreadPool(this.workers);
			for (int i = 0; i < this.workers; ++i)
				this.pool.scheduleWithFixedDelay(new Runnable() {
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
					break;
				}

			if (this.abort) {
				this.agentService.actionCompleted(false);
				return;
			}

			// Shut down and wait for active scenarios to complete.
			this.pool.shutdown();
			try {
				while (!this.pool.awaitTermination(1, TimeUnit.SECONDS))
					Thread.yield();
			} catch (InterruptedException e) {
			}

			// Generate the resulting statistical information.
			ScenarioExecution stats = new ScenarioExecution(this.agents,
					this.workers, this.duration, execution, this.hostname,
					scenarioBean.createGraphs(execution));
			this.agentService.setStats(stats);

			// Notify the agent service of the scenario completion.
			this.agentService.actionCompleted(true);
		}

		catch (Exception e) {
			this.agentService.setError(e);
			this.agentService.actionCompleted(false);
			LOG.error("Processing Scenario Execution Failed", e);
		}
	}

	/**
	 * Make a best-effort attempt at halting the scenario execution.
	 */
	public void halt() {

		this.abort = true;
		this.pool.shutdownNow();
	}
}
