/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Date;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

/**
 * This thread executes a scenario on a given agent and manages the
 * {@link ConsoleAgent} object's execution status.
 *
 * @author mbillemo
 */
public class ScenarioExecutorThread extends ScenarioThread {

	private Date startTime;

	public ScenarioExecutorThread(ScenarioChooser chooser) {

		super(AgentState.EXECUTE, chooser);

		this.startTime = new Date();
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void process(ConsoleAgent agent) throws Exception {

		String hostname = String.format("%s:%d", ConsoleData.getHostname(),
				ConsoleData.getPort());

		this.scenarioDeployer.execute(agent.getAddress(), ConsoleData
				.getScenarioName(), ConsoleData.getSelectedAgents().size(),
				ConsoleData.getWorkers(), ConsoleData.getDuration(), hostname,
				this.startTime);

	}
}
