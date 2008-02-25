/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.ui.ChartWindow;
import net.link.safeonline.performance.console.swing.ui.ScenarioChooser;

/**
 * <h2>{@link ScenarioCharterThread}<br>
 * <sub>This thread generates charts on a given agent.</sub></h2>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioCharterThread extends ScenarioThread {

	boolean createPDF;

	public ScenarioCharterThread(ScenarioChooser chooser, boolean createPDF) {

		super(AgentState.CHART, chooser);

		this.createPDF = createPDF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		new Thread("Worker") {
			{
				setDaemon(true);
			}

			@Override
			public void run() {

				if (ScenarioCharterThread.this.createPDF)
					PDF.generate();
				else
					ChartWindow.display();
			}
		}.start();

		super.run();
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void process(ConsoleAgent agent) throws Exception {

		/* Not used. */
	}
}
