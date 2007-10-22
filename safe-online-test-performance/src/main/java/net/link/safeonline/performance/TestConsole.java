/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

/**
 * 
 * 
 * @author lhunath
 */

public class TestConsole {

	private Scenario scenario;

	/**
	 * Create a new {@link TestConsole} instance.
	 */
	public TestConsole() {

		scenario = Scenario.getIdMappingScenario();
	}

	public static void main(String[] args) {

		new TestConsole();
	}
}
