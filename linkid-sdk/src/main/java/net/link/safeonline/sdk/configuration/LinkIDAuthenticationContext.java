/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import static net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder.config;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.util.config.KeyProvider;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link LinkIDAuthenticationContext}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDAuthenticationContext extends LinkIDContext {

    private boolean                         forceAuthentication;
    private Map<String, List<Serializable>> subjectAttributes;
    private LinkIDPaymentContext            paymentContext;
    private LinkIDCallback                  callback;

    /**
     * @see #LinkIDAuthenticationContext(String, KeyProvider, String)
     */
    public LinkIDAuthenticationContext() {

        this( null, null, (String) null );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be {@code null}, in which case
     *                        {@link LinkIDAppConfig#name()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be {@code null}, in which case the user is sent to the application's
     *                        context path.
     * @param protocol        Authentication protocol to use
     *
     * @see #LinkIDAuthenticationContext(String, String, KeyProvider, boolean, Locale, String)
     */
    public LinkIDAuthenticationContext(String applicationName, String target, LinkIDProtocol protocol) {

        this( applicationName, null, null, false, null, target, protocol );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be {@code null}, in which case
     *                        {@link LinkIDAppConfig#name()} will be used.
     * @param keyProvider     The provider that will provide the necessary keys and certificates to authenticate and sign the application's
     *                        requests and responses or verify the linkID server's communications.  May be {@code null}, in which case
     *                        {@link LinkIDAppConfig#keyProvider()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be {@code null}, in which case the user is sent to the application's
     *                        context path.
     *
     * @see #LinkIDAuthenticationContext(String, String, KeyProvider, boolean, Locale, String)
     */
    public LinkIDAuthenticationContext(@Nullable String applicationName, @Nullable KeyProvider keyProvider, String target) {

        this( applicationName, null, keyProvider, false, null, target );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be {@code null}, in
     *                                which case {@link LinkIDAppConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be {@code null}, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param keyProvider             The provider that will provide the necessary keys and certificates to authenticate and sign the
     *                                application's requests and responses or verify the linkID server's communications.  May be
     *                                {@code null}, in which case {@link LinkIDAppConfig#keyProvider()} will be used.
     * @param forceAuthentication     If {@code true}, users initiating authentication while in a live SSO environment will still be
     *                                required to fully identify and authenticate themselves with a device.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be {@code null}, in which case the
     *                                user is sent to the application's context path.
     *
     * @see #LinkIDAuthenticationContext(String, String, KeyPair, X509Certificate, Collection, X509Certificate, boolean, Locale, String, LinkIDProtocol)
     */
    public LinkIDAuthenticationContext(String applicationName, @Nullable String applicationFriendlyName, @Nullable KeyProvider keyProvider,
                                       boolean forceAuthentication, @Nullable Locale language, String target) {

        this( applicationName, applicationFriendlyName, null != keyProvider? keyProvider: config().linkID().app().keyProvider(), forceAuthentication, language,
                target, null );
    }

    public LinkIDAuthenticationContext(String applicationName, @Nullable String applicationFriendlyName, @Nullable KeyProvider keyProvider,
                                       boolean forceAuthentication, @Nullable Locale language, String target, @Nullable LinkIDProtocol protocol) {

        this( applicationName, applicationFriendlyName, //
                null != keyProvider? keyProvider.getIdentityKeyPair(): null, //
                null != keyProvider? keyProvider.getIdentityCertificate(): null,//
                null != keyProvider? keyProvider.getTrustedCertificates(): null, //
                null != keyProvider? keyProvider.getTrustedCertificate( LinkIDConfigUtils.SSL_ALIAS ): null, //
                forceAuthentication, language, target, protocol );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be {@code null}, in
     *                                which case {@link LinkIDAppConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be {@code null}, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param applicationKeyPair      The application's key pair that will be used to sign the authentication request.
     * @param applicationCertificate  The certificate issued for the application's key pair.  It will be added to WS-Security headers for
     *                                purpose of server-side identification and verification.
     * @param trustedCertificates     Used for validating whether incoming messages can be trusted.  The certificate chain in the incoming
     *                                message's signature is deemed trusted when the chain is valid, all certificates are valid, none are
     *                                revoked, and at least is in the set of trusted certificates.
     * @param sslCertificate          The linkID server's SSL certificate. It will be used to validate establishment of SSL-transport based
     *                                communication with the server. May be {@code null}, in which case no SSL certificate validation
     *                                will take place.
     * @param forceAuthentication     If {@code true}, users initiating authentication while in a live SSO environment will still be
     *                                required to fully identify and authenticate themselves with a device.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be {@code null}, in which case the
     *                                user is sent to the application's context path.
     * @param protocol                The protocol to use for the communication between the application and the linkID services.  May be
     *                                {@code null}, in which case {@link LinkIDProtocolConfig#defaultProtocol()} will be used.
     */
    public LinkIDAuthenticationContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
                                       X509Certificate applicationCertificate, Collection<X509Certificate> trustedCertificates, X509Certificate sslCertificate,
                                       boolean forceAuthentication, Locale language, String target, LinkIDProtocol protocol) {

        super( applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate, trustedCertificates, sslCertificate, language, target,
                protocol );

        this.forceAuthentication = forceAuthentication;
    }

    public boolean isForceAuthentication() {

        return forceAuthentication;
    }

    public void setForceAuthentication(final boolean forceAuthentication) {

        this.forceAuthentication = forceAuthentication;
    }

    public Map<String, List<Serializable>> getSubjectAttributes() {

        return subjectAttributes;
    }

    public void setSubjectAttributes(final Map<String, List<Serializable>> subjectAttributes) {

        this.subjectAttributes = subjectAttributes;
    }

    public LinkIDPaymentContext getPaymentContext() {

        return paymentContext;
    }

    public void setPaymentContext(final LinkIDPaymentContext paymentContext) {

        this.paymentContext = paymentContext;
    }

    public LinkIDCallback getCallback() {

        return callback;
    }

    public void setCallback(final LinkIDCallback callback) {

        this.callback = callback;
    }

    @Override
    public String toString() {

        return String.format( "{authn: %s, forceAuth=%s}", //
                super.toString(), isForceAuthentication() );
    }
}
