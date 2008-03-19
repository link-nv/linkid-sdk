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

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;

/**
 * <h2>{@link AttribDriver}<br>
 * <sub>Provides access to the Attribute Request service.</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class AttribDriver extends ProfileDriver {

	public static final String NAME = "User Attribute Driver";

	public AttribDriver(ExecutionEntity execution,
			ScenarioTimingEntity agentTime) {

		super(NAME, execution, agentTime);
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
			String userId) {

		if (!(applicationKey.getCertificate() instanceof X509Certificate))
			throw new IllegalArgumentException(
					"The certificate in the keystore needs to be of X509 format.");

		try {
			AttributeClientImpl service = new AttributeClientImpl(getHost(),
					(X509Certificate) applicationKey.getCertificate(),
					applicationKey.getPrivateKey());

			try {
				return service.getAttributeValues(userId);
			}

			finally {
				report(service);
			}
		}

		catch (Throwable error) {
			throw report(error);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {

		return "<b>Attribute Driver:</b><br>"
				+ "Retrieves all accessible attributes of the <i>'performance'</i> user for the <i>'performance-application'</i>.";
	}
}
