/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.identity;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.util.ASN1Dump;

public class IdentityStatementFactoryTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(IdentityStatementFactoryTest.class);

	public void testCreateIdentityStatement() throws Exception {
		// setup
		String testUser = "test-user";
		String testGivenName = "test-given-name";
		String testSurname = "test-surname";
		String testStreet = "test-street";
		String testPostalCode = "test-postal-code";
		String testCity = "test-city";
		KeyPair testKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate testCertificate = PkiTestUtils
				.generateSelfSignedCertificate(testKeyPair, "CN=Test");
		SmartCard testSmartCard = new SoftwareSmartCard(testGivenName,
				testSurname, testStreet, testPostalCode, testCity, testKeyPair
						.getPrivate(), testCertificate);

		// operate
		byte[] result = IdentityStatementFactory.createIdentityStatement(
				testUser, testSmartCard);

		// verify
		assertNotNull(result);
		LOG.debug("result size: " + result.length);
		DERSequence sequence = (DERSequence) ASN1Object.fromByteArray(result);
		LOG.debug("DER result: " + ASN1Dump.dumpAsString(sequence));
	}
}
