/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.drivers;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;

import org.w3c.dom.Element;

/**
 * 
 * @author mbillemo
 */
public class StsDriver extends ProfileDriver {

	public StsDriver(String hostname) {

		super(hostname, "Security Token Service Driver");
	}

	/**
	 * Validate the given SAML token.
	 * 
	 * @param applicationKey
	 *            The certificate of the application making the request. This
	 *            identifies the application and gives the request the
	 *            application's authority.
	 * @param token
	 *            The SAML token that needs to be validated.
	 * @throws DriverException
	 *             Any exception that occurred during the request will be
	 *             wrapped into this one.
	 */
	public void getAttributes(PrivateKeyEntry applicationKey, Element token)
			throws DriverException {

		if (!(applicationKey.getCertificate() instanceof X509Certificate))
			throw new DriverException(
					"The certificate in the keystore needs to be of X509 format.");

		SecurityTokenServiceClientImpl service = new SecurityTokenServiceClientImpl(
				this.host, (X509Certificate) applicationKey.getCertificate(),
				applicationKey.getPrivateKey());
		loadDriver(service);

		try {
			service.validate(token);
		}

		catch (Exception e) {
			throw setDriverError(service, e);
		}

		finally {
			unloadDriver(service);
		}
	}
}
