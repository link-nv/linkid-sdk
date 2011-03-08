package net.link.safeonline.sdk.configuration;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.config;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;
import net.link.safeonline.keystore.LinkIDKeyStore;
import net.link.safeonline.sdk.auth.protocol.Protocol;
import org.jetbrains.annotations.NotNull;


/**
 * <h2>{@link AuthenticationContext}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class LogoutContext extends LinkIDContext {

    /**
     * @see #LogoutContext(String, LinkIDKeyStore, String)
     */
    public LogoutContext() {

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
     * @see #LogoutContext(String, String, LinkIDKeyStore, String, String, Locale, String)
     */
    public LogoutContext(String applicationName, LinkIDKeyStore keyStore, String target) {

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
     * @see #LogoutContext(String, String, KeyPair, X509Certificate, List, List, X509Certificate, String, String, Locale, String, Protocol)
     */
    public LogoutContext(String applicationName, String applicationFriendlyName, LinkIDKeyStore keyStore, String sessionTrackingId,
                         String themeName, Locale language, String target) {

        this( applicationName, applicationFriendlyName, getOrDefault( keyStore, config().linkID().app().keyStore() ), sessionTrackingId,
              themeName, language, target, null );
    }

    private LogoutContext(String applicationName, String applicationFriendlyName, @NotNull LinkIDKeyStore keyStore,
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
     *                                purpose of server-side identification and verification. be <code>null</code>, in which case the
     *                                messages are only validated using the embedded certificate.
     * @param serviceCertificates     The linkID service certificates, optionally used for validation of signed logout requests/responses.
     * @param serviceRootCertificates The linkID service root certificates, optionally used for trust validation of the cert.chain returned
     *                                in signed logout requests/responses
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
    public LogoutContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
                         X509Certificate applicationCertificate, List<X509Certificate> serviceCertificates,
                         List<X509Certificate> serviceRootCertificates, X509Certificate sslCertificate, String sessionTrackingId,
                         String themeName, Locale language, String target, Protocol protocol) {

        super( applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate, serviceCertificates,
               serviceRootCertificates, sslCertificate, sessionTrackingId, themeName, language, target, protocol );
    }

    @Override
    public String toString() {

        return String.format( "{logout: %s}", super.toString() );
    }
}
