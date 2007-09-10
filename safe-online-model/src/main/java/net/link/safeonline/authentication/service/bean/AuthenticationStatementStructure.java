/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;

import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.shared.asn1.statement.DERAuthenticationStatement;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERVisibleString;

public class AuthenticationStatementStructure extends
		AbstractStatementStructure {

	private String sessionId;

	private String applicationId;

	private X509Certificate authCert;

	public AuthenticationStatementStructure(
			byte[] encodedAuthenticationStatement) throws DecodingException {
		super(encodedAuthenticationStatement);
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	@Override
	protected void decode(ASN1Sequence tbsSequence) throws DecodingException {
		if (tbsSequence.size() != 4) {
			throw new DecodingException();
		}
		DERInteger version = DERInteger.getInstance(tbsSequence
				.getObjectAt(DERAuthenticationStatement.TBS_VERSION_IDX));
		if (DERAuthenticationStatement.VERSION != version.getValue().intValue()) {
			throw new DecodingException();
		}
		DERVisibleString derSessionId = DERVisibleString
				.getInstance(tbsSequence
						.getObjectAt(DERAuthenticationStatement.TBS_SESSION_IDX));
		this.sessionId = derSessionId.getString();
		DERVisibleString derApplicationId = DERVisibleString
				.getInstance(tbsSequence
						.getObjectAt(DERAuthenticationStatement.TBS_APPLICATION_IDX));
		this.applicationId = derApplicationId.getString();
		ASN1Sequence derAuthCert = ASN1Sequence.getInstance(tbsSequence
				.getObjectAt(DERAuthenticationStatement.TBS_AUTH_CERT_IDX));
		this.authCert = decodeCertificate(derAuthCert.getDEREncoded());
	}

	@Override
	protected X509Certificate getCertificate() {
		return this.authCert;
	}
}
