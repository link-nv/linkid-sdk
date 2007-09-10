/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.asn1.statement;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import net.link.safeonline.shared.asn1.DEREncodable;
import net.link.safeonline.shared.asn1.DEREncodedData;
import net.link.safeonline.shared.asn1.DERInteger;
import net.link.safeonline.shared.asn1.DERSequence;
import net.link.safeonline.shared.asn1.DERVisibleString;

public class DERIdentityStatement extends AbstractDERStatement {

	public static final int VERSION = 1;

	public static final int VERSION_IDX = 0;

	public static final int USER_IDX = 1;

	public static final int GIVEN_NAME_IDX = 2;

	public static final int SURNAME_IDX = 3;

	public static final int AUTH_CERT_IDX = 4;

	private final X509Certificate authenticationCertificate;

	private final String user;

	private final String givenName;

	private final String surname;

	public DERIdentityStatement(X509Certificate authenticationCertificate,
			String user, String givenName, String surname) {
		this.authenticationCertificate = authenticationCertificate;
		this.user = user;
		this.givenName = givenName;
		this.surname = surname;
	}

	public DEREncodable getToBeSigned() {
		DERSequence tbsSequence = new DERSequence();
		DERInteger version = new DERInteger(VERSION);
		tbsSequence.add(version);
		DERVisibleString derUser = new DERVisibleString(this.user);
		tbsSequence.add(derUser);
		DERVisibleString derGivenName = new DERVisibleString(this.givenName);
		tbsSequence.add(derGivenName);
		DERVisibleString derSurname = new DERVisibleString(this.surname);
		tbsSequence.add(derSurname);
		DEREncodedData encodedCert;
		try {
			encodedCert = new DEREncodedData(this.authenticationCertificate
					.getEncoded());
		} catch (CertificateEncodingException e) {
			throw new RuntimeException("cert encoding error: " + e.getMessage());
		}
		tbsSequence.add(encodedCert);
		return tbsSequence;
	}
}
