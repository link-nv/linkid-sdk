/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.identity;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.util.ASN1Dump;

import net.link.safeonline.shared.identity.IdentityStatement;
import net.link.safeonline.test.util.PkiTestUtils;

import junit.framework.TestCase;

public class IdentityStatementTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(IdentityStatementTest.class);

	public void testCreateIdentityStatement() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair,
						"CN=AuthenticationCertificate");
		String user = "user";
		String givenName = "givenName";
		String surname = "surname";

		// operate
		IdentityStatement identityStatement = new IdentityStatement(
				certificate, user, givenName, surname, keyPair.getPrivate());
		byte[] resultIdentityStatement = identityStatement
				.generateIdentityStatement();

		// verify
		assertNotNull(resultIdentityStatement);
		ASN1Object asn1IdentityStatement = ASN1Object
				.fromByteArray(resultIdentityStatement);
		LOG.debug("Identity statement: "
				+ ASN1Dump.dumpAsString(asn1IdentityStatement));
	}
}
