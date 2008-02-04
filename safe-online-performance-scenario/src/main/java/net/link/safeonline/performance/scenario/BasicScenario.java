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
import net.link.safeonline.performance.entity.AgentTimeEntity;
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

	private PrivateKeyEntry applicationKey;
	private AttribDriver attribDriver;
	private IdMappingDriver idDriver;
	private AuthDriver authDriver;

	public void prepare(ExecutionEntity execution, AgentTimeEntity agentTime) {

		LOG.debug("retrieving performance keys..");
		try {
			PerformanceService service = (PerformanceService) getInitialContext(
					execution.getHostname()).lookup(PerformanceService.BINDING);
			this.applicationKey = new KeyStore.PrivateKeyEntry(service
					.getPrivateKey(), new Certificate[] { service
					.getCertificate() });
		} catch (NamingException e) {
			LOG.error("OLAS couldn't provide performance keys.", e);
		}
		if (this.applicationKey == null)
			this.applicationKey = PerformanceKeyStoreUtils.getPrivateKeyEntry();

		LOG.debug("building drivers..");
		this.authDriver = new AuthDriver(execution, agentTime);
		this.attribDriver = new AttribDriver(execution, agentTime);
		this.idDriver = new IdMappingDriver(execution, agentTime);
	}

	/**
	 * @{inheritDoc}
	 */
	public void run() {

		if (this.applicationKey == null)
			throw new IllegalStateException(
					"Performance keys not set up. Perhaps you didn't call prepare?");

		LOG.debug("getting id..");
		String loginUserId = this.authDriver.login(this.applicationKey,
				applicationName, username, password);
		String mappedUserId = this.idDriver.getUserId(this.applicationKey,
				username);

		LOG.debug("verify id..");
		if (loginUserId == null || !loginUserId.equals(mappedUserId))
			LOG.warn("UUID from login is not the same as UUID from idmapping.");

		LOG.debug("getting attribs..");
		this.attribDriver.getAttributes(this.applicationKey, mappedUserId);
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
