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

import junit.framework.TestCase;
import net.link.safeonline.shared.asn1.statement.AbstractDERStatement;
import net.link.safeonline.shared.asn1.statement.DERAuthenticationStatement;
import net.link.safeonline.test.util.PkiTestUtils;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERVisibleString;


public class DERAuthenticationStatementTest extends TestCase {

    public void testEncoding() throws Exception {

        // setup
        KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate authCert = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=AuthTest");

        String sessionId = "test-session-id";
        String applicationId = "test-application-id";
        byte[] signature = "signature-value".getBytes();

        // operate
        DERAuthenticationStatement authenticationStatement = new DERAuthenticationStatement(sessionId, applicationId,
                authCert);
        authenticationStatement.setSignature(signature);

        byte[] result = authenticationStatement.getEncoded();

        // verify
        assertNotNull(result);

        ASN1Sequence sequence = ASN1Sequence.getInstance(ASN1Object.fromByteArray(result));
        assertEquals(2, sequence.size());
        ASN1Sequence tbsSequence = ASN1Sequence.getInstance(sequence.getObjectAt(AbstractDERStatement.TBS_IDX));
        assertEquals(4, tbsSequence.size());
        DERInteger resultVersion = DERInteger.getInstance(tbsSequence
                .getObjectAt(DERAuthenticationStatement.TBS_VERSION_IDX));
        assertEquals(DERAuthenticationStatement.VERSION, resultVersion.getValue().intValue());
        DERVisibleString resultSession = DERVisibleString.getInstance(tbsSequence
                .getObjectAt(DERAuthenticationStatement.TBS_SESSION_IDX));
        assertEquals(sessionId, resultSession.getString());
        DERVisibleString resultApplication = DERVisibleString.getInstance(tbsSequence
                .getObjectAt(DERAuthenticationStatement.TBS_APPLICATION_IDX));
        assertEquals(applicationId, resultApplication.getString());
        byte[] resultEncodedCert = tbsSequence.getObjectAt(DERAuthenticationStatement.TBS_AUTH_CERT_IDX).getDERObject()
                .getDEREncoded();
        assertTrue(Arrays.equals(resultEncodedCert, authCert.getEncoded()));

        DERBitString resultSignature = DERBitString.getInstance(sequence
                .getObjectAt(AbstractDERStatement.SIGNATURE_IDX));
        assertTrue(Arrays.equals(signature, resultSignature.getBytes()));
    }
}
