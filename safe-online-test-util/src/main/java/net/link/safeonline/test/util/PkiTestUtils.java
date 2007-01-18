/*
 * SafeOnline project.
 * 
 * Copyright 2005-2006 Frank Cornelis.
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.joda.time.DateTime;

public class PkiTestUtils {

	private PkiTestUtils() {
		// empty
	}

	static {
		/*
		 * XXX: It's possible that we need to do something similar later on
		 * within the SafeOnline application itself, i.e., lifecycle management
		 * of the BouncyCastle crypto provider.
		 */
		if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = new SecureRandom();
		keyPairGenerator.initialize(new RSAKeyGenParameterSpec(1024,
				RSAKeyGenParameterSpec.F4), random);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		return keyPair;
	}

	public static X509Certificate generateSelfSignedCertificate(
			KeyPair keyPair, String dn, DateTime notBefore, DateTime notAfter,
			String signatureAlgorithm, boolean caCert,
			boolean timeStampingPurpose) {
		X509Certificate certificate = generateCertificate(keyPair.getPublic(),
				dn, keyPair.getPrivate(), null, notBefore, notAfter,
				signatureAlgorithm, caCert, timeStampingPurpose);
		return certificate;
	}

	public static X509Certificate generateSelfSignedCertificate(
			KeyPair keyPair, String dn) {
		DateTime now = new DateTime();
		DateTime future = now.plusYears(10);
		X509Certificate certificate = generateSelfSignedCertificate(keyPair,
				dn, now, future, null, true, false);
		return certificate;
	}

	public static X509Certificate generateCertificate(
			PublicKey subjectPublicKey, String subjectDn,
			PrivateKey issuerPrivateKey, X509Certificate issuerCert,
			DateTime notBefore, DateTime notAfter, String signatureAlgorithm,
			boolean caCert, boolean timeStampingPurpose) {
		if (null == signatureAlgorithm) {
			signatureAlgorithm = "SHA512WithRSAEncryption";
		}
		X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
		certificateGenerator.reset();
		certificateGenerator.setPublicKey(subjectPublicKey);
		certificateGenerator.setSignatureAlgorithm(signatureAlgorithm);
		certificateGenerator.setNotBefore(notBefore.toDate());
		certificateGenerator.setNotAfter(notAfter.toDate());
		X509Principal issuerDN;
		if (null != issuerCert) {
			issuerDN = new X509Principal(issuerCert.getSubjectX500Principal()
					.toString());
		} else {
			issuerDN = new X509Principal(subjectDn);
		}
		certificateGenerator.setIssuerDN(issuerDN);
		certificateGenerator.setSubjectDN(new X509Principal(subjectDn));
		certificateGenerator.setSerialNumber(new BigInteger(128,
				new SecureRandom()));

		try {
			certificateGenerator.addExtension(
					X509Extensions.SubjectKeyIdentifier, false,
					createSubjectKeyId(subjectPublicKey));
			PublicKey issuerPublicKey;
			if (null != issuerCert) {
				issuerPublicKey = issuerCert.getPublicKey();
			} else {
				issuerPublicKey = subjectPublicKey;
			}
			certificateGenerator.addExtension(
					X509Extensions.AuthorityKeyIdentifier, false,
					createAuthorityKeyId(issuerPublicKey));
		} catch (IOException e) {
			throw new RuntimeException("error adding extensions: "
					+ e.getMessage(), e);
		}

		certificateGenerator.addExtension(X509Extensions.BasicConstraints,
				false, new BasicConstraints(caCert));

		if (timeStampingPurpose) {
			certificateGenerator.addExtension(X509Extensions.ExtendedKeyUsage,
					true, new ExtendedKeyUsage(new DERSequence(
							KeyPurposeId.id_kp_timeStamping)));
		}

		X509Certificate certificate;
		try {
			certificate = certificateGenerator.generate(issuerPrivateKey);
		} catch (Exception e) {
			throw new RuntimeException("certificate generator error: "
					+ e.getMessage(), e);
		}
		return certificate;
	}

	private static SubjectKeyIdentifier createSubjectKeyId(PublicKey publicKey)
			throws IOException {

		ByteArrayInputStream bais = new ByteArrayInputStream(publicKey
				.getEncoded());
		SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
				(ASN1Sequence) new ASN1InputStream(bais).readObject());
		return new SubjectKeyIdentifier(info);
	}

	private static AuthorityKeyIdentifier createAuthorityKeyId(
			PublicKey publicKey) throws IOException {

		ByteArrayInputStream bais = new ByteArrayInputStream(publicKey
				.getEncoded());
		SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
				(ASN1Sequence) new ASN1InputStream(bais).readObject());

		return new AuthorityKeyIdentifier(info);
	}
}
