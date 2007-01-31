/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.authentication;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

import net.link.safeonline.shared.asn1.authentication.DERAuthenticationStatement;

/**
 * Component for constructing the authentication statement. The authentication
 * statement links the session Id and application Id with an PKIX-based
 * authentication device.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationStatement {

	private final DERAuthenticationStatement derAuthenticationStatement;

	private final PrivateKey authenticationPrivateKey;

	public AuthenticationStatement(String sessionId, String applicationId,
			X509Certificate authenticationCertificate,
			PrivateKey authenticationPrivateKey) {
		this.derAuthenticationStatement = new DERAuthenticationStatement(
				sessionId, applicationId, authenticationCertificate);
		this.authenticationPrivateKey = authenticationPrivateKey;
	}

	public byte[] generateAuthenticationStatement() {
		byte[] tbs = this.derAuthenticationStatement.getToBeSigned();
		Signature signature;
		try {
			signature = Signature.getInstance("SHA1withRSA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA1withRSA algo not available");
		}
		try {
			signature.initSign(this.authenticationPrivateKey);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("invalid key: " + e.getMessage());
		}
		byte[] signatureValue;
		try {
			signature.update(tbs);
			signatureValue = signature.sign();
		} catch (SignatureException e) {
			throw new RuntimeException("signature error: " + e.getMessage());
		}
		this.derAuthenticationStatement.setSignature(signatureValue);
		return this.derAuthenticationStatement.getEncoded();
	}
}
