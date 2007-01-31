/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import net.link.safeonline.shared.asn1.authentication.DERAuthenticationStatement;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERVisibleString;

public class AuthenticationStatementStructure {

	private final byte[] signature;

	private final String sessionId;

	private final String applicationId;

	private final byte[] authCert;

	private final byte[] toBeSignedData;

	public static AuthenticationStatementStructure getInstance(Object obj) {
		if (obj instanceof AuthenticationStatementStructure) {
			return (AuthenticationStatementStructure) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new AuthenticationStatementStructure((ASN1Sequence) obj);
		}
		throw new IllegalArgumentException("unknown object in factory");
	}

	public AuthenticationStatementStructure(ASN1Sequence sequence) {
		if (sequence.size() != 2) {
			throw new IllegalArgumentException(
					"sequence wrong size for an auth statement");
		}
		ASN1Sequence tbsSequence = ASN1Sequence.getInstance(sequence
				.getObjectAt(DERAuthenticationStatement.TBS_IDX));
		if (tbsSequence.size() != 4) {
			throw new IllegalArgumentException(
					"tbs sequence wrong size for an auth statement");
		}
		this.toBeSignedData = tbsSequence.getDEREncoded();
		DERInteger version = DERInteger.getInstance(tbsSequence
				.getObjectAt(DERAuthenticationStatement.TBS_VERSION_IDX));
		if (DERAuthenticationStatement.VERSION != version.getValue().intValue()) {
			throw new IllegalArgumentException("wrong auth statement version");
		}
		DERVisibleString derSessionId = DERVisibleString
				.getInstance(tbsSequence
						.getObjectAt(DERAuthenticationStatement.TBS_SESSION_IDX));
		this.sessionId = derSessionId.getString();
		DERVisibleString derApplicationId = DERVisibleString
				.getInstance(tbsSequence
						.getObjectAt(DERAuthenticationStatement.TBS_APPLICATION_IDX));
		this.applicationId = derApplicationId.getString();
		DERBitString derSignature = DERBitString.getInstance(sequence
				.getObjectAt(DERAuthenticationStatement.SIGNATURE_IDX));
		ASN1Sequence derAuthCert = ASN1Sequence.getInstance(tbsSequence
				.getObjectAt(DERAuthenticationStatement.TBS_AUTH_CERT_IDX));
		this.authCert = derAuthCert.getDEREncoded();
		this.signature = derSignature.getBytes();
	}

	public byte[] getSignature() {
		return this.signature;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	public byte[] getToBeSignedData() {
		return this.toBeSignedData;
	}

	public X509Certificate getAuthenticationCertificate()
			throws CertificateException, IOException {
		CertificateFactory certificateFactory = CertificateFactory
				.getInstance("X.509");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				this.authCert);
		X509Certificate certificate = (X509Certificate) certificateFactory
				.generateCertificate(inputStream);
		return certificate;
	}
}
