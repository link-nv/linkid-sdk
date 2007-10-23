/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author mbillemo
 */
public class IdMappingDriver extends ProfileDriver {

	private static final Log LOG = LogFactory.getLog(IdMappingDriver.class);

	private String host;

	private String user;

	private NameIdentifierMappingClientImpl service;

	/**
	 * Create a new {@link IdMappingDriver} instance.
	 * 
	 * @param hostname
	 *            The hostname of the host that's running the idmapping
	 *            webservice.
	 * @param username
	 *            The username to map to ID in this test.
	 */
	public IdMappingDriver(String hostname, String username) {

		this.host = hostname;
		this.user = username;
	}

	@Override
	protected void prepare() {

		PrivateKeyEntry serviceEntry = DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry();

		if (!(serviceEntry.getCertificate() instanceof X509Certificate))
			throw new RuntimeException(
					"The certificate in the keystore needs to be of X509 format.");

		this.service = new NameIdentifierMappingClientImpl(this.host,
				(X509Certificate) serviceEntry.getCertificate(), serviceEntry
						.getPrivateKey());
	}

	@Override
	protected ProfileData run() throws DriverException {

		try {
			LOG.debug("retrieving user ID for " + this.user);
			this.service.getUserId(this.user);

			return new ProfileData(this.service.getHeaders());
		}

		catch (Exception e) {
			throw new DriverException(e);
		}
	}
}
