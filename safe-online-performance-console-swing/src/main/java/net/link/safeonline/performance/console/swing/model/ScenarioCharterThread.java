/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
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
	private Map<ConsoleAgent, ScenarioExecution> agentCharts;

	public ScenarioCharterThread(ScenarioChooser chooser, boolean createPDF) {

		super(AgentState.CHART, chooser);

		this.createPDF = createPDF;
		this.agentCharts = new HashMap<ConsoleAgent, ScenarioExecution>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void completed() {

		if (ScenarioCharterThread.this.createPDF)
			PDF.generate(this.agentCharts);
		else
			ChartWindow.display(this.agentCharts);
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	void process(ConsoleAgent agent) throws Exception {

		this.agentCharts.put(agent, agent.getCharts(ConsoleData.getExecution()
				.getStartTime()));
	}
}
