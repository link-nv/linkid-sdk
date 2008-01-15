/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.io.File;
import java.util.Map;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

import org.jgroups.Address;

/**
 * This thread uploads a scenario to a given agent and manages the
 * {@link ConsoleAgent} object's uploading status.
 * 
 * @author mbillemo
 */
public class ScenarioUploaderThread extends ScenarioThread {

	private File application;

	public ScenarioUploaderThread(Map<Address, ConsoleAgent> map,
			ScenarioChooser chooser, File application) {

		super(AgentState.UPLOAD, map, chooser);
		this.application = application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(Address address, ConsoleAgent agent) throws Exception {

		this.scenarioDeployer.upload(address, this.application);
	}
}
