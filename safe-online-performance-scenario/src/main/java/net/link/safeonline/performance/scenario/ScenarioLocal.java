/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.List;

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
	public void execute(Scenario scenario) throws Exception;

	/**
	 * This method is called before any iterations are executed.
	 *
	 * @see Scenario#prepare(String)
	 */
	public Scenario prepare(String hostname, PrivateKeyEntry performanceKey);

	/**
	 * Create charts on data collected in this scenario.
	 */
	public List<byte[]> createGraphs(Scenario scenario, List<Long> scenarioStart);
}
