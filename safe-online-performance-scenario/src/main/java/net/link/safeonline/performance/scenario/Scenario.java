/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.Map;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.scenario.bean.ScenarioBean;

/**
 * @author mbillemo
 *
 */
public interface Scenario {

	/**
	 * This method is called before any iterations are executed. This is where
	 * you should build all the drivers that you'll be needing in the scenario
	 * story. You should return all the drivers so that the {@link ScenarioBean}
	 * can read their profile data after the scenario has completed.
	 */
	public void prepare(String hostname, PrivateKeyEntry performanceKey,
			ExecutionEntity execution, ScenarioLocal scenarioBean);

	/**
	 * This method is called for each iteration of the scenario.
	 */
	public void execute() throws Exception;

	/**
	 * Retrieve the data collected by the drivers used in this scenario mapped
	 * by the driver titles.
	 */
	public Map<String, DriverProfileEntity> getDriverProfiles();
}
