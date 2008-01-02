/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import javax.ejb.Local;

import net.link.safeonline.performance.scenario.bean.Scenario;

/**
 * @author mbillemo
 * 
 */
@Local
public interface ScenarioLocal {

	/**
	 * Execute the scenario.
	 */
	public void execute(Scenario scenario) throws Exception;
}
