/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import net.link.safeonline.performance.console.ScenarioDeployer;
import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.ui.Charts;
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

	private Map<Address, List<byte[]>> charts;
	private String hostname;
	private int port;

	public ScenarioExecutorThread(Map<Address, Agent> map,
			ScenarioChooser chooser, String hostname, int port) {

		super(map, chooser);

		this.hostname = hostname;
		this.port = port;
		this.charts = new HashMap<Address, List<byte[]>>();
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void done(boolean success) {

		if (success)
			new Charts(this.charts);
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void process(Address address, Agent agent) throws Exception {

		try {
			if (!agent.setExecuting(true))
				return;

			InitialContext context = ScenarioDeployer
					.getInitialContext(address);
			ScenarioRemote scenario = (ScenarioRemote) context
					.lookup("SafeOnline/ScenarioBean");

			this.charts.put(address, scenario.execute(String.format("%s:%d",
					this.hostname, this.port)));
		}

		finally {
			agent.setExecuting(false);
		}
	}
}
