/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;


/**
 * <h2>{@link LinkIDKeyStore}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public interface LinkIDKeyStore extends Serializable {

    /**
     * The alias that identifies LinkID's service certificate.
     */
    String LINKID_SERVICE_ALIAS = "linkID_service";

    /**
     * The alias that identifies LinkID's service root certificate.
     */
    String LINKID_SERVICE_ROOT_ALIAS = "linkID_service_root";

    /**
     * The alias that identifies LinkID's SSL certificate.
     */
    String LINKID_SSL_ALIAS = "linkID_ssl";

    /**
     * @return The {@link PrivateKeyEntry} that identifies and authenticates the owning application or node in his communications.
     */
    PrivateKeyEntry _getPrivateKeyEntry();

    /**
     * @return The private key of the owning application or node.
     */
    PrivateKey getPrivateKey();

    /**
     * @return The public certificate of the owning application or node.
     */
    X509Certificate getCertificate();

    /**
     * @return The public certificate chain of the owning application or node.
     */
    List<X509Certificate> getCertificateChain();

    /**
     * @return Any other certificates provided by this store.
     */
    Map<String, X509Certificate> getOtherCertificates();

    /**
     * @param prefix the prefix of the certificate alias
     *
     * @return list of certificates who's alias equals the specified prefix or is <code><prefix.xxx></code>
     */
    List<X509Certificate> getCertificates(String prefix);

    /**
     * @return A {@link KeyPair} containing the private and public key of the owning application or node.
     */
    KeyPair getKeyPair();
}
