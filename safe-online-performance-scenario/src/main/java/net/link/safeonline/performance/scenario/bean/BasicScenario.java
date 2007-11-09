/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.ArrayList;
import java.util.List;

import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.drivers.ProfileDriver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mbillemo
 * 
 */
public class BasicScenario implements Scenario {

	private static final Log LOG = LogFactory.getLog(BasicScenario.class);
	private static final int ITERATIONS = 50;

	private IdMappingDriver idDriver;
	private AttribDriver attribDriver;

	/**
	 * @{inheritDoc}
	 */
	public List<ProfileDriver> prepare(String hostname) {

		List<ProfileDriver> drivers = new ArrayList<ProfileDriver>();

		LOG.debug("building drivers..");
		drivers.add(this.idDriver = new IdMappingDriver(hostname));
		drivers.add(this.attribDriver = new AttribDriver(hostname));

		return drivers;
	}

	/**
	 * @{inheritDoc}
	 */
	public void execute() throws Exception {

		// Initialise application data needed to run the scenario.
		LOG.debug("initializing..");
		String username = "admin", userId;
		PrivateKeyEntry applicationKey = DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry();

		LOG.debug("getting id..");
		userId = this.idDriver.getUserId(applicationKey, username);

		LOG.debug("getting attribs..");
		this.attribDriver.getAttributes(applicationKey, userId);
	}

	/**
	 * @{inheritDoc}
	 */
	public int getIterations() {

		return ITERATIONS;
	}
}
