/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth;

import static net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder.config;
import static net.link.util.util.ObjectUtils.ifNotNullElse;
import static net.link.util.util.ObjectUtils.ifNotNullElseNullable;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.configuration.LinkIDAppConfig;
import net.link.safeonline.sdk.configuration.LinkIDConfigUtils;
import net.link.safeonline.sdk.configuration.LinkIDProtocol;
import net.link.safeonline.sdk.configuration.LinkIDProtocolConfig;
import net.link.safeonline.sdk.configuration.LinkIDSAMLBinding;
import net.link.safeonline.sdk.configuration.LinkIDSAMLProtocolConfig;
import net.link.util.config.KeyProvider;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDAuthenticationContext implements Serializable {

    private String                          applicationName;
    private String                          applicationFriendlyName;
    //
    private KeyPair                         applicationKeyPair;
    private X509Certificate                 applicationCertificate;
    private Collection<X509Certificate>     trustedCertificates;
    //
    private Locale                          language;
    private String                          target;
    private String                          landingUrl;                 // optional landing url, if not specified is constructed in {@LinkIDRequestConfig}
    //
    private LinkIDProtocol                  protocol;
    private SAMLContext                     saml;
    //
    private String                          authenticationMessage;
    private String                          finishedMessage;
    private String                          identityProfile;
    private Long                            sessionExpiryOverride;
    private String                          theme;
    //
    private String                          mobileLandingSuccess;       // landing page for an authn/payment started on iOS browser
    private String                          mobileLandingError;         // landing page for an authn/payment started on iOS browser
    private String                          mobileLandingCancel;        // landing page for an authn/payment started on iOS browser
    //
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

        this( applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate, trustedCertificates, sslCertificate, language, target,
                protocol );
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
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be {@code null}, in which case the
     *                                user is sent to the application's context path.
     * @param protocol                The protocol to use for the communication between the application and the linkID services.  May be
     *                                {@code null}, in which case {@link LinkIDProtocolConfig#defaultProtocol()} will be used.
     */
    private LinkIDAuthenticationContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
                                        X509Certificate applicationCertificate, Collection<X509Certificate> trustedCertificates, X509Certificate sslCertificate,
                                        Locale language, String target, LinkIDProtocol protocol) {

        saml = new SAMLContext();

        this.applicationName = null != applicationName? applicationName: config().linkID().app().name();
        this.applicationFriendlyName = applicationFriendlyName;
        this.applicationKeyPair = applicationKeyPair;
        this.applicationCertificate = applicationCertificate;
        this.trustedCertificates = trustedCertificates;
        this.language = ifNotNullElseNullable( language, config().linkID().language() );
        this.target = target;
        this.protocol = ifNotNullElse( protocol, config().proto().defaultProtocol() );
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDAuthenticationContext{" +
               "applicationName='" + applicationName + '\'' +
               ", applicationFriendlyName='" + applicationFriendlyName + '\'' +
               ", applicationCertificate=" + applicationCertificate +
               ", trustedCertificates=" + trustedCertificates +
               ", language=" + language +
               ", target='" + target + '\'' +
               ", landingUrl='" + landingUrl + '\'' +
               ", protocol=" + protocol +
               ", saml=" + saml +
               ", authenticationMessage='" + authenticationMessage + '\'' +
               ", finishedMessage='" + finishedMessage + '\'' +
               ", identityProfile='" + identityProfile + '\'' +
               ", sessionExpiryOverride=" + sessionExpiryOverride +
               ", theme='" + theme + '\'' +
               ", mobileLandingSuccess='" + mobileLandingSuccess + '\'' +
               ", mobileLandingError='" + mobileLandingError + '\'' +
               ", mobileLandingCancel='" + mobileLandingCancel + '\'' +
               ", subjectAttributes=" + subjectAttributes +
               ", paymentContext=" + paymentContext +
               ", callback=" + callback +
               '}';
    }

    // Accessors

    public String getApplicationName() {

        return applicationName;
    }

    public void setApplicationName(final String applicationName) {

        this.applicationName = applicationName;
    }

    public String getApplicationFriendlyName() {

        return applicationFriendlyName;
    }

    public void setApplicationFriendlyName(final String applicationFriendlyName) {

        this.applicationFriendlyName = applicationFriendlyName;
    }

    public KeyPair getApplicationKeyPair() {

        return applicationKeyPair;
    }

    public void setApplicationKeyPair(final KeyPair applicationKeyPair) {

        this.applicationKeyPair = applicationKeyPair;
    }

    public X509Certificate getApplicationCertificate() {

        return applicationCertificate;
    }

    public void setApplicationCertificate(final X509Certificate applicationCertificate) {

        this.applicationCertificate = applicationCertificate;
    }

    public Collection<X509Certificate> getTrustedCertificates() {

        return trustedCertificates;
    }

    public void setTrustedCertificates(final Collection<X509Certificate> trustedCertificates) {

        this.trustedCertificates = trustedCertificates;
    }

    public Locale getLanguage() {

        return language;
    }

    public void setLanguage(final Locale language) {

        this.language = language;
    }

    public String getTarget() {

        return target;
    }

    public void setTarget(final String target) {

        this.target = target;
    }

    public String getLandingUrl() {

        return landingUrl;
    }

    public void setLandingUrl(final String landingUrl) {

        this.landingUrl = landingUrl;
    }

    public LinkIDProtocol getProtocol() {

        return protocol;
    }

    public void setProtocol(final LinkIDProtocol protocol) {

        this.protocol = protocol;
    }

    public SAMLContext getSaml() {

        return saml;
    }

    public void setSaml(final SAMLContext saml) {

        this.saml = saml;
    }

    public String getAuthenticationMessage() {

        return authenticationMessage;
    }

    public void setAuthenticationMessage(final String authenticationMessage) {

        this.authenticationMessage = authenticationMessage;
    }

    public String getFinishedMessage() {

        return finishedMessage;
    }

    public void setFinishedMessage(final String finishedMessage) {

        this.finishedMessage = finishedMessage;
    }

    public String getIdentityProfile() {

        return identityProfile;
    }

    public void setIdentityProfile(final String identityProfile) {

        this.identityProfile = identityProfile;
    }

    public Long getSessionExpiryOverride() {

        return sessionExpiryOverride;
    }

    public void setSessionExpiryOverride(final Long sessionExpiryOverride) {

        this.sessionExpiryOverride = sessionExpiryOverride;
    }

    public String getTheme() {

        return theme;
    }

    public void setTheme(final String theme) {

        this.theme = theme;
    }

    public String getMobileLandingSuccess() {

        return mobileLandingSuccess;
    }

    public void setMobileLandingSuccess(final String mobileLandingSuccess) {

        this.mobileLandingSuccess = mobileLandingSuccess;
    }

    public String getMobileLandingError() {

        return mobileLandingError;
    }

    public void setMobileLandingError(final String mobileLandingError) {

        this.mobileLandingError = mobileLandingError;
    }

    public String getMobileLandingCancel() {

        return mobileLandingCancel;
    }

    public void setMobileLandingCancel(final String mobileLandingCancel) {

        this.mobileLandingCancel = mobileLandingCancel;
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

    public static class SAMLContext implements Serializable {

        private final LinkIDSAMLBinding binding;
        private final String            relayState;

        public SAMLContext() {

            this( null, null, null );
        }

        /**
         * @param binding    The SAML binding that should be used to establish SAML communication.  May be {@code null}, in which case
         *                   the value of {@link LinkIDSAMLProtocolConfig#binding()} will be used.
         * @param relayState The Relay State that is sent along with SAML communications. May be {@code null}, in which case the value
         *                   of {@link LinkIDSAMLProtocolConfig#relayState()} will be used.
         * @param breakFrame break frame, used for having the linkID authentication process in an iframe. At the end of the process, the
         *                   SAML2 browser POST will make it break out of the iframe.
         */
        public SAMLContext(@Nullable LinkIDSAMLBinding binding, @Nullable String relayState, @Nullable Boolean breakFrame) {

            this.binding = ifNotNullElse( binding, config().proto().saml().binding() );
            this.relayState = ifNotNullElseNullable( relayState, config().proto().saml().relayState() );
        }

        @Override
        public String toString() {

            return "SAMLContext{" +
                   "binding=" + binding +
                   ", relayState='" + relayState + '\'' +
                   '}';
        }

        public LinkIDSAMLBinding getBinding() {

            return binding;
        }

        public String getRelayState() {

            return relayState;
        }
    }
}
