/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author mbillemo
 */
public class TestAgent {

	private static final Log LOG = LogFactory.getLog(TestAgent.class);

	public TestAgent() {

	}

	private void runTest(Scenario scenario) {

		for (Scenario.Request request : scenario.getRequests())
			new TestAgentWorker(request).start();
	}

	public static void main(String[] args) {

		new TestAgent().runTest(Scenario.getIdMappingScenario());
	}
}
