/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.model;

import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;


/**
 * <h2>{@link AgentRefreshThread}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 2, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class AgentRefreshThread extends ScenarioThread {

    private boolean reset;


    public AgentRefreshThread(boolean reset) {

        super(null);

        this.reset = reset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void process(ConsoleAgent agent) throws Exception {

        if (this.reset)
            agent.resetTransit();

        agent.updateState();
        ConsoleData.fireAgentStatus(agent);
    }
}
