package net.link.safeonline.sdk.configuration;

import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.config;

import com.google.common.base.Supplier;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import net.link.safeonline.sdk.auth.protocol.Protocol;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.config.KeyProvider;


/**
 * <h2>{@link AuthenticationContext}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class AuthenticationContext extends LinkIDContext {

    private final boolean     forceAuthentication;
    private final Set<String> devices;

    /**
     * @see #AuthenticationContext(String, KeyProvider, Set, String)
     */
    public AuthenticationContext() {

        this( null, null, null, (String) null );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be <code>null</code>, in which case
     *                        {@link AppLinkIDConfig#name()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be <code>null</code>, in which case the user is sent to the application's
     *                        context path.
     * @param protocol        Authentication protocol to use
     * @param devices         A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in this set
     *                        cannot be used by the user to authenticate himself as a result of this call.  May be <code>null</code> or
     *                        empty, in which case the user is free to pick from any supported devices.  NOTE: Either way, the application's
     *                        device policy configured at the linkID node may further restrict the available devices.
     *
     * @see #AuthenticationContext(String, String, KeyProvider, boolean, Set, String, String, Locale, String)
     */
    public AuthenticationContext(String applicationName, Set<String> devices, String target, Protocol protocol) {

        this( applicationName, null, null, false, null, null, target, devices, null, protocol );
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
     * @param devices         A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in this set
     *                        cannot be used by the user to authenticate himself as a result of this call.  May be <code>null</code> or
     *                        empty, in which case the user is free to pick from any supported devices.  NOTE: Either way, the application's
     *                        device policy configured at the linkID node may further restrict the available devices.
     *
     * @see #AuthenticationContext(String, String, KeyProvider, boolean, Set, String, String, Locale, String)
     */
    public AuthenticationContext(String applicationName, KeyProvider keyProvider, Set<String> devices, String target) {

        this( applicationName, null, keyProvider, false, devices, null, null, null, target );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be <code>null</code>, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be <code>null</code>, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param keyProvider             The provider that will provide the necessary keys and certificates to authenticate and sign the
     *                                application's requests and responses or verify the linkID server's communications.  May be
     *                                <code>null</code>, in which case {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param forceAuthentication     If <code>true</code>, users initiating authentication while in a live SSO environment will still be
     *                                required to fully identify and authenticate themselves with a device.
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                <code>null</code>, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be <code>null</code>, in which case the
     *                                user is sent to the application's context path.
     * @param devices                 A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in
     *                                this set cannot be used by the user to authenticate himself as a result of this call.  May be
     *                                <code>null</code> or empty, in which case the user is free to pick from any supported devices.  NOTE:
     *                                Either way, the application's device policy configured at the linkID node may further restrict the
     *                                available devices.
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     *
     * @see #AuthenticationContext(String, String, KeyPair, X509Certificate, Collection, X509Certificate, boolean, String, Locale, String,
     *      Set, String, Protocol)
     */
    public AuthenticationContext(String applicationName, String applicationFriendlyName, KeyProvider keyProvider,
                                 boolean forceAuthentication, Set<String> devices, String sessionTrackingId, String themeName,
                                 Locale language, String target) {

        this( applicationName, applicationFriendlyName, getOrDefault( keyProvider, new Supplier<KeyProvider>() {
            public KeyProvider get() {
                return config().linkID().app().keyProvider();
            }
        } ), forceAuthentication, themeName, language, target, devices, sessionTrackingId, null );
    }

    private AuthenticationContext(String applicationName, String applicationFriendlyName, KeyProvider keyProvider,
                                  boolean forceAuthentication, String themeName, Locale language, String target, Set<String> devices,
                                  String sessionTrackingId, Protocol protocol) {

        this( applicationName, applicationFriendlyName, //
              null != keyProvider? keyProvider.getIdentityKeyPair(): null, //
              null != keyProvider? keyProvider.getIdentityCertificate(): null,//
              null != keyProvider? keyProvider.getTrustedCertificates(): null, //
              null != keyProvider? keyProvider.getTrustedCertificate( LinkIDServiceFactory.SSL_ALIAS ): null, //
              forceAuthentication, themeName, language, target, devices, sessionTrackingId, protocol );
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
     * @param forceAuthentication     If <code>true</code>, users initiating authentication while in a live SSO environment will still be
     *                                required to fully identify and authenticate themselves with a device.
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                <code>null</code>, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be <code>null</code>, in which case the
     *                                user is sent to the application's context path.
     * @param devices                 A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in
     *                                this set cannot be used by the user to authenticate himself as a result of this call.  May be
     *                                <code>null</code>, in which case the user is free to pick from any supported devices.  NOTE: Either
     *                                way, the application's device policy configured at the linkID node may further restrict the available
     *                                devices.  <b>Note:</b> An empty set means does NOT mean <i>all</i>, but <i>no</i> devices are
     *                                allowed!
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param protocol                The protocol to use for the communication between the application and the linkID services.  May be
     *                                <code>null</code>, in which case {@link ProtocolConfig#defaultProtocol()} will be used.
     */
    public AuthenticationContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
                                 X509Certificate applicationCertificate, Collection<X509Certificate> trustedCertificates,
                                 X509Certificate sslCertificate, boolean forceAuthentication, String themeName, Locale language,
                                 String target, Set<String> devices, String sessionTrackingId, Protocol protocol) {

        super( applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate, trustedCertificates, sslCertificate,
               sessionTrackingId, themeName, language, target, protocol );

        this.forceAuthentication = forceAuthentication;
        this.devices = devices;
    }

    public boolean isForceAuthentication() {

        return forceAuthentication;
    }

    public Set<String> getDevices() {

        return devices;
    }

    @Override
    public String toString() {

        return String.format( "{authn: %s, force=%s, dev=%s}", //
                              super.toString(), isForceAuthentication(), getDevices() );
    }
}
