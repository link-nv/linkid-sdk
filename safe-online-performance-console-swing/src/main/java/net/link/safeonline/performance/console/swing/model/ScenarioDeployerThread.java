/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

/**
 * This thread deploys a scenario on a given agent and manages the
 * {@link ConsoleAgent} object's deployment status.
 *
 * @author mbillemo
 */
public class ScenarioDeployerThread extends ScenarioThread {

	public ScenarioDeployerThread(ScenarioChooser chooser) {

		super(AgentState.DEPLOY, chooser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(ConsoleAgent agent) throws Exception {

		this.scenarioDeployer.deploy(agent.getAddress());
	}
}
