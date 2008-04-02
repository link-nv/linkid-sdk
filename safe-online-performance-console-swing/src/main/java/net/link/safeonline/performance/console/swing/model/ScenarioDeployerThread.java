/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;

/**
 * <h2>{@link ScenarioDeployerThread}<br>
 * <sub>This thread deploys a scenario on a given agent.</sub></h2>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioDeployerThread extends ScenarioThread {

	public ScenarioDeployerThread() {

		super(AgentState.DEPLOY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(ConsoleAgent agent) throws Exception {

		this.scenarioDeployer.deploy(agent.getAddress());
	}
}
