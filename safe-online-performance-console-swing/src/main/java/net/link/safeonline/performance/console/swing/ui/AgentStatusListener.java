/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import net.link.safeonline.performance.console.swing.data.Agent;

/**
 * @author mbillemo
 * 
 */
public interface AgentStatusListener {

	public void statusChanged(Agent agent);
}
