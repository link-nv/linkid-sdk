/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Map;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

import org.jgroups.Address;

/**
 * This thread executes a scenario on a given agent and manages the
 * {@link ConsoleAgent} object's execution status.
 * 
 * @author mbillemo
 */
public class ScenarioExecutorThread extends ScenarioThread {

	public ScenarioExecutorThread(Map<Address, ConsoleAgent> map,
			ScenarioChooser chooser) {

		super(AgentState.EXECUTE, map, chooser);
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void process(Address address, ConsoleAgent agent) throws Exception {

		String hostname = String.format("%s:%d", ConsoleData.getInstance()
				.getHostname(), ConsoleData.getInstance().getPort());
		this.scenarioDeployer.execute(address, hostname, ConsoleData
				.getInstance().getWorkers(), ConsoleData.getInstance()
				.getDuration());

	}
}
