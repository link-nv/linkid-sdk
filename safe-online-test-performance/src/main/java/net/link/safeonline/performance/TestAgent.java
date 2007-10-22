/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.util.jacc.ProfileData;

/**
 * 
 * @author mbillemo
 */
public class TestAgent {

	private static final Log LOG = LogFactory.getLog(TestAgent.class);

	private IdMappingDriver driver;

	public TestAgent() {

		this.driver = new IdMappingDriver("localhost:8443", "admin");
	}

	private void runTest() {

		for (ProfileData iteration : this.driver.execute())
			LOG.info(iteration);
	}

	public static void main(String[] args) {

		new TestAgent().runTest();
	}
}
