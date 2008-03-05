/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link ScenarioThread}<br>
 * <sub>Wrap long-running agent tasks in threads.</sub></h2>
 *
 * <p>
 * Threads that extend this class are used for delegating actions that should be
 * performed on {@link ConsoleAgent}s and can take a long time in order to
 * prevent hanging the UI during this operation.
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public abstract class ScenarioThread extends Thread {

	AgentState state;
	ScenarioChooser chooser;
	ScenarioRemoting scenarioDeployer;

	public ScenarioThread(AgentState state, ScenarioChooser chooser) {

		super("Scenario Invoker");
		setDaemon(true);

		this.state = state;
		this.chooser = chooser;
		this.scenarioDeployer = ConsoleData.getRemoting();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		ExecutorService pool = Executors.newCachedThreadPool();
		for (final ConsoleAgent agent : ConsoleData.getSelectedAgents()) {
			pool.submit(new Worker(agent));

			agent.setError(null);
			agent.setTransit(ScenarioThread.this.state);
		}

		try {
			pool.shutdown();
			while (!pool.awaitTermination(10, TimeUnit.SECONDS))
				Thread.yield();
		} catch (InterruptedException e) {
		}

		completed();
	}

	/**
	 * This method is called after all agents have completed the task.
	 */
	protected void completed() {

		/* Feel free to override. */
	}

	/**
	 * Perform the action that needs to be performed on each selected agent.
	 */
	abstract void process(ConsoleAgent agent) throws Exception;

	class Worker implements Runnable {

		final Log LOG = LogFactory.getLog(ScenarioThread.Worker.class);

		private ConsoleAgent agent;

		public Worker(ConsoleAgent agent) {

			this.agent = agent;
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {

			try {
				process(this.agent);
			}

			catch (Exception e) {
				this.LOG.error(
						"Couldn't perform requested operation on agent.", e);
			}
		}
	}
}
