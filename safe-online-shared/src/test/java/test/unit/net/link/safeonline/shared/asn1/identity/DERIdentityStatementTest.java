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
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.shared.asn1.statement.DERIdentityStatement;
import net.link.safeonline.test.util.PkiTestUtils;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERVisibleString;

public class DERIdentityStatementTest extends TestCase {

	public void testEncoding() throws Exception {
		// setup
		KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate authCert = PkiTestUtils.generateSelfSignedCertificate(
				authKeyPair, "CN=AuthTest");

		String sessionId = UUID.randomUUID().toString();
		String user = "user";
		String operation = "operation";
		String givenName = "given-name";
		String surname = "surname";

		byte[] signature = "signature-value".getBytes();

		// operate
		DERIdentityStatement identityStatement = new DERIdentityStatement(
				authCert, sessionId, user, operation, givenName, surname);
		identityStatement.setSignature(signature);
		byte[] result = identityStatement.getEncoded();

		// verify
		assertNotNull(result);
		DERSequence sequence = (DERSequence) ASN1Sequence
				.getInstance(ASN1Object.fromByteArray(result));
		DERSequence bodySequence = (DERSequence) sequence.getObjectAt(0);
		DERInteger version = DERInteger.getInstance(bodySequence
				.getObjectAt(DERIdentityStatement.VERSION_IDX));
		assertEquals(1, version.getValue().intValue());

		DERVisibleString sessionIdString = DERVisibleString
				.getInstance(bodySequence
						.getObjectAt(DERIdentityStatement.SESSION_IDX));
		assertEquals(sessionId, sessionIdString.getString());

		DERVisibleString userString = DERVisibleString.getInstance(bodySequence
				.getObjectAt(DERIdentityStatement.USER_IDX));
		assertEquals(user, userString.getString());

		DERVisibleString operationString = DERVisibleString
				.getInstance(bodySequence
						.getObjectAt(DERIdentityStatement.OPERATION_IDX));
		assertEquals(operation, operationString.getString());

		DERVisibleString givenNameString = DERVisibleString
				.getInstance(bodySequence
						.getObjectAt(DERIdentityStatement.GIVEN_NAME_IDX));
		assertEquals(givenName, givenNameString.getString());

		DERVisibleString surnameString = DERVisibleString
				.getInstance(bodySequence
						.getObjectAt(DERIdentityStatement.SURNAME_IDX));
		assertEquals(surname, surnameString.getString());

		DEREncodable encodedCert = bodySequence
				.getObjectAt(DERIdentityStatement.AUTH_CERT_IDX);
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
