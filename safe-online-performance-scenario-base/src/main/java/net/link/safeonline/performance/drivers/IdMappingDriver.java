/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;

/**
 * 
 * 
 * @author mbillemo
 */
public class IdMappingDriver extends ProfileDriver {

	public IdMappingDriver(String hostname) {

		super(hostname, "User ID Mapping Driver");
	}

	/**
	 * Retrieve the ID of the user with the given username.
	 * 
	 * @param applicationKey
	 *            The certificate of the application making the request. This
	 *            identifies the application and gives the request the
	 *            application's authority.
	 * @param username
	 *            The username that the application wishes to know the ID for.
	 * @return The ID of the user with the given username.
	 * @throws DriverException
	 *             Any exception that occurred during the request will be
	 *             wrapped into this one.
	 */
	public String getUserId(PrivateKeyEntry applicationKey, String username)
			throws DriverException {

		if (!(applicationKey.getCertificate() instanceof X509Certificate))
			throw new DriverException(
					"The certificate in the keystore needs to be of X509 format.");

		startNewIteration();
		try {
			NameIdentifierMappingClientImpl service = new NameIdentifierMappingClientImpl(
					this.host, (X509Certificate) applicationKey
							.getCertificate(), applicationKey.getPrivateKey());

			String result = service.getUserId(username);
			setIterationData(service);

			return result;
		}

		catch (Exception e) {
			setIterationError(e);
			throw new DriverException(e);
		}
	}
}
