/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.identity;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import net.link.safeonline.shared.identity.IdentityStatement;
import net.link.safeonline.test.util.PkiTestUtils;

import junit.framework.TestCase;

public class IdentityStatementTest extends TestCase {

	public void testCreateIdentityStatement() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=AuthTest");
		String givenName = "test-given-name";
		String surname = "test-sur-name";

		// operate
		IdentityStatement identityStatement = new IdentityStatement(
				certificate, givenName, surname, keyPair.getPrivate());
		byte[] resultIdentityStatement = identityStatement
				.generateIdentityStatement();

		// verify
		assertNotNull(resultIdentityStatement);
	}
}
