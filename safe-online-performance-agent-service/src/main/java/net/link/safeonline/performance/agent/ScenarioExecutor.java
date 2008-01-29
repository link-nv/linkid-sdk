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
 * <h2>{@link ScenarioExecutor} - Thread in which we execute the scenario.</h2>
 * <p>
 * We retrieve the EJB for the scenario and use it to prepare a new scenario
 * execution. We then create a thread pool which size depends on the amount of
 * workers we were instructed to use and tell the scenario EJB to execute
 * scenarios in these threads.<br>
 * <br>
 * The execution can be aborted by calling {@link #halt()}. A best-effort
 * attempt will be made to abort the scenario by interrupting all active threads
 * and shutting down the thread pool. The agent will revert to its previous
 * state and no statistics will be available.
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

		this.abort = false;
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
			while (!this.abort && System.currentTimeMillis() < until)
				try {
					Thread.sleep(Math.min(until - System.currentTimeMillis(),
							50));
				} catch (InterruptedException e) {
					break;
				}

			// Shut down and wait for active scenarios to complete.
			this.pool.shutdown();
			try {
				while (!this.pool.awaitTermination(1, TimeUnit.SECONDS))
					Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Don't generate stats if we were aborted.
			if (this.abort) {
				this.agentService.actionCompleted(false);
				return;
			}

			// Generate the resulting statistical information.
			ScenarioExecution stats = new ScenarioExecution(this.agents,
					this.workers, this.duration, this.hostname, execution,
					scenarioBean.getSpeed(execution), scenarioBean
							.getScenario(execution), scenarioBean
							.createGraphs(execution));
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

		if (this.pool != null)
			this.pool.shutdownNow();

		if (isAlive())
			interrupt();
	}
}
