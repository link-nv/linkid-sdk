/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.util.webapp.filter.ProfileStats;

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

		for (Map<ProfileStats, Number> iteration : this.driver.execute())
			for (Map.Entry<ProfileStats, Number> stat : iteration.entrySet())
				LOG.info(String.format("%20s : %-10s (%s)", stat.getKey()
						.getHeader(), stat.getValue(), stat.getKey()
						.getDescription()));
	}

	public static void main(String[] args) {
		new TestAgent().runTest();
	}
}
