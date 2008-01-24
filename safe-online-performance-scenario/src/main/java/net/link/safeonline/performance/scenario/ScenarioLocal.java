/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.util.Map;

import javax.ejb.Local;

/**
 * @author mbillemo
 *
 */
@Local
public interface ScenarioLocal {

	public static final String BINDING = "SafeOnline/ScenarioBean";

	/**
	 * Execute the scenario.
	 */
	public void execute(int executionId) throws Exception;

	/**
	 * This method is called before any iterations are executed.
	 */
	public int prepare(String hostname);

	/**
	 * Create charts on data collected in this scenario.
	 */
	public Map<String, byte[][]> createGraphs(int executionId);

	/**
	 * Calculate the average speed for the given execution (#/ms).
	 */
	public Double getSpeed(int execution);

	/**
	 * Retrieve the name of the scenario that was used in the given execution.
	 */
	public String getScenario(int execution);
}
