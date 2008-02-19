/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.util.Map;
import java.util.Set;

import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.scenario.bean.ScenarioControllerBean;

/**
 * <h2>{@link ScenarioController}<br>
 * <sub>The POJO interface to the {@link ScenarioControllerBean}.</sub></h2>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public interface ScenarioController {

	public static final String BINDING = "SafeOnline/ScenarioControllerBean";

	/**
	 * Execute the scenario.
	 */
	public void execute(int executionId) throws Exception;

	/**
	 * This method is called before any iterations are executed.<br>
	 * <br>
	 * These accessors need to be non-<code>null</code>:
	 * {@link ExecutionMetadata#getAgents()},
	 * {@link ExecutionMetadata#getWorkers()},
	 * {@link ExecutionMetadata#getDuration()},
	 * {@link ExecutionMetadata#getHostname()}.
	 */
	public int prepare(ExecutionMetadata metaData);

	/**
	 * Retrieve all scenarios registered for use.
	 */
	public Set<String> getScenarios();

	/**
	 * Retrieve all available execution IDs.
	 */
	public Set<Integer> getExecutions();

	/**
	 * Retrieve an object that holds all metadata concerning a given execution.
	 */
	public ExecutionMetadata getExecutionMetadata(int execution);

	/**
	 * Retrieve an HTML formatted description string for the given scenario.
	 */
	public String getDescription(String scenario);


	/**
	 * Create charts on data collected in this scenario.
	 */
	public Map<String, byte[][]> createCharts(int executionId);

	/**
	 * Retrieve and fully load all fields of the given execution.
	 */
	public ExecutionEntity loadExecution(int executionId);
}
