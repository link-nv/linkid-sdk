/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.asn1.identity;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERVisibleString;

import net.link.safeonline.shared.asn1.identity.DERIdentityStatement;
import net.link.safeonline.test.util.PkiTestUtils;
import junit.framework.TestCase;

public class DERIdentityStatementTest extends TestCase {

	public void testEncoding() throws Exception {
		// setup
		KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate authCert = PkiTestUtils.generateSelfSignedCertificate(
				authKeyPair, "CN=AuthTest");

		String givenName = "given-name";
		String surname = "surname";

		byte[] signature = "signature-value".getBytes();

		// operate
		DERIdentityStatement identityStatement = new DERIdentityStatement(
				authCert, givenName, surname);
		identityStatement.setSignature(signature);
		byte[] result = identityStatement.getEncoded();

		// verify
		assertNotNull(result);
		DERSequence sequence = (DERSequence) DERSequence
				.getInstance(DERSequence.fromByteArray(result));
		DERSequence bodySequence = (DERSequence) sequence.getObjectAt(0);
		DERInteger version = DERInteger
				.getInstance(bodySequence.getObjectAt(0));
		assertEquals(1, version.getValue().intValue());

		DERVisibleString givenNameString = DERVisibleString
				.getInstance(bodySequence.getObjectAt(1));
		assertEquals(givenName, givenNameString.getString());

		DERVisibleString surnameString = DERVisibleString
				.getInstance(bodySequence.getObjectAt(2));
		assertEquals(surname, surnameString.getString());

		DEREncodable encodedCert = bodySequence.getObjectAt(3);
		assertNotNull(encodedCert);
		byte[] resultEncodedCert = encodedCert.getDERObject().getEncoded();
		X509Certificate resultCert = (X509Certificate) CertificateFactory
				.getInstance("X.509").generateCertificate(
						new ByteArrayInputStream(resultEncodedCert));
		assertEquals(authCert, resultCert);

		DERBitString signBitString = DERBitString.getInstance(sequence
				.getObjectAt(1));
		assertTrue(Arrays.equals(signature, signBitString.getBytes()));

	}
}
