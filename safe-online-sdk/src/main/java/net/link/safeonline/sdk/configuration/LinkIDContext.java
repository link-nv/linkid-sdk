/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import net.link.safeonline.keystore.LinkIDKeyStore;
import net.link.safeonline.sdk.auth.protocol.Protocol;
import net.link.safeonline.sdk.auth.protocol.openid.OpenIDSSLSocketFactory;
import net.link.safeonline.sdk.auth.protocol.openid.OpenIDTrustManager;
import net.link.safeonline.sdk.auth.protocol.saml2.SAMLBinding;
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

    private final String                applicationName;
    private final String                applicationFriendlyName;
    private final KeyPair               applicationKeyPair;
    private final X509Certificate       applicationCertificate;
    //
    private final List<X509Certificate> serviceCertificates;
    private final List<X509Certificate> serviceRootCertificates;
    private final String                sessionTrackingId;
    private final String                themeName;
    private final Locale                language;
    private final String                target;
    //
    private final Protocol              protocol;
    private final SAMLContext           saml;
    private final OpenIDContext         openID;

    /**
     * @see #LinkIDContext(String, LinkIDKeyStore, String)
     */
    protected LinkIDContext() {

        this( null, null, null );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be <code>null</code>, in which case
     *                        {@link AppLinkIDConfig#name()} will be used.
     * @param keyStore        The store that will provide the necessary keys and certificates to authenticate and sign the application's
     *                        requests and responses or verify the linkID server's communications.  May be <code>null</code>, in which case
     *                        {@link AppLinkIDConfig#keyStore()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be <code>null</code>, in which case the user is sent to the application's
     *                        context path.
     *
     * @see #LinkIDContext(String, String, LinkIDKeyStore, String, String, Locale, String)
     */
    protected LinkIDContext(String applicationName, LinkIDKeyStore keyStore, String target) {

        this( applicationName, null, keyStore, null, null, null, target );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be <code>null</code>, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be <code>null</code>, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param keyStore                The store that will provide the necessary keys and certificates to authenticate and sign the
     *                                application's requests and responses or verify the linkID server's communications.  May be
     *                                <code>null</code>, in which case {@link AppLinkIDConfig#keyStore()} will be used.
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
     * @see #LinkIDContext(String, String, KeyPair, X509Certificate, List, List, X509Certificate, String, String, Locale, String, Protocol)
     */
    protected LinkIDContext(String applicationName, String applicationFriendlyName, LinkIDKeyStore keyStore, String sessionTrackingId,
                            String themeName, Locale language, String target) {

        this( applicationName, applicationFriendlyName, getOrDefault( keyStore, config().linkID().app().keyStore() ), sessionTrackingId,
              themeName, language, target, null );
    }

    private LinkIDContext(String applicationName, String applicationFriendlyName, @NotNull LinkIDKeyStore keyStore,
                          String sessionTrackingId, String themeName, Locale language, String target, Void v) {

        this( applicationName, applicationFriendlyName, //
              keyStore.getKeyPair(), keyStore.getCertificate(),  //
              keyStore.getCertificates( LinkIDKeyStore.LINKID_SERVICE_ALIAS ), //
              keyStore.getCertificates( LinkIDKeyStore.LINKID_SERVICE_ROOT_ALIAS ), //
              keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ), //
              sessionTrackingId, themeName, language, target, null );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be <code>null</code>, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be <code>null</code>, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param applicationKeyPair      The application's key pair that will be used to sign the authentication request.
     * @param applicationCertificate  The certificate issued for the application's key pair.  It will be added to WS-Security headers for
     *                                purpose of server-side identification and verification.
     * @param serviceCertificates     The linkID service certificates. Will be used for validation of signatures (e.g. SAML v2.0
     *                                HTTP-Redirect).
     * @param serviceRootCertificates The linkID service root certificates, optionally used for trust validation of the cert.chain returned
     *                                in signed authentication responses.
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
                            X509Certificate applicationCertificate, List<X509Certificate> serviceCertificates,
                            List<X509Certificate> serviceRootCertificates, X509Certificate sslCertificate, String sessionTrackingId,
                            String themeName, Locale language, String target, Protocol protocol) {

        saml = new SAMLContext();
        openID = new OpenIDContext( sslCertificate );
        this.applicationName = getOrDefault( applicationName, config().linkID().app().name() );
        this.applicationFriendlyName = applicationFriendlyName;
        this.applicationKeyPair = applicationKeyPair;
        this.applicationCertificate = applicationCertificate;
        this.serviceCertificates = serviceCertificates;
        this.serviceRootCertificates = serviceRootCertificates;
        this.sessionTrackingId = sessionTrackingId;
        this.themeName = getOrDefault( themeName, config().linkID().theme() );
        this.language = getOrDefault( language, config().linkID().language() );
        this.target = target;
        this.protocol = getOrDefault( protocol, config().proto().defaultProtocol() );
    }

    /**
     * @param object        The object to return if it isn't <code>null</code>.
     * @param defaultObject The object to return when the first object is <code>null</code>.
     *
     * @return The given object or the defaultObject if the object is <code>null</code>.
     */
    protected static <T> T getOrDefault(T object, T defaultObject) {

        if (object != null)
            return object;

        return defaultObject;
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

    public List<X509Certificate> getServiceCertificates() {

        return serviceCertificates;
    }

    public List<X509Certificate> getServiceRootCertificates() {

        return serviceRootCertificates;
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
                              getApplicationName(), getApplicationCertificate().getSubjectDN(), getSessionTrackingId(), getThemeName(),
                              getTarget(), getProtocol() );
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

            this.binding = getOrDefault( binding, config().proto().saml().binding() );
            this.relayState = getOrDefault( relayState, config().proto().saml().relayState() );
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
                } catch (KeyManagementException e) {
                    throw new RuntimeException( e );
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException( e );
                } catch (KeyStoreException e) {
                    throw new RuntimeException( e );
                }
            }
            return manager;
        }
    }
}
