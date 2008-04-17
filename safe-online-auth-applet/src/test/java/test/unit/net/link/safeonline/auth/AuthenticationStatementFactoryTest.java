/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.auth.AuthenticationStatementFactory;
import net.link.safeonline.shared.JceSigner;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.util.ASN1Dump;

public class AuthenticationStatementFactoryTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationStatementFactoryTest.class);

	public void testCreateAuthenticationStatement() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate cert = PkiTestUtils.generateSelfSignedCertificate(
				keyPair, "CN=Test");
		String sessionId = UUID.randomUUID().toString();
		String applicationId = "test-application-id";

		Signer signer = new JceSigner(keyPair.getPrivate(), cert);

		// operate
		byte[] result = AuthenticationStatementFactory
				.createAuthenticationStatement(sessionId, applicationId, signer);

		// verify
		assertNotNull(result);
		LOG.debug("authentication statement: "
				+ ASN1Dump.dumpAsString(ASN1Object.fromByteArray(result)));
	}
}
