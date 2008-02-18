/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Threads that extend this class are used for delegating actions that should be
 * performed on {@link ConsoleAgent}s and can take a long time in order to
 * prevent hanging the UI during this operation.
 *
 * @author mbillemo
 */
public abstract class ScenarioThread implements Runnable {

	AgentState state;
	ScenarioChooser chooser;
	ScenarioRemoting scenarioDeployer;

	public ScenarioThread(AgentState state, ScenarioChooser chooser) {

		this.state = state;
		this.chooser = chooser;
		this.scenarioDeployer = ConsoleData.getRemoting();
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {

		for (final ConsoleAgent agent : ConsoleData.getSelectedAgents())
			new Worker(agent).start();
	}

	/**
	 * Perform the action that needs to be performed on each selected agent.
	 */
	abstract void process(ConsoleAgent agent) throws Exception;

	class Worker extends Thread {

		final Log LOG = LogFactory.getLog(ScenarioThread.Worker.class);

		private ConsoleAgent agent;

		public Worker(ConsoleAgent agent) {

			super("Dispatch Thread");
			setDaemon(true);

			this.agent = agent;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
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
