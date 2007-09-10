/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.shared.statement.AuthenticationStatement;

public class AuthenticationStatementFactory {

	private AuthenticationStatementFactory() {
		// empty
	}

	public static byte[] createAuthenticationStatement(String sessionId,
			String applicationId, SmartCard smartCard) {
		X509Certificate authCert = smartCard.getAuthenticationCertificate();
		PrivateKey authPrivateKey = smartCard.getAuthenticationPrivateKey();
		AuthenticationStatement authenticationStatement = new AuthenticationStatement(
				sessionId, applicationId, authCert, authPrivateKey);
		byte[] authenticationStatementData = authenticationStatement
				.generateStatement();
		return authenticationStatementData;
	}
}
