/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.identity;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

import net.link.safeonline.shared.asn1.identity.DERIdentityStatement;

/**
 * Component for construction of the identity statement.
 * 
 * @author fcorneli
 * 
 */
public class IdentityStatement {

	private final PrivateKey authenticationPrivateKey;

	private final DERIdentityStatement derIdentityStatement;

	public IdentityStatement(X509Certificate authenticationCertificate,
			String user, String givenName, String surname,
			PrivateKey authenticationPrivateKey) {
		this.derIdentityStatement = new DERIdentityStatement(
				authenticationCertificate, user, givenName, surname);
		this.authenticationPrivateKey = authenticationPrivateKey;
	}

	public byte[] generateIdentityStatement() {
		byte[] tbs = this.derIdentityStatement.getToBeSigned();
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
		this.derIdentityStatement.setSignature(signatureValue);
		return this.derIdentityStatement.getEncoded();
	}
}
