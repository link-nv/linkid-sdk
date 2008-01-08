/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.ArrayList;
import java.util.List;

import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.drivers.ProfileDriver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mbillemo
 */
public class BasicScenario implements Scenario {

	private static final Log LOG = LogFactory.getLog(BasicScenario.class);

	private static final String applicationName = "performance-application";
	private static final String username = "performance";
	private static final String password = "performance";

	private PrivateKeyEntry applicationKey;
	private AttribDriver attribDriver;
	private IdMappingDriver idDriver;
	private AuthDriver authDriver;

	/**
	 * Create a new {@link BasicScenario} instance.
	 */
	public BasicScenario(PrivateKeyEntry applicationKey) {

		this.applicationKey = applicationKey;
	}

	/**
	 * @{inheritDoc}
	 */
	public void execute() throws Exception {

		LOG.debug("getting id..");
		String userId = this.authDriver.login(this.applicationKey,
				applicationName, username, password);

		LOG.debug("verify id..");
		if (!userId.equals(this.idDriver.getUserId(this.applicationKey,
				username)))
			throw new RuntimeException(
					"UUID from login is not the same as UUID from idmapping.");

		LOG.debug("getting attribs..");
		this.attribDriver.getAttributes(this.applicationKey, userId);
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
