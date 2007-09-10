/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.authentication.service.bean.IdentityStatement;
import net.link.safeonline.test.util.PkiTestUtils;

public class IdentityStatementTest extends TestCase {

	public void testVerify() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		String user = "test-user";
		String givenName = "test-given-name";
		String surname = "test-surname";

		net.link.safeonline.shared.statement.IdentityStatement testIdentityStatement = new net.link.safeonline.shared.statement.IdentityStatement(
				certificate, user, givenName, surname, keyPair.getPrivate());

		byte[] encodedIdentityStatement = testIdentityStatement
				.generateStatement();

		// operate
		IdentityStatement identityStatement = new IdentityStatement(
				encodedIdentityStatement);

		X509Certificate resultCertificate = identityStatement.verifyIntegrity();

		// verify
		assertNotNull(resultCertificate);
		assertEquals(certificate, resultCertificate);
		assertEquals(givenName, identityStatement.getGivenName());
		assertEquals(surname, identityStatement.getSurname());
	}
}
