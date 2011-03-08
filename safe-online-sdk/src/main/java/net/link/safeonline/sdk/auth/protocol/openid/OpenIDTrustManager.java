/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.openid;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * OpenID Trust Manager to install to override the default set of trusted SSL certificates.
 *
 * Used by {@link OpenIDSSLSocketFactory}.
 *
 * @author Wim Vandenhaute
 */
public class OpenIDTrustManager implements X509TrustManager {

    private static final Log LOG = LogFactory.getLog( OpenIDTrustManager.class );

    private final X509Certificate serverCertificate;

    private X509TrustManager defaultTrustManager;

    /**
     * Allows all server certificates.
     */
    public OpenIDTrustManager() {
        serverCertificate = null;
        defaultTrustManager = null;
    }

    /**
     * Trust only the given server certificate, and the default trusted server certificates.
     *
     * @param serverCertificate SSL certificate to trust
     *
     * @throws NoSuchAlgorithmException could not get an SSLContext instance
     * @throws KeyStoreException        failed to intialize the {@link OpenIDTrustManager}
     */
    public OpenIDTrustManager(X509Certificate serverCertificate)
            throws NoSuchAlgorithmException, KeyStoreException {
        this.serverCertificate = serverCertificate;
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance( algorithm );
        trustManagerFactory.init( (KeyStore) null );
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                defaultTrustManager = (X509TrustManager) trustManager;
                break;
            }
        }
        if (null == defaultTrustManager) {
            throw new IllegalStateException( "no default X509 trust manager found" );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

        LOG.error( "checkClientTrusted" );
        if (null != defaultTrustManager) {
            defaultTrustManager.checkClientTrusted( chain, authType );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

        LOG.debug( "check server trusted" );
        LOG.debug( "auth type: " + authType );
        if (null == serverCertificate) {
            LOG.debug( "trusting all server certificates" );
            return;
        }
        if (!serverCertificate.equals( chain[0] )) {
            throw new CertificateException( "untrusted server certificate" );
        }
    }

    /**
     * {@inheritDoc}
     */
    public X509Certificate[] getAcceptedIssuers() {

        LOG.error( "getAcceptedIssuers" );
        if (null == defaultTrustManager) {
            return null;
        }
        return defaultTrustManager.getAcceptedIssuers();
    }
}
