/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model;

import java.net.URI;
import java.security.cert.X509Certificate;

import javax.ejb.Local;


/**
 * Validator for certificates through OCSP
 * 
 * @author dhouthoo
 * 
 */
@Local
public interface OcspValidator {

    public enum OcspResult {
        FAILED, UNKNOWN, REVOKED, SUSPENDED, GOOD
    }


    /**
     * Given an X509 certificate and its issuerCertificate, validates the certificate using OCSP
     * 
     * @param certificate
     * @param issuerCertificate
     */
    boolean performOcspCheck(X509Certificate certificate, X509Certificate issuerCertificate);

    /**
     * Extracts a OCSP URI from a certificate
     * 
     * @param certificate
     */
    URI getOcspUri(X509Certificate certificate);

    /**
     * Performs an OCSP check just like performOcspCheck but returns a finer grained response
     * 
     * @param ocspUri
     * @param certificate
     * @param issuerCertificate
     */
    OcspResult verifyOcspStatus(URI ocspUri, X509Certificate certificate, X509Certificate issuerCertificate);

}
