/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;


/**
 * Interface for component that manages the different PKI providers.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface PkiProviderManager {

    /**
     * Finds the PKI provider that could possibly help in validate the given certificate. If there exists no PKI
     * provider within the system that could handle the certificate's validation process, this method returns
     * <code>null</code>.
     * 
     * @param certificate
     * @return the PKI provider, or <code>null</code> if no existing PKI provider could handle the certificate's
     *         validation.
     */
    PkiProvider findPkiProvider(X509Certificate certificate);
}
