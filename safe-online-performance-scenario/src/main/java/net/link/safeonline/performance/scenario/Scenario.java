/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import net.link.safeonline.performance.entity.ExecutionEntity;

/**
 * @author mbillemo
 *
 */
public interface Scenario {

	/**
	 * Prepare execution drivers and execute the scenario.
	 *
	 * @return The time the scenario actually started querying OLAS.
	 */
	public long execute(ExecutionEntity execution) throws Exception;
}
