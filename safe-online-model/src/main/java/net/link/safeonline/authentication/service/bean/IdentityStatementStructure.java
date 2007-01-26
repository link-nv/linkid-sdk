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

import net.link.safeonline.shared.asn1.identity.DERIdentityStatement;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERVisibleString;

public class IdentityStatementStructure {

	private final ASN1Sequence sequence;

	private final ASN1Sequence tbsSequence;

	private final DERBitString signature;

	private final DERInteger version;

	private final DERVisibleString givenName;

	private final DERVisibleString surname;

	private final ASN1Sequence authCertificate;

	private final DERVisibleString user;

	public static IdentityStatementStructure getInstance(Object obj) {
		if (obj instanceof IdentityStatementStructure) {
			return (IdentityStatementStructure) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new IdentityStatementStructure((ASN1Sequence) obj);
		}
		throw new IllegalArgumentException("unknown object in factory");
	}

	public IdentityStatementStructure(ASN1Sequence sequence) {
		this.sequence = sequence;
		if (this.sequence.size() != 2) {
			throw new IllegalArgumentException(
					"sequence wrong size for an identity statement");
		}
		this.tbsSequence = (ASN1Sequence) this.sequence.getObjectAt(0);
		this.signature = DERBitString.getInstance(this.sequence.getObjectAt(1));

		if (this.tbsSequence.size() != 5) {
			throw new IllegalArgumentException(
					"sequence wrong size of TBS sequence of identity statement");
		}

		this.version = DERInteger.getInstance(this.tbsSequence
				.getObjectAt(DERIdentityStatement.VERSION_IDX));
		if (getVersion() != DERIdentityStatement.VERSION) {
			throw new IllegalArgumentException(
					"wrong identity statement version");
		}
		this.user = DERVisibleString.getInstance(this.tbsSequence
				.getObjectAt(DERIdentityStatement.USER_IDX));
		this.givenName = DERVisibleString.getInstance(this.tbsSequence
				.getObjectAt(DERIdentityStatement.GIVEN_NAME_IDX));
		this.surname = DERVisibleString.getInstance(this.tbsSequence
				.getObjectAt(DERIdentityStatement.SURNAME_IDX));
		this.authCertificate = ASN1Sequence.getInstance(this.tbsSequence
				.getObjectAt(DERIdentityStatement.AUTH_CERT_IDX));
	}

	public byte[] getToBeSignedData() {
		return this.tbsSequence.getDEREncoded();
	}

	public byte[] getSignature() {
		return this.signature.getBytes();
	}

	public int getVersion() {
		return this.version.getValue().intValue();
	}

	public String getGivenName() {
		return this.givenName.getString();
	}

	public String getSurname() {
		return this.surname.getString();
	}

	public String getUser() {
		return this.user.getString();
	}

	public X509Certificate getAuthenticationCertificate()
			throws CertificateException, IOException {
		CertificateFactory certificateFactory = CertificateFactory
				.getInstance("X.509");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				this.authCertificate.getEncoded());
		X509Certificate certificate = (X509Certificate) certificateFactory
				.generateCertificate(inputStream);
		return certificate;
	}
}
