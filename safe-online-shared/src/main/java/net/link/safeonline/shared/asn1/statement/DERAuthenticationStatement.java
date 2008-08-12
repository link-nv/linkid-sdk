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

public class DERAuthenticationStatement extends AbstractDERStatement {

    public static final int       VERSION             = 1;

    public static final int       TBS_VERSION_IDX     = 0;

    public static final int       TBS_SESSION_IDX     = 1;

    public static final int       TBS_APPLICATION_IDX = 2;

    public static final int       TBS_AUTH_CERT_IDX   = 3;

    private final String          sessionId;

    private final String          applicationId;

    private final X509Certificate authenticationCertificate;


    public DERAuthenticationStatement(String sessionId, String applicationId,
            X509Certificate authenticationCertificate) {

        this.sessionId = sessionId;
        this.applicationId = applicationId;
        this.authenticationCertificate = authenticationCertificate;
    }

    @Override
    public DEREncodable getToBeSigned() {

        DERSequence tbsSequence = new DERSequence();
        DERInteger version = new DERInteger(VERSION);
        tbsSequence.add(version);
        DERVisibleString session = new DERVisibleString(this.sessionId);
        tbsSequence.add(session);
        DERVisibleString application = new DERVisibleString(this.applicationId);
        tbsSequence.add(application);
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
