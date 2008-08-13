/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;


/**
 * Interface for Identity Service JMX bean.
 *
 * @author fcorneli
 *
 */
public interface IdentityServiceMBean {

    /*
     * Operations.
     */
    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    X509Certificate getCertificate();

    void loadKeyPair();

    /*
     * Attributes.
     */
    void setKeyStoreResource(String keyStoreResource);

    String getKeyStoreResource();

    void setKeyStoreFile(String keyStoreFile);

    String getKeyStoreFile();

    void setKeyStorePassword(String keyStorePassword);

    String getKeyStorePassword();

    void setKeyStoreType(String keyStoreType);

    String getKeyStoreType();
}
