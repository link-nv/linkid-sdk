/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;


/**
 * <h2>{@link AbstractKeyStore}<br>
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
public abstract class AbstractKeyStore implements OlasKeyStore {

    /**
     * {@inheritDoc}
     */
    public PrivateKey getPrivateKey() {

        return _getPrivateKeyEntry().getPrivateKey();
    }

    /**
     * {@inheritDoc}
     */
    public X509Certificate getCertificate() {

        return (X509Certificate) _getPrivateKeyEntry().getCertificate();
    }

    /**
     * {@inheritDoc}
     */
    public KeyPair getKeyPair() {

        return new KeyPair(getCertificate().getPublicKey(), getPrivateKey());
    }
}
