/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;
import static net.link.util.util.ObjectUtils.*;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Locale;
import net.link.util.config.KeyProvider;
import net.link.util.util.NNSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link AuthenticationContext}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class LogoutContext extends LinkIDContext {

    /**
     * @see #LogoutContext(String, KeyProvider, String)
     */
    public LogoutContext() {

        this( null, null, null );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be <code>null</code>, in which case
     *                        {@link AppLinkIDConfig#name()} will be used.
     * @param keyProvider     The store that will provide the necessary keys and certificates to authenticate and sign the application's
     *                        requests and responses or verify the linkID server's communications.  May be <code>null</code>, in which case
     *                        {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be <code>null</code>, in which case the user is sent to the application's
     *                        context path.
     *
     * @see #LogoutContext(String, String, KeyProvider, String, Locale, String)
     */
    public LogoutContext(@Nullable String applicationName, @Nullable KeyProvider keyProvider, @Nullable String target) {

        this( applicationName, null, keyProvider, null, null, target );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be <code>null</code>, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be <code>null</code>, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param keyProvider             The store that will provide the necessary keys and certificates to authenticate and sign the
     *                                application's requests and responses or verify the linkID server's communications.  May be
     *                                <code>null</code>, in which case {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be <code>null</code>, in which case the
     *                                user is sent to the application's context path.
     *
     * @see #LogoutContext(String, String, KeyPair, X509Certificate, Collection, X509Certificate, String, Locale, String, Protocol)
     */
    public LogoutContext(String applicationName, @Nullable String applicationFriendlyName, KeyProvider keyProvider, @Nullable String sessionTrackingId,
                         @Nullable Locale language, String target) {

        this( applicationName, applicationFriendlyName, ifNotNullElse( keyProvider, new NNSupplier<KeyProvider>() {
            @NotNull
            public KeyProvider get() {

                return config().linkID().app().keyProvider();
            }
        } ), sessionTrackingId, language, target, null );
    }

    private LogoutContext(String applicationName, String applicationFriendlyName, @NotNull KeyProvider keyProvider, String sessionTrackingId, Locale language,
                          String target, @Nullable Void v) {

        this( applicationName, applicationFriendlyName, //
                keyProvider.getIdentityKeyPair(), keyProvider.getIdentityCertificate(),  //
                keyProvider.getTrustedCertificates(), keyProvider.getTrustedCertificate( ConfigUtils.SSL_ALIAS ), //
                sessionTrackingId, language, target, null );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be <code>null</code>, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be <code>null</code>, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param applicationKeyPair      The application's key pair that will be used to sign the authentication request.
     * @param applicationCertificate  The certificate issued for the application's key pair.  It will be added to WS-Security headers for
     *                                purpose of server-side identification and verification. be <code>null</code>, in which case the
     *                                messages are only validated using the embedded certificate.
     * @param trustedCertificates     Used for validating whether incoming messages can be trusted.  The certificate chain in the incoming
     *                                message's signature is deemed trusted when the chain is valid, all certificates are valid, none are
     *                                revoked, and at least is in the set of trusted certificates.
     * @param sslCertificate          The linkID server's SSL certificate. It will be used to validate establishment of SSL-transport based
     *                                communication with the server. May be <code>null</code>, in which case no SSL certificate validation
     *                                will take place.
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be <code>null</code>, in which case the
     *                                user is sent to the application's context path.
     * @param protocol                The protocol to use for the communication between the application and the linkID services.  May be
     *                                <code>null</code>, in which case {@link ProtocolConfig#defaultProtocol()} will be used.
     */
    public LogoutContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair, X509Certificate applicationCertificate,
                         Collection<X509Certificate> trustedCertificates, X509Certificate sslCertificate, String sessionTrackingId, Locale language,
                         String target, @Nullable Protocol protocol) {

        super( applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate, trustedCertificates, sslCertificate, language, target,
                protocol );
    }

    @Override
    public String toString() {

        return String.format( "{logout: %s}", super.toString() );
    }
}
