/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;


/**
 * WS-Security configuration service interface.
 * 
 * @author wvdhaute
 * 
 */
public interface WSSecurityConfigurationService {

    /**
     * Returns the maximum offset of the WS-Security timestamp.
     * 
     */
    public long getMaximumWsSecurityTimestampOffset();

    /**
     * Given the calling entity's certificate, skip or perform a verification of the digestion of the SOAP body element
     * by the WS-Security signature.
     * 
     * @param certificate
     */
    public boolean skipMessageIntegrityCheck(X509Certificate certificate);

    /**
     * Returns the certificate which will be used to sign web service response messages.
     * 
     */
    public X509Certificate getCertificate();

    /**
     * Returns the private key which will be used to sign web service response messages.
     * 
     */
    public PrivateKey getPrivateKey();

}
