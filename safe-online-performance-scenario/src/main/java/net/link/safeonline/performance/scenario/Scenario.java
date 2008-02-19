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
 * <h2>{@link Scenario}<br>
 * <sub>This interface defines what a {@link Scenario} should implement to be
 * launchable in the agent.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public interface Scenario extends Runnable {

	/**
	 * Prepare execution drivers and load the keys.
	 */
	public void prepare(ExecutionEntity execution, AgentTimeEntity agentTime);

	/**
	 * Implement this method to provide am HTML formatted description string for
	 * your scenario: You should explain what it does and how it works.
	 */
	public String getDescription();
}
