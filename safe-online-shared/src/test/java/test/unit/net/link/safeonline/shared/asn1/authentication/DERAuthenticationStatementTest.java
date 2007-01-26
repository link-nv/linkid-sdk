/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.shared.asn1.authentication;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERVisibleString;

import net.link.safeonline.shared.asn1.authentication.DERAuthenticationStatement;
import net.link.safeonline.test.util.PkiTestUtils;
import junit.framework.TestCase;

public class DERAuthenticationStatementTest extends TestCase {

	public void testEncoding() throws Exception {
		// setup
		KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate authCert = PkiTestUtils.generateSelfSignedCertificate(
				authKeyPair, "CN=AuthTest");

		String sessionId = "test-session-id";
		byte[] signature = "signature-value".getBytes();

		// operate
		DERAuthenticationStatement authenticationStatement = new DERAuthenticationStatement(
				sessionId, authCert);
		authenticationStatement.setSignature(signature);

		byte[] result = authenticationStatement.getEncoded();

		// verify
		assertNotNull(result);

		ASN1Sequence sequence = ASN1Sequence.getInstance(ASN1Object
				.fromByteArray(result));
		assertEquals(2, sequence.size());
		ASN1Sequence tbsSequence = ASN1Sequence.getInstance(sequence
				.getObjectAt(DERAuthenticationStatement.TBS_IDX));
		assertEquals(3, tbsSequence.size());
		DERInteger resultVersion = DERInteger.getInstance(tbsSequence
				.getObjectAt(DERAuthenticationStatement.TBS_VERSION_IDX));
		assertEquals(DERAuthenticationStatement.VERSION, resultVersion
				.getValue().intValue());
		DERVisibleString resultSession = DERVisibleString
				.getInstance(tbsSequence
						.getObjectAt(DERAuthenticationStatement.TBS_SESSION_IDX));
		assertEquals(sessionId, resultSession.getString());
		byte[] resultEncodedCert = tbsSequence.getObjectAt(
				DERAuthenticationStatement.TBS_AUTH_CERT_IDX).getDERObject()
				.getDEREncoded();
		assertTrue(Arrays.equals(resultEncodedCert, authCert.getEncoded()));

		DERBitString resultSignature = DERBitString.getInstance(sequence
				.getObjectAt(DERAuthenticationStatement.SIGNATURE_IDX));
		assertTrue(Arrays.equals(signature, resultSignature.getBytes()));
	}
}
