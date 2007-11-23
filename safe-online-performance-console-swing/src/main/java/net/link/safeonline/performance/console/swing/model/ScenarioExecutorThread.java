/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Map;

import javax.naming.InitialContext;

import net.link.safeonline.performance.console.ScenarioDeployer;
import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.data.Agent.State;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;
import net.link.safeonline.performance.scenario.ScenarioRemote;

import org.jgroups.Address;

/**
 * This thread executes a scenario on a given agent and manages the
 * {@link Agent} object's execution status.
 * 
 * @author mbillemo
 */
public class ScenarioExecutorThread extends ScenarioThread {

	private ConsoleData consoleData;

	public ScenarioExecutorThread(Map<Address, Agent> map,
			ScenarioChooser chooser, ConsoleData consoleData) {

		super(State.EXECUTE, map, chooser);
		this.consoleData = consoleData;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void process(Address address, Agent agent) throws Exception {

		InitialContext context = ScenarioDeployer.getInitialContext(address);
		ScenarioRemote scenario = (ScenarioRemote) context
				.lookup("SafeOnline/ScenarioBean");

		agent.setCharts(scenario.execute(String.format("%s:%d",
				this.consoleData.getHostname(), this.consoleData.getPort()),
				this.consoleData.getWorkers()));

	}
}
