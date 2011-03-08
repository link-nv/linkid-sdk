/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * <h2>{@link AbstractKeyStore}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class AbstractKeyStore implements LinkIDKeyStore {

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

        return new KeyPair( getCertificate().getPublicKey(), getPrivateKey() );
    }

    /**
     * {@inheritDoc}
     */
    public List<X509Certificate> getCertificateChain() {

        KeyStore.PrivateKeyEntry privateKeyEntry = _getPrivateKeyEntry();
        List<X509Certificate> certificateChain = new LinkedList<X509Certificate>();
        for (Certificate certificate : privateKeyEntry.getCertificateChain()) {
            certificateChain.add( (X509Certificate) certificate );
        }
        return certificateChain;
    }

    /**
     * {@inheritDoc}
     */
    public List<X509Certificate> getCertificates(String prefix) {

        List<X509Certificate> certificates = new LinkedList<X509Certificate>();
        for (Map.Entry<String, X509Certificate> certificateEntry : getOtherCertificates().entrySet()) {

            if (certificateEntry.getKey().equals( prefix ) || certificateEntry.getKey().startsWith( prefix + '.' )) {
                certificates.add( certificateEntry.getValue() );
            }
        }

        return certificates;
    }
}
