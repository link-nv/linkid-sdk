/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import java.io.File;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;


/**
 * <h2>{@link ScenarioUploaderThread}<br>
 * <sub>This thread uploads a scenario to a given agent.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ScenarioUploaderThread extends ScenarioThread {

    private File application;


    public ScenarioUploaderThread(File application) {

        super(AgentState.UPLOAD);
        this.application = application;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void process(ConsoleAgent agent)
            throws Exception {

        try {
            agent.setAutoUpdate(false);

            scenarioDeployer.upload(agent.getAddress(), application);
        }

        finally {
            agent.setAutoUpdate(true);
        }
    }
}
