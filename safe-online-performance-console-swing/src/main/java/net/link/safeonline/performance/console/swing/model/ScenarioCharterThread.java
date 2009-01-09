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

    boolean                                      createPDF;
    private Map<ConsoleAgent, ScenarioExecution> agentCharts;


    public ScenarioCharterThread(boolean createPDF) {

        super(AgentState.CHART);

        this.createPDF = createPDF;
        agentCharts = new HashMap<ConsoleAgent, ScenarioExecution>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void completed() {

        if (createPDF) {
            PDF.generate(agentCharts);
        } else {
            ChartWindow.display(agentCharts);
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    void process(ConsoleAgent agent)
            throws Exception {

        agentCharts.put(agent, agent.getCharts(ConsoleData.getSelectedExecution().getStartTime()));
    }
}
