/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.shared.identity.IdentityStatement;

public class IdentityStatementFactory {

	public byte[] createIdentityStatement(SmartCard smartCard) {
		X509Certificate authCert = smartCard.getAuthenticationCertificate();
		String givenName = smartCard.getGivenName();
		String surname = smartCard.getSurname();
		PrivateKey authPrivateKey = smartCard.getAuthenticationPrivateKey();
		IdentityStatement identityStatement = new IdentityStatement(authCert,
				givenName, surname, authPrivateKey);
		byte[] identityStatementData = identityStatement
				.generateIdentityStatement();
		return identityStatementData;
	}
}
