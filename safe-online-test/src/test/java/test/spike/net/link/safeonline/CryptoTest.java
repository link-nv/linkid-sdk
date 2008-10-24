/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import junit.framework.TestCase;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;


public class CryptoTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(CryptoTest.class);


    public void testSignAndVerify() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        byte[] data = "hello world".getBytes();

        // operate
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        byte[] signatureResult = signature.sign();

        // verify
        assertNotNull(signatureResult);
        signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(keyPair.getPublic());
        signature.update(data);
        boolean verifyResult = signature.verify(signatureResult);
        assertTrue(verifyResult);

        byte[] fakeData = "foobar".getBytes();
        signature.initVerify(keyPair.getPublic());
        signature.update(fakeData);
        boolean fakeVerifyResult = signature.verify(signatureResult);
        LOG.debug("signature result size: " + signatureResult.length);
        assertFalse(fakeVerifyResult);

        boolean fakeSignatureValueResult = signature.verify("foobar-signature".getBytes());
        assertFalse(fakeSignatureValueResult);
    }

    public void testAsn1Der() throws Exception {

        int testInt = 1234;
        DERInteger integer = new DERInteger(testInt);
        byte[] derEncoded = integer.getDEREncoded();
        LOG.debug("DER encoded integer " + testInt + " size: " + derEncoded.length);
        LOG.debug("DER encoded integer value: " + new String(Hex.encode(derEncoded)));
        LOG.debug("Version: " + new String(Hex.encode(new DERInteger(1).getDEREncoded())));
        LOG.debug("SEQUENCE { version INTEGER }: " + new String(Hex.encode(new DERSequence(new DERInteger(1)).getDEREncoded())));
        ASN1Object asn1Object = new DERSequence(new DERInteger(1));
        LOG.debug("ASN1 encoded: " + new String(Hex.encode(asn1Object.getDEREncoded())));
        LOG.debug("ASN1 dump: " + ASN1Dump.dumpAsString(asn1Object));

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        LOG.debug("ASN1 dump of X509 cert: " + ASN1Dump.dumpAsString(ASN1Object.fromByteArray(certificate.getEncoded())));

        byte[] encodedObject = asn1Object.getEncoded();
        ASN1Object resultObject = ASN1Object.fromByteArray(encodedObject);
        LOG.debug("result object type: " + resultObject.getClass().getName());
    }

    public void testCustomSecurityProvider() throws Exception {

    }

    public void testEncryptDecrypt() throws Exception {

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", bcp);
        keyGen.init(128, new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();

        String data = "userId=test-login;applicationId=test-application;device=password;time=2008-09-09T08:38:41.624+02:00";

        Cipher encryptCipher = Cipher.getInstance("AES", bcp);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = encryptCipher.doFinal(data.getBytes());
        String encryptedValue = new String(Base64.encode(encryptedBytes));

        Cipher decryptCipher = Cipher.getInstance("AES", bcp);
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = decryptCipher.doFinal(Base64.decode(encryptedValue.getBytes()));
        String decryptedData = new String(decryptedBytes);

        assertEquals(data, decryptedData);
    }
}
