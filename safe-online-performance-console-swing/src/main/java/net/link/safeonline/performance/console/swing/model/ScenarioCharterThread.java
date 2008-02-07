/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.ui.Charts;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

/**
 * This thread executes a scenario on a given agent and manages the
 * {@link ConsoleAgent} object's execution status.
 *
 * @author mbillemo
 */
public class ScenarioCharterThread extends ScenarioThread {

	public ScenarioCharterThread(ScenarioChooser chooser) {

		super(AgentState.CHART, chooser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		super.run();

		new Thread("Worker") {
			{
				setDaemon(true);
			}

			@Override
			public void run() {

				Charts.display();
			}
		}.start();
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void process(ConsoleAgent agent) throws Exception {

	}
}
