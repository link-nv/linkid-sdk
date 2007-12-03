/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Map;

import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;

/**
 * 
 * @author mbillemo
 */
public class AttribDriver extends ProfileDriver<AttributeClientImpl> {

	public AttribDriver(String hostname) {

		super(hostname, "User Attribute Driver");
	}

	/**
	 * Retrieve the attributes for a given user.
	 * 
	 * @param applicationKey
	 *            The certificate of the application making the request. This
	 *            identifies the application and gives the request the
	 *            application's authority.
	 * @param userId
	 *            The ID of the user whose attributes are being requested.
	 * @return A map of attributes belonging to the user containing all
	 *         attributes the application has access to.
	 * @throws DriverException
	 *             Any exception that occurred during the request will be
	 *             wrapped into this one.
	 */
	public Map<String, Object> getAttributes(PrivateKeyEntry applicationKey,
			String userId) throws DriverException {

		if (!(applicationKey.getCertificate() instanceof X509Certificate))
			throw new DriverException(
					"The certificate in the keystore needs to be of X509 format.");

		loadDriver(new AttributeClientImpl(this.host,
				(X509Certificate) applicationKey.getCertificate(),
				applicationKey.getPrivateKey()));

		try {
			return this.service.getAttributeValues(userId);
		}

		catch (Exception e) {
			throw setDriverError(e);
		}

		finally {
			unloadDriver();
		}
	}
}
