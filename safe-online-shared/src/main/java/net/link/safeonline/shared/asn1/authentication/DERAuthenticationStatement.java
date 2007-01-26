/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1.authentication;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import net.link.safeonline.shared.asn1.DERBitString;
import net.link.safeonline.shared.asn1.DEREncodable;
import net.link.safeonline.shared.asn1.DEREncodedData;
import net.link.safeonline.shared.asn1.DERInteger;
import net.link.safeonline.shared.asn1.DERSequence;
import net.link.safeonline.shared.asn1.DERVisibleString;

public class DERAuthenticationStatement implements DEREncodable {

	public static final int VERSION = 1;

	public static final int TBS_IDX = 0;

	public static final int SIGNATURE_IDX = 1;

	public static final int TBS_VERSION_IDX = 0;

	public static final int TBS_SESSION_IDX = 1;

	public static final int TBS_AUTH_CERT_IDX = 2;

	private final String sessionId;

	private final X509Certificate authenticationCertificate;

	private byte[] signature;

	public DERAuthenticationStatement(String sessionId,
			X509Certificate authenticationCertificate) {
		this.sessionId = sessionId;
		this.authenticationCertificate = authenticationCertificate;
	}

	public byte[] getToBeSigned() {
		DERSequence tbsSequence = new DERSequence();
		DERInteger version = new DERInteger(VERSION);
		tbsSequence.add(version);
		DERVisibleString session = new DERVisibleString(this.sessionId);
		tbsSequence.add(session);
		DEREncodedData encodedCert;
		try {
			encodedCert = new DEREncodedData(this.authenticationCertificate
					.getEncoded());
		} catch (CertificateEncodingException e) {
			throw new RuntimeException("cert encoding error: " + e.getMessage());
		}
		tbsSequence.add(encodedCert);
		return tbsSequence.getEncoded();
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public byte[] getEncoded() {
		DEREncodedData tbs = new DEREncodedData(getToBeSigned());

		DERSequence sequence = new DERSequence();
		sequence.add(tbs);
		if (null == this.signature) {
			throw new IllegalStateException("set signature value first");
		}
		DERBitString signatureBitString = new DERBitString(this.signature);
		sequence.add(signatureBitString);
		return sequence.getEncoded();
	}
}
