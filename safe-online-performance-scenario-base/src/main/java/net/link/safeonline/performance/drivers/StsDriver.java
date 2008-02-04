/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.drivers;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;

import org.w3c.dom.Element;

/**
 *
 * @author mbillemo
 */
public class StsDriver extends ProfileDriver {

	public StsDriver(ExecutionEntity execution, AgentTimeEntity agentTime) {

		super("Security Token Service Driver", execution, agentTime);
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
	public void getAttributes(PrivateKeyEntry applicationKey, Element token) {

		if (!(applicationKey.getCertificate() instanceof X509Certificate))
			throw new IllegalArgumentException(
					"The certificate in the keystore needs to be of X509 format.");

		SecurityTokenServiceClientImpl service = new SecurityTokenServiceClientImpl(
				getHost(), (X509Certificate) applicationKey.getCertificate(),
				applicationKey.getPrivateKey());

		try {
			service.validate(token);
		}

		catch (Exception error) {
			report(error);
		} finally {
			report(service);
		}
	}
}
