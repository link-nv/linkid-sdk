/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;


/**
 * <h2>{@link OlasKeyStore}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 15, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public interface OlasKeyStore {

    /**
     * @return The application or node's {@link PrivateKeyEntry} for service authentication/identification purposes.
     */
    public PrivateKeyEntry _getPrivateKeyEntry();

    /**
     * @return The private key of this application.
     */
    public PrivateKey getPrivateKey();

    /**
     * @return The public certificate (chain) of this application.
     */
    public X509Certificate getCertificate();

    /**
     * @return A {@link KeyPair} containing the private and public key of this application.
     */
    public KeyPair getKeyPair();
}
