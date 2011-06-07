/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import static com.lyndir.lhunath.opal.system.util.ObjectUtils.*;
import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;

import com.google.common.base.Supplier;
import java.io.Serializable;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import net.link.safeonline.sdk.auth.protocol.Protocol;
import net.link.safeonline.sdk.auth.protocol.openid.OpenIDSSLSocketFactory;
import net.link.safeonline.sdk.auth.protocol.openid.OpenIDTrustManager;
import net.link.safeonline.sdk.auth.protocol.saml2.SAMLBinding;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.config.KeyProvider;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.jetbrains.annotations.NotNull;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.xri.XriResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcherFactory;


/**
 * <h2>{@link LinkIDContext}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public abstract class LinkIDContext implements Serializable {

    private final String                      applicationName;
    private final String                      applicationFriendlyName;
    private final KeyPair                     applicationKeyPair;
    private final X509Certificate             applicationCertificate;
    //
    private final Collection<X509Certificate> trustedCertificates;
    private final String                      sessionTrackingId;
    private final String                      themeName;
    private final Locale                      language;
    private final String                      target;
    //
    private final Protocol                    protocol;
    private final SAMLContext                 saml;
    private final OpenIDContext               openID;

    /**
     * @see #LinkIDContext(String, KeyProvider, String)
     */
    protected LinkIDContext() {

        this( null, null, null );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be <code>null</code>, in which case
     *                        {@link AppLinkIDConfig#name()} will be used.
     * @param keyProvider     The provider that will provide the necessary keys and certificates to authenticate and sign the application's
     *                        requests and responses or verify the linkID server's communications.  May be <code>null</code>, in which case
     *                        {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be <code>null</code>, in which case the user is sent to the application's
     *                        context path.
     *
     * @see #LinkIDContext(String, String, KeyProvider, String, String, Locale, String)
     */
    protected LinkIDContext(String applicationName, KeyProvider keyProvider, String target) {

        this( applicationName, null, keyProvider, null, null, null, target );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be <code>null</code>, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be <code>null</code>, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param keyProvider             The provider that will provide the necessary keys and certificates to authenticate and sign the
     *                                application's requests and responses or verify the linkID server's communications.  May be
     *                                <code>null</code>, in which case {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                <code>null</code>, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be <code>null</code>, in which case the
     *                                user is sent to the application's context path.
     *
     * @see #LinkIDContext(String, String, KeyProvider, String, String, Locale, String, Void)
     */
    protected LinkIDContext(String applicationName, String applicationFriendlyName, KeyProvider keyProvider, String sessionTrackingId,
                            String themeName, Locale language, String target) {

        this( applicationName, applicationFriendlyName, ifNotNullElse( keyProvider, new Supplier<KeyProvider>() {
            public KeyProvider get() {

                return config().linkID().app().keyProvider();
            }
        } ),
                sessionTrackingId, themeName, language, target, null );
    }

    private LinkIDContext(String applicationName, String applicationFriendlyName, @NotNull KeyProvider keyProvider,
                          String sessionTrackingId, String themeName, Locale language, String target, Void v) {

        this( applicationName, applicationFriendlyName, //
                keyProvider.getIdentityKeyPair(), keyProvider.getIdentityCertificate(),  //
                keyProvider.getTrustedCertificates(), //
                keyProvider.getTrustedCertificate( LinkIDServiceFactory.SSL_ALIAS ), sessionTrackingId, themeName, language, target, null );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be <code>null</code>, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be <code>null</code>, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param applicationKeyPair      The application's key pair that will be used to sign the authentication request.
     * @param applicationCertificate  The certificate issued for the application's key pair.  It will be added to WS-Security headers for
     *                                purpose of server-side identification and verification.
     * @param trustedCertificates     Used for validating whether incoming messages can be trusted.  The certificate chain in the incoming
     *                                message's signature is deemed trusted when the chain is valid, all certificates are valid, none are
     *                                revoked, and at least is in the set of trusted certificates.
     * @param sslCertificate          The linkID server's SSL certificate. It will be used to validate establishment of SSL-transport based
     *                                communication with the server. May be <code>null</code>, in which case no SSL certificate validation
     *                                will take place.
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                <code>null</code>, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be <code>null</code>, in which case the
     *                                user is sent to the application's context path.
     * @param protocol                The protocol to use for the communication between the application and the linkID services.  May be
     *                                <code>null</code>, in which case {@link ProtocolConfig#defaultProtocol()} will be used.
     */
    protected LinkIDContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
                            X509Certificate applicationCertificate, Collection<X509Certificate> trustedCertificates,
                            X509Certificate sslCertificate, String sessionTrackingId, String themeName, Locale language, String target,
                            Protocol protocol) {

        saml = new SAMLContext();
        openID = new OpenIDContext( sslCertificate );

        this.applicationName = ifNotNullElse( applicationName, config().linkID().app().name() );
        this.applicationFriendlyName = applicationFriendlyName;
        this.applicationKeyPair = applicationKeyPair;
        this.applicationCertificate = applicationCertificate;
        this.trustedCertificates = trustedCertificates;
        this.sessionTrackingId = sessionTrackingId;
        this.themeName = ifNotNullElseNullable( themeName, config().linkID().theme() );
        this.language = ifNotNullElseNullable( language, config().linkID().language() );
        this.target = target;
        this.protocol = ifNotNullElse( protocol, config().proto().defaultProtocol() );
    }

    public String getApplicationName() {

        return applicationName;
    }

    public String getApplicationFriendlyName() {

        return applicationFriendlyName;
    }

    public KeyPair getApplicationKeyPair() {

        return applicationKeyPair;
    }

    public X509Certificate getApplicationCertificate() {

        return applicationCertificate;
    }

    public Collection<X509Certificate> getTrustedCertificates() {

        return trustedCertificates;
    }

    public String getSessionTrackingId() {

        return sessionTrackingId;
    }

    public String getThemeName() {

        return themeName;
    }

    public Locale getLanguage() {

        return language;
    }

    public String getTarget() {

        return target;
    }

    public Protocol getProtocol() {

        return protocol;
    }

    public SAMLContext getSAML() {

        return saml;
    }

    public OpenIDContext getOpenID() {

        return openID;
    }

    @Override
    public String toString() {

        return String.format( "{app=%s, dn=%s, session=%s, themeName=%s, target=%s, protocol=%s}", //
                getApplicationName(), getApplicationCertificate().getSubjectDN(), getSessionTrackingId(), getThemeName(), getTarget(),
                getProtocol() );
    }

    public static class SAMLContext implements Serializable {

        private final SAMLBinding binding;
        private final String      relayState;

        public SAMLContext() {

            this( null, null );
        }

        /**
         * @param binding    The SAML binding that should be used to establish SAML communication.  May be <code>null</code>, in which case
         *                   the value of {@link SAMLProtocolConfig#binding()} will be used.
         * @param relayState The Relay State that is sent along with SAML communications. May be <code>null</code>, in which case the value
         *                   of {@link SAMLProtocolConfig#relayState()} will be used.
         */
        public SAMLContext(SAMLBinding binding, String relayState) {

            this.binding = ifNotNullElse( binding, config().proto().saml().binding() );
            this.relayState = ifNotNullElseNullable( relayState, config().proto().saml().relayState() );
        }

        public SAMLBinding getBinding() {

            return binding;
        }

        public String getRelayState() {

            return relayState;
        }
    }


    public static class OpenIDContext implements Serializable {

        /*
         * Consumer Manager cannot be included in the OpenIDContext as it is not serializable.
         */
        private static ConsumerManager manager;

        private final X509Certificate sslCertificate;

        /**
         * @param sslCertificate optional SSL certificate of the LinkID Service. If <code>null</code> all SSL certificates are considered
         *                       trusted.
         */
        public OpenIDContext(X509Certificate sslCertificate) {

            this.sslCertificate = sslCertificate;
        }

        public ConsumerManager getManager() {

            if (null == manager) {

                // ConsumerManager initialization
                try {

                    TrustManager trustManager;
                    if (null == sslCertificate) {
                        OpenIDSSLSocketFactory.installAllTrusted();
                        trustManager = new OpenIDTrustManager();
                    } else {
                        OpenIDSSLSocketFactory.install( sslCertificate );
                        trustManager = new OpenIDTrustManager( sslCertificate );
                    }

                    SSLContext sslContext = SSLContext.getInstance( "SSL" );
                    TrustManager[] trustManagers = { trustManager };
                    sslContext.init( null, trustManagers, null );
                    HttpFetcherFactory httpFetcherFactory = new HttpFetcherFactory( sslContext,
                            SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
                    YadisResolver yadisResolver = new YadisResolver( httpFetcherFactory );
                    RealmVerifierFactory realmFactory = new RealmVerifierFactory( yadisResolver );
                    HtmlResolver htmlResolver = new HtmlResolver( httpFetcherFactory );
                    XriResolver xriResolver = Discovery.getXriResolver();
                    Discovery discovery = new Discovery( htmlResolver, yadisResolver, xriResolver );
                    manager = new ConsumerManager( realmFactory, discovery, httpFetcherFactory );
                }
                catch (KeyManagementException e) {
                    throw new RuntimeException( e );
                }
                catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException( e );
                }
                catch (KeyStoreException e) {
                    throw new RuntimeException( e );
                }
            }
            return manager;
        }
    }
}
