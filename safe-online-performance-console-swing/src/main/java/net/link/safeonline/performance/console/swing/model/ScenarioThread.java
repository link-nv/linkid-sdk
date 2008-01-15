/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Map;
import java.util.Map.Entry;

import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

import org.jgroups.Address;

/**
 * Threads that extend this class are used for delegating actions that should be
 * performed on {@link ConsoleAgent}s and can take a long time in order to
 * prevent hanging the UI during this operation.
 * 
 * @author mbillemo
 */
public abstract class ScenarioThread implements Runnable {

	AgentState state;
	Map<Address, ConsoleAgent> agents;
	ScenarioChooser chooser;
	ScenarioRemoting scenarioDeployer;

	public ScenarioThread(AgentState state, Map<Address, ConsoleAgent> agents,
			ScenarioChooser chooser) {

		this.state = state;
		this.agents = agents;
		this.chooser = chooser;
		this.scenarioDeployer = ConsoleData.getInstance().getRemoting();
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {

		for (final Map.Entry<Address, ConsoleAgent> agentEntry : this.agents
				.entrySet())
			new Worker(agentEntry).start();
	}

	/**
	 * Perform the action that needs to be performed on each selected agent.
	 */
	abstract void process(Address address, ConsoleAgent agent) throws Exception;

	class Worker extends Thread {

		private Entry<Address, ConsoleAgent> agentEntry;

		public Worker(Entry<Address, ConsoleAgent> agentEntry) {

			super("Dispatch Thread");
			setDaemon(true);

			this.agentEntry = agentEntry;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {

			ConsoleAgent agent = this.agentEntry.getValue();
			if (!agent.actionRequest(ScenarioThread.this.state))
				return;

			try {
				agent.setError(null);
				process(this.agentEntry.getKey(), agent);
			}

			catch (Exception e) {
				agent.setError(e);
			}
		}
	}
}
