/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.agent;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import net.link.safeonline.performance.scenario.ExecutionMetadata;
import net.link.safeonline.performance.scenario.ScenarioController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link ScenarioExecutor}<br>
 * <sub>Thread in which we execute the scenario.</sub></h2>
 * 
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
 * 
 * <p>
 * <i>Jan 8, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ScenarioExecutor extends Thread {

	static final Log LOG = LogFactory.getLog(ScenarioExecutor.class);

	AgentService agentService;
	private ExecutionMetadata request;
	private ScheduledExecutorService pool;
	private boolean abort;

	public ScenarioExecutor(ExecutionMetadata request, AgentService agentService) {

		super("ScenarioExecutor");

		this.request = request;
		this.agentService = agentService;
		this.abort = false;
	}

	@Override
	public void run() {

		try {
			// Find the scenario bean.
			final ScenarioController scenarioBean = (ScenarioController) new InitialContext()
					.lookup(ScenarioController.BINDING);

			// Setup the scenario.
			final Date execution = scenarioBean.prepare(this.request);

			// Create a pool of threads that execute scenario beans.
			long startTime = System.currentTimeMillis();
			this.pool = Executors.newScheduledThreadPool(this.request
					.getWorkers());
			for (int i = 0; i < this.request.getWorkers(); ++i)
				this.pool.scheduleWithFixedDelay(new Runnable() {
					public void run() {

						try {
							LOG.debug(">>> Scenario start.");
							// Thread.sleep(1000);
							scenarioBean.execute(execution);
							LOG.debug("<<< Scenario end.");
						} catch (Throwable e) {
							LOG.error("!!! Scenario error.", e);
						}
					}
				}, 0, 100, TimeUnit.MILLISECONDS);

			// Sleep this thread until the specified duration has elapsed.
			long until = startTime + this.request.getDuration();
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
			if (this.abort)
				return;

			// Start fetching metadata and creating charts already.
			Thread charter = new Thread("Charter") {
				@Override
				public void run() {

					try {
						LOG.debug("charts started.");
						ScenarioExecutor.this.agentService.getCharts(execution);
						LOG.debug("charts success.");
					} catch (Throwable e) {
						LOG.error(e);
					}
				}
			};
			charter.setDaemon(true);
			charter.start();
		}

		catch (Throwable e) {
			this.agentService.setError(e);
			LOG.error("Processing Scenario Execution Failed", e);
		}

		finally {
			this.agentService.actionCompleted();
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
