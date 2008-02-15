/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.script;

import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.scenario.Scenario;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link DummyScenario} - A scenario that does absolutely nothing
 * whatsoever.</h2>
 *
 * <p>
 * This scenario does nothing other than logging the prepare and run method
 * entry and sleeping for a second in the run call.
 * </p>
 *
 * <p>
 * <i>Feb 7, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class DummyScenario implements Scenario {

	private static final Log LOG = LogFactory.getLog(DummyScenario.class);
	private static final long SLEEP_TIME = 1000;

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {

		return "This is a scenario stub which basically does nothing other than sleep for a specified amount of time (normally 1 second).\n\n"
				+ "No drivers are loaded and some debug-level messages are logged upon preparing and executing the scenario.";
	}

	public void prepare(ExecutionEntity execution, AgentTimeEntity agentTime) {

		LOG.debug("Prepare called.");
	}

	/**
	 * @{inheritDoc}
	 */
	public void run() {

		try {
			LOG.debug("Run called; sleeping for " + SLEEP_TIME + " ms.");

			Thread.sleep(SLEEP_TIME);
		}

		catch (InterruptedException e) {
			LOG.debug("Interrupted.");
		}
	}
}
