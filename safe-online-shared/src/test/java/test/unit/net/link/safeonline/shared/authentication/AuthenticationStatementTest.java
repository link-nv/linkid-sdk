/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.authentication;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.shared.statement.AuthenticationStatement;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.util.ASN1Dump;

public class AuthenticationStatementTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationStatementTest.class);

	public void testCreateAuthenticationStatement() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair,
						"CN=AuthenticationCertificate");
		String sessionId = UUID.randomUUID().toString();
		String applicationId = UUID.randomUUID().toString();

		// operate
		AuthenticationStatement authenticationStatement = new AuthenticationStatement(
				sessionId, applicationId, certificate, keyPair.getPrivate());
		byte[] resultAuthenticationStatement = authenticationStatement
				.generateStatement();

		// verify
		assertNotNull(resultAuthenticationStatement);
		ASN1Object asn1IdentityStatement = ASN1Object
				.fromByteArray(resultAuthenticationStatement);
		LOG.debug("Authentication statement: "
				+ ASN1Dump.dumpAsString(asn1IdentityStatement));
	}
}
