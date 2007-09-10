/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.statement;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import net.link.safeonline.shared.asn1.statement.DERIdentityStatement;

/**
 * Component for construction of the identity statement. The identity statement
 * links the certificate with the username.
 * 
 * @author fcorneli
 * 
 */
public class IdentityStatement extends AbstractStatement {

	public IdentityStatement(X509Certificate authenticationCertificate,
			String user, String givenName, String surname,
			PrivateKey authenticationPrivateKey) {
		super(authenticationPrivateKey, new DERIdentityStatement(
				authenticationCertificate, user, givenName, surname));
	}
}
