/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.model.performance.PerformanceService;
import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;

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

	private AttribDriver attribDriver;
	private IdMappingDriver idDriver;
	private AuthDriver authDriver;

	/**
	 * @{inheritDoc}
	 */
	public long execute(ExecutionEntity execution) throws Exception {

		LOG.debug("retrieving performance keys..");
		PrivateKeyEntry applicationKey = null;
		try {
			PerformanceService service = (PerformanceService) getInitialContext(
					execution.getHostname()).lookup(PerformanceService.BINDING);
			applicationKey = new KeyStore.PrivateKeyEntry(service
					.getPrivateKey(), new Certificate[] { service
					.getCertificate() });
		} catch (NamingException e) {
			LOG.error("OLAS couldn't provide performance keys.", e);
		}
		if (applicationKey == null)
			applicationKey = PerformanceKeyStoreUtils.getPrivateKeyEntry();

		LOG.debug("building drivers..");
		this.authDriver = new AuthDriver(execution);
		this.attribDriver = new AttribDriver(execution);
		this.idDriver = new IdMappingDriver(execution);

		long startTime = System.currentTimeMillis();

		LOG.debug("getting id..");
		String userId = this.authDriver.login(applicationKey, applicationName,
				username, password);

		LOG.debug("verify id..");
		if (!userId.equals(this.idDriver.getUserId(applicationKey, username)))
			throw new RuntimeException(
					"UUID from login is not the same as UUID from idmapping.");

		LOG.debug("getting attribs..");
		this.attribDriver.getAttributes(applicationKey, userId);

		return startTime;
	}

	/**
	 * Retrieve an {@link InitialContext} for the JNDI of the AS on the given
	 * host.
	 */
	private static InitialContext getInitialContext(String hostname)
			throws NamingException {

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "jnp://" + hostname + ":1099");

		return new InitialContext(environment);
	}
}
