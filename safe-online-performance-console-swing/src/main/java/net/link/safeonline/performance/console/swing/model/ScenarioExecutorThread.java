/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.util.Date;

import javax.swing.JOptionPane;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;


/**
 * <h2>{@link ScenarioExecutorThread}<br>
 * <sub>This thread executes a scenario on a given agent.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ScenarioExecutorThread extends ScenarioThread {

    private Date startTime;


    public ScenarioExecutorThread() {

        super(AgentState.EXECUTE);

        this.startTime = new Date();
    }

    /**
     * @{inheritDoc
     */
    @Override
    void process(ConsoleAgent agent)
            throws Exception {

        if (ConsoleData.getScenarioName() == null) {
            JOptionPane.showMessageDialog(null, "You didn't select a scenario to execute!", "No scenario selected.",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hostname = String.format("%s:%d", ConsoleData.getHostname(), ConsoleData.getPort());

        this.scenarioDeployer.execute(agent.getAddress(), ConsoleData.getScenarioName(), ConsoleData.getSelectedAgents().size(),
                ConsoleData.getWorkers(), ConsoleData.getDuration(), hostname, ConsoleData.isSsl(), this.startTime);

    }
}
