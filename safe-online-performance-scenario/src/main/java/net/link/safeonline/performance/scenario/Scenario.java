/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;

/**
 * @author mbillemo
 *
 */
public interface Scenario extends Runnable {

	/**
	 * Prepare execution drivers and load the keys.
	 */
	public void prepare(ExecutionEntity execution, AgentTimeEntity agentTime);
}
