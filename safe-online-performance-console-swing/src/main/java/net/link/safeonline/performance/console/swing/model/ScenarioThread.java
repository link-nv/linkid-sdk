/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Map;

import net.link.safeonline.performance.console.ScenarioDeployer;
import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.data.Agent.State;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

import org.jgroups.Address;

/**
 * Threads that extend this class are used for delegating actions that should be
 * performed on {@link Agent}s and can take a long time in order to prevent
 * hanging the UI during this operation.
 * 
 * @author mbillemo
 */
public abstract class ScenarioThread implements Runnable {

	State state;
	Map<Address, Agent> agents;
	ScenarioChooser chooser;
	ScenarioDeployer scenarioDeployer;

	public ScenarioThread(State state, Map<Address, Agent> agents,
			ScenarioChooser chooser) {

		this.state = state;
		this.agents = agents;
		this.chooser = chooser;
		this.scenarioDeployer = new ScenarioDeployer();
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {

		for (final Map.Entry<Address, Agent> agentEntry : this.agents
				.entrySet())

			new Thread() {
				@Override
				public void run() {

					Agent agent = agentEntry.getValue();
					if (!agent.startAction(ScenarioThread.this.state))
						return;

					try {
						agent.setError(null);
						process(agentEntry.getKey(), agent);
						agent.stopAction(true);
					}

					catch (Exception e) {
						agent.setError(e);
						agent.stopAction(false);
					}
				}
			}.start();
	}

	/**
	 * Perform the action that needs to be performed on each selected agent.
	 */
	abstract void process(Address address, Agent agent) throws Exception;
}
