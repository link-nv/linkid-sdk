/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.identity;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.shared.JceSigner;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;
import net.link.safeonline.shared.statement.IdentityStatement;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.util.ASN1Dump;

public class IdentityStatementTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(IdentityStatementTest.class);

	public void testCreateIdentityStatement() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair,
						"CN=AuthenticationCertificate");
		String sessionId = UUID.randomUUID().toString();
		String user = "user";
		String operation = "operation";

		Signer signer = new JceSigner(keyPair.getPrivate(), certificate);

		IdentityProvider identityProvider = new IdentityProvider() {
			public String getGivenName() {
				return "givenName";
			}

			public String getSurname() {
				return "surname";
			}
		};

		// operate
		IdentityStatement identityStatement = new IdentityStatement(sessionId,
				user, operation, identityProvider, signer);
		byte[] resultIdentityStatement = identityStatement.generateStatement();

		// verify
		assertNotNull(resultIdentityStatement);
		ASN1Object asn1IdentityStatement = ASN1Object
				.fromByteArray(resultIdentityStatement);
		LOG.debug("Identity statement: "
				+ ASN1Dump.dumpAsString(asn1IdentityStatement));
	}
}
