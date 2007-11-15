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
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.drivers.ProfileDriver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mbillemo
 * 
 */
public class BasicScenario implements Scenario {

	private static final int ITERATIONS = 50;
	private static final Log LOG = LogFactory.getLog(BasicScenario.class);

	private AttribDriver attribDriver;
	private AuthDriver authDriver;

	private IdMappingDriver idDriver;

	/**
	 * @{inheritDoc}
	 */
	public void execute() throws Exception {

		// Initialize givens (application, username, password).
		String applicationName = "demo-lawyer", username = "admin", password = "admin", userId;
		PrivateKeyEntry applicationKey = DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry();

		LOG.debug("getting id..");
		userId = this.authDriver.login(applicationName, username, password);

		LOG.debug("verify id..");
		if (!userId.equals(this.idDriver.getUserId(applicationKey, username)))
			throw new RuntimeException(
					"UUID from login is not the same as UUID from idmapping.");

		LOG.debug("getting attribs..");
		this.attribDriver.getAttributes(applicationKey, userId);
	}

	/**
	 * @{inheritDoc}
	 */
	public int getIterations() {

		return ITERATIONS;
	}

	/**
	 * @{inheritDoc}
	 */
	public List<ProfileDriver> prepare(String hostname) {

		List<ProfileDriver> drivers = new ArrayList<ProfileDriver>();

		LOG.debug("building drivers..");
		drivers.add(this.authDriver = new AuthDriver(hostname));
		drivers.add(this.attribDriver = new AttribDriver(hostname));
		drivers.add(this.idDriver = new IdMappingDriver(hostname));

		return drivers;
	}
}
