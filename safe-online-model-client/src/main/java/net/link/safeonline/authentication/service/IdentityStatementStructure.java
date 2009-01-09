/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.security.cert.X509Certificate;

import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.shared.asn1.statement.DERIdentityStatement;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERVisibleString;


public class IdentityStatementStructure extends AbstractStatementStructure {

    public IdentityStatementStructure(byte[] encodedIdentityStatement) throws DecodingException {

        super(encodedIdentityStatement);
    }


    private String          sessionId;

    private String          user;

    private String          operation;

    private String          givenName;

    private String          surname;

    private X509Certificate authCert;


    @Override
    protected void decode(ASN1Sequence tbsSequence)
            throws DecodingException {

        if (tbsSequence.size() != 7)
            throw new DecodingException();

        DERInteger version = DERInteger.getInstance(tbsSequence.getObjectAt(DERIdentityStatement.VERSION_IDX));
        if (version.getValue().intValue() != DERIdentityStatement.VERSION)
            throw new DecodingException();
        sessionId = DERVisibleString.getInstance(tbsSequence.getObjectAt(DERIdentityStatement.SESSION_IDX)).getString();
        user = DERVisibleString.getInstance(tbsSequence.getObjectAt(DERIdentityStatement.USER_IDX)).getString();
        operation = DERVisibleString.getInstance(tbsSequence.getObjectAt(DERIdentityStatement.OPERATION_IDX)).getString();
        givenName = DERVisibleString.getInstance(tbsSequence.getObjectAt(DERIdentityStatement.GIVEN_NAME_IDX)).getString();
        surname = DERVisibleString.getInstance(tbsSequence.getObjectAt(DERIdentityStatement.SURNAME_IDX)).getString();

        ASN1Sequence derAuthCert = ASN1Sequence.getInstance(tbsSequence.getObjectAt(DERIdentityStatement.AUTH_CERT_IDX));

        authCert = decodeCertificate(derAuthCert.getDEREncoded());
    }

    public String getGivenName() {

        return givenName;
    }

    public String getSurname() {

        return surname;
    }

    public String getUser() {

        return user;
    }

    public String getSessionId() {

        return sessionId;
    }

    public String getOperation() {

        return operation;
    }

    @Override
    public X509Certificate getCertificate() {

        return authCert;
    }
}
