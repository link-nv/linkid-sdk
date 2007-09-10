/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.statement;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import net.link.safeonline.shared.asn1.statement.AbstractDERStatement;

abstract public class AbstractStatement {

	private final AbstractDERStatement derStatement;
	private final PrivateKey privateKey;

	public AbstractStatement(PrivateKey privateKey,
			AbstractDERStatement derStatement) {
		this.derStatement = derStatement;
		this.privateKey = privateKey;
	}

	public byte[] generateStatement() {
		byte[] tbs = this.derStatement.getToBeSignedEncoded();
		Signature signature;
		try {
			signature = Signature.getInstance("SHA1withRSA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA1withRSA algo not available");
		}
		try {
			signature.initSign(this.privateKey);
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
		this.derStatement.setSignature(signatureValue);
		return this.derStatement.getEncoded();
	}
}
