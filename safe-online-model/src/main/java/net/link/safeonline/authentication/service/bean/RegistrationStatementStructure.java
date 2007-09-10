/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;

import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.shared.asn1.statement.DERRegistrationStatement;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERVisibleString;

public class RegistrationStatementStructure extends AbstractStatementStructure {

	private String sessionId;

	private String applicationId;

	private X509Certificate authCert;

	private String username;

	public RegistrationStatementStructure(byte[] encodedRegistrationStatement)
			throws DecodingException {
		super(encodedRegistrationStatement);
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	public String getUsername() {
		return this.username;
	}

	@Override
	protected void decode(ASN1Sequence tbsSequence) throws DecodingException {
		if (tbsSequence.size() != DERRegistrationStatement.TBS_SIZE) {
			throw new DecodingException();
		}

		DERInteger version = DERInteger.getInstance(tbsSequence
				.getObjectAt(DERRegistrationStatement.TBS_VERSION_IDX));
		if (DERRegistrationStatement.VERSION != version.getValue().intValue()) {
			throw new DecodingException();
		}

		DERVisibleString derUsername = DERVisibleString.getInstance(tbsSequence
				.getObjectAt(DERRegistrationStatement.TBS_USER_IDX));
		this.username = derUsername.getString();

		DERVisibleString derSessionId = DERVisibleString
				.getInstance(tbsSequence
						.getObjectAt(DERRegistrationStatement.TBS_SESSION_IDX));
		this.sessionId = derSessionId.getString();

		DERVisibleString derApplicationId = DERVisibleString
				.getInstance(tbsSequence
						.getObjectAt(DERRegistrationStatement.TBS_APPLICATION_IDX));
		this.applicationId = derApplicationId.getString();

		ASN1Sequence derAuthCert = ASN1Sequence.getInstance(tbsSequence
				.getObjectAt(DERRegistrationStatement.TBS_AUTH_CERT_IDX));
		this.authCert = decodeCertificate(derAuthCert.getDEREncoded());
	}

	@Override
	protected X509Certificate getCertificate() {
		return this.authCert;
	}
}
