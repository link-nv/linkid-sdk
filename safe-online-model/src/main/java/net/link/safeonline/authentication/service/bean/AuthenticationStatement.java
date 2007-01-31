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

public class AuthenticationStatement {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationStatement.class);

	private final byte[] encodedAuthenticationStatement;

	private final AuthenticationStatementStructure authenticationStatementStructure;

	public AuthenticationStatement(byte[] encodedAuthenticationStatement) {
		this.encodedAuthenticationStatement = encodedAuthenticationStatement;

		ASN1Sequence sequence;
		try {
			sequence = ASN1Sequence.getInstance(ASN1Sequence
					.fromByteArray(this.encodedAuthenticationStatement));
		} catch (IOException e) {
			throw new IllegalArgumentException("identity statement IO error: "
					+ e.getMessage(), e);
		}
		this.authenticationStatementStructure = new AuthenticationStatementStructure(
				sequence);
	}

	// TODO: factor out common code with identity statement
	public X509Certificate verifyIntegrity() {
		X509Certificate authCert;
		try {
			authCert = this.authenticationStatementStructure
					.getAuthenticationCertificate();
		} catch (CertificateException e) {
			LOG.error("cert error: " + e.getMessage(), e);
			return null;
		} catch (IOException e) {
			LOG.error("IO error: " + e.getMessage(), e);
			return null;
		}
		byte[] data = this.authenticationStatementStructure.getToBeSignedData();
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
			boolean result = signature
					.verify(this.authenticationStatementStructure
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

	public String getSessionId() {
		return this.authenticationStatementStructure.getSessionId();
	}

	public String getApplicationId() {
		return this.authenticationStatementStructure.getApplicationId();
	}
}
