/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Sequence;

public class IdentityStatement {

	private final byte[] encodedIdentityStatement;

	private final IdentityStatementStructure identityStatementStructure;

	private static final Log LOG = LogFactory.getLog(IdentityStatement.class);

	public IdentityStatement(byte[] encodedIdentityStatement) {
		this.encodedIdentityStatement = encodedIdentityStatement;
		ASN1Sequence sequence;
		try {
			sequence = ASN1Sequence.getInstance(ASN1Sequence
					.fromByteArray(this.encodedIdentityStatement));
		} catch (IOException e) {
			throw new IllegalArgumentException("identity statement IO error: "
					+ e.getMessage(), e);
		}
		this.identityStatementStructure = new IdentityStatementStructure(
				sequence);
	}

	/**
	 * Verifies the integrity of the identity statement.
	 * 
	 * @return the authentication certificate, or <code>null</code> if
	 *         integrity check failed.
	 */
	public X509Certificate verifyIntegrity() {
		X509Certificate authCert;
		try {
			authCert = this.identityStatementStructure
					.getAuthenticationCertificate();
		} catch (CertificateException e) {
			LOG.error("cert error: " + e.getMessage(), e);
			return null;
		} catch (IOException e) {
			LOG.error("IO error: " + e.getMessage(), e);
			return null;
		}
		byte[] data = this.identityStatementStructure.getToBeSignedData();
		Signature signature;
		try {
			signature = Signature.getInstance("SHA1withRSA");
		} catch (NoSuchAlgorithmException e) {
			LOG.error("sign algo error: " + e.getMessage(), e);
			return null;
		}
		try {
			signature.initVerify(authCert);
		} catch (InvalidKeyException e) {
			LOG.error("Invalid key: " + e.getMessage(), e);
			return null;
		}
		try {
			signature.update(data);
			boolean result = signature.verify(this.identityStatementStructure
					.getSignature());
			if (result) {
				return authCert;
			} else {
				return null;
			}
		} catch (SignatureException e) {
			LOG.error("signature error: " + e.getMessage());
			return null;
		}
	}

	public String getGivenName() {
		return this.identityStatementStructure.getGivenName();
	}

	public String getSurname() {
		return this.identityStatementStructure.getSurname();
	}
}
