package net.link.safeonline.sdk.configuration;

import static com.lyndir.lhunath.opal.system.util.ObjectUtils.*;
import static net.link.safeonline.sdk.configuration.SafeOnlineConfigHolder.*;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.*;
import net.link.safeonline.sdk.api.auth.LoginMode;
import net.link.safeonline.sdk.auth.protocol.Protocol;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.config.KeyProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link AuthenticationContext}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class AuthenticationContext extends LinkIDContext {

    private boolean                         forceAuthentication;
    private boolean                         forceRegistration;
    private Set<String>                     devices;
    private Map<String, List<Serializable>> subjectAttributes;

    /**
     * @see #AuthenticationContext(String, KeyProvider, Set, String)
     */
    public AuthenticationContext() {

        this( null, null, null, (String) null );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be {@code null}, in which case
     *                        {@link AppLinkIDConfig#name()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be {@code null}, in which case the user is sent to the application's
     *                        context path.
     * @param protocol        Authentication protocol to use
     * @param devices         A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in this
     *                        set
     *                        cannot be used by the user to authenticate himself as a result of this call.  May be {@code null} or
     *                        empty, in which case the user is free to pick from any supported devices.  NOTE: Either way, the
     *                        application's
     *                        device policy configured at the linkID node may further restrict the available devices.
     *
     * @see #AuthenticationContext(String, String, KeyProvider, boolean, Set, String, String, Locale, String)
     */
    public AuthenticationContext(String applicationName, Set<String> devices, String target, Protocol protocol) {

        this( applicationName, null, null, false, null, null, target, devices, null, protocol, null );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be {@code null}, in which case
     *                        {@link AppLinkIDConfig#name()} will be used.
     * @param keyProvider     The provider that will provide the necessary keys and certificates to authenticate and sign the application's
     *                        requests and responses or verify the linkID server's communications.  May be {@code null}, in which case
     *                        {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be {@code null}, in which case the user is sent to the application's
     *                        context path.
     * @param devices         A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in this
     *                        set
     *                        cannot be used by the user to authenticate himself as a result of this call.  May be {@code null} or
     *                        empty, in which case the user is free to pick from any supported devices.  NOTE: Either way, the
     *                        application's
     *                        device policy configured at the linkID node may further restrict the available devices.
     *
     * @see #AuthenticationContext(String, String, KeyProvider, boolean, Set, String, String, Locale, String)
     */
    public AuthenticationContext(@Nullable String applicationName, @Nullable KeyProvider keyProvider, @Nullable Set<String> devices,
                                 String target) {

        this( applicationName, null, keyProvider, false, devices, null, null, null, target );
    }

    /**
     * @param applicationName The name of the application that the user is being authenticated for. May be {@code null}, in which case
     *                        {@link AppLinkIDConfig#name()} will be used.
     * @param keyProvider     The provider that will provide the necessary keys and certificates to authenticate and sign the application's
     *                        requests and responses or verify the linkID server's communications.  May be {@code null}, in which case
     *                        {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param target          Either an absolute URL or a path relative to the application's context path that specifies the location the
     *                        user will be sent to after the authentication response has been handled (or with the authentication response,
     *                        if there is no landing page).  May be {@code null}, in which case the user is sent to the application's
     *                        context path.
     * @param devices         A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in this
     *                        set
     *                        cannot be used by the user to authenticate himself as a result of this call.  May be {@code null} or
     *                        empty, in which case the user is free to pick from any supported devices.  NOTE: Either way, the
     *                        application's
     *                        device policy configured at the linkID node may further restrict the available devices.
     *
     * @see #AuthenticationContext(String, String, KeyProvider, boolean, Set, String, String, Locale, String)
     */
    public AuthenticationContext(String applicationName, KeyProvider keyProvider, Set<String> devices, String target, LoginMode loginMode) {

        this( applicationName, null, keyProvider, false, devices, null, null, null, target, loginMode );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be {@code null}, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be {@code null}, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param keyProvider             The provider that will provide the necessary keys and certificates to authenticate and sign the
     *                                application's requests and responses or verify the linkID server's communications.  May be
     *                                {@code null}, in which case {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param forceAuthentication     If {@code true}, users initiating authentication while in a live SSO environment will still be
     *                                required to fully identify and authenticate themselves with a device.
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                {@code null}, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be {@code null}, in which case the
     *                                user is sent to the application's context path.
     * @param devices                 A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in
     *                                this set cannot be used by the user to authenticate himself as a result of this call.  May be
     *                                {@code null} or empty, in which case the user is free to pick from any supported devices.  NOTE:
     *                                Either way, the application's device policy configured at the linkID node may further restrict the
     *                                available devices.
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     *
     * @see #AuthenticationContext(String, String, KeyPair, X509Certificate, Collection, X509Certificate, boolean, String, Locale, String,
     *      Set, String, Protocol)
     */
    public AuthenticationContext(String applicationName, @Nullable String applicationFriendlyName, KeyProvider keyProvider,
                                 boolean forceAuthentication, Set<String> devices, @Nullable String sessionTrackingId,
                                 @Nullable String themeName, @Nullable Locale language, String target) {

        this( applicationName, applicationFriendlyName, ifNotNullElse( keyProvider, new NNSupplier<KeyProvider>() {
            @NotNull
            @Override
            public KeyProvider get() {

                return config().linkID().app().keyProvider();
            }
        } ), forceAuthentication, themeName, language, target, devices, sessionTrackingId, null, null );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be {@code null}, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
     * @param applicationFriendlyName A user-friendly name of the application.  May be {@code null}, in which case the user-friendly
     *                                name configured at the linkID server will be used.
     * @param keyProvider             The provider that will provide the necessary keys and certificates to authenticate and sign the
     *                                application's requests and responses or verify the linkID server's communications.  May be
     *                                {@code null}, in which case {@link AppLinkIDConfig#keyProvider()} will be used.
     * @param forceAuthentication     If {@code true}, users initiating authentication while in a live SSO environment will still be
     *                                required to fully identify and authenticate themselves with a device.
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                {@code null}, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be {@code null}, in which case the
     *                                user is sent to the application's context path.
     * @param devices                 A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in
     *                                this set cannot be used by the user to authenticate himself as a result of this call.  May be
     *                                {@code null} or empty, in which case the user is free to pick from any supported devices.  NOTE:
     *                                Either way, the application's device policy configured at the linkID node may further restrict the
     *                                available devices.
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param loginMode               Indicates to the LinkID services how the login procedure wil be shown visually at client side: a
     *                                redirect
     *                                to the LinkID login, inside a popup window, or inside an (i)frame (e.g with a modal window). Based on
     *                                this
     *                                information, LinkID services can make decisions on for example theme's to use, and wether or not
     *                                authorisation
     *                                responses should try to break out of an iframe (needed when showing the login inside an iframe). If
     *                                {@code null},
     *                                will default to redirect mode, unless the legacy breakFrame configuration option has been enabled.
     *
     * @see #AuthenticationContext(String, String, KeyPair, X509Certificate, Collection, X509Certificate, boolean, String, Locale, String,
     *      Set, String, Protocol)
     */
    public AuthenticationContext(String applicationName, @Nullable String applicationFriendlyName, KeyProvider keyProvider,
                                 boolean forceAuthentication, Set<String> devices, @Nullable String sessionTrackingId,
                                 @Nullable String themeName, @Nullable Locale language, String target, LoginMode loginMode) {

        this( applicationName, applicationFriendlyName, ifNotNullElse( keyProvider, new NNSupplier<KeyProvider>() {
            @NotNull
            @Override
            public KeyProvider get() {

                return config().linkID().app().keyProvider();
            }
        } ), forceAuthentication, themeName, language, target, devices, sessionTrackingId, null, loginMode );
    }

    private AuthenticationContext(String applicationName, @Nullable String applicationFriendlyName, @Nullable KeyProvider keyProvider,
                                  boolean forceAuthentication, @Nullable String themeName, @Nullable Locale language, String target,
                                  Set<String> devices, @Nullable String sessionTrackingId, @Nullable Protocol protocol,
                                  @Nullable LoginMode loginMode) {

        this( applicationName, applicationFriendlyName, //
                null != keyProvider? keyProvider.getIdentityKeyPair(): null, //
                null != keyProvider? keyProvider.getIdentityCertificate(): null,//
                null != keyProvider? keyProvider.getTrustedCertificates(): null, //
                null != keyProvider? keyProvider.getTrustedCertificate( LinkIDServiceFactory.SSL_ALIAS ): null, //
                forceAuthentication, themeName, language, target, devices, sessionTrackingId, protocol, loginMode );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be {@code null}, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
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
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                {@code null}, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be {@code null}, in which case the
     *                                user is sent to the application's context path.
     * @param devices                 A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in
     *                                this set cannot be used by the user to authenticate himself as a result of this call.  May be
     *                                {@code null}, in which case the user is free to pick from any supported devices.  NOTE: Either
     *                                way, the application's device policy configured at the linkID node may further restrict the available
     *                                devices.  <b>Note:</b> An empty set means does NOT mean <i>all</i>, but <i>no</i> devices are
     *                                allowed!
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param protocol                The protocol to use for the communication between the application and the linkID services.  May be
     *                                {@code null}, in which case {@link ProtocolConfig#defaultProtocol()} will be used.
     */
    public AuthenticationContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
                                 X509Certificate applicationCertificate, Collection<X509Certificate> trustedCertificates,
                                 X509Certificate sslCertificate, boolean forceAuthentication, String themeName, Locale language,
                                 String target, Set<String> devices, String sessionTrackingId, Protocol protocol) {

        this( applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate, trustedCertificates, sslCertificate,
                forceAuthentication, themeName, language, target, devices, sessionTrackingId, protocol, null );
    }

    /**
     * @param applicationName         The name of the application that the user is being authenticated for. May be {@code null}, in
     *                                which case {@link AppLinkIDConfig#name()} will be used.
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
     * @param themeName               The name of the theme configured at the linkID node that should be applied to the linkID
     *                                authentication application while the user authenticates himself as a result of this call.  May be
     *                                {@code null}, in which case {@link LinkIDConfig#theme()} will be used.
     * @param language                The language that the linkID services should use for localization of their interaction with the user.
     * @param target                  Either an absolute URL or a path relative to the application's context path that specifies the
     *                                location the user will be sent to after the authentication response has been handled (or with the
     *                                authentication response, if there is no landing page).  May be {@code null}, in which case the
     *                                user is sent to the application's context path.
     * @param devices                 A set of devices with which the user is allowed to authenticate himself.  Any devices that are not in
     *                                this set cannot be used by the user to authenticate himself as a result of this call.  May be
     *                                {@code null}, in which case the user is free to pick from any supported devices.  NOTE: Either
     *                                way, the application's device policy configured at the linkID node may further restrict the available
     *                                devices.  <b>Note:</b> An empty set means does NOT mean <i>all</i>, but <i>no</i> devices are
     *                                allowed!
     * @param sessionTrackingId       An identifier that is used when session tracking is enabled to identify the session that will be
     *                                authenticated for by this authentication process.
     * @param protocol                The protocol to use for the communication between the application and the linkID services.  May be
     *                                {@code null}, in which case {@link ProtocolConfig#defaultProtocol()} will be used.
     * @param loginMode               Indicates to the LinkID services how the login procedure wil be shown visually at client side: a
     *                                redirect
     *                                to the LinkID login, inside a popup window, or inside an (i)frame (e.g with a modal window). Based on
     *                                this
     *                                information, LinkID services can make decisions on for example theme's to use, and wether or not
     *                                authorisation
     *                                responses should try to break out of an iframe (needed when showing the login inside an iframe). If
     *                                {@code null},
     *                                will default to redirect mode, unless the legacy breakFrame configuration option has been enabled.
     */
    public AuthenticationContext(String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
                                 X509Certificate applicationCertificate, Collection<X509Certificate> trustedCertificates,
                                 X509Certificate sslCertificate, boolean forceAuthentication, String themeName, Locale language,
                                 String target, Set<String> devices, String sessionTrackingId, Protocol protocol,
                                 @Nullable LoginMode loginMode) {

        super( applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate, trustedCertificates, sslCertificate,
                sessionTrackingId, themeName, language, target, protocol, loginMode );

        this.forceAuthentication = forceAuthentication;
        this.devices = devices;
    }

    public boolean isForceRegistration() {

        return forceRegistration;
    }

    /**
     * Setting this flag to true will force the linkID authentication webapp to override the page "do you have an account" and go straight
     * to the register device page. It will also disregard any "deflowered" cookie which is a cookie set upon the first successful
     * authentication which will also skip the "do you have an account" page and go straight to the login device page.
     */
    public void setForceRegistration(final boolean forceRegistration) {

        this.forceRegistration = forceRegistration;
    }

    public boolean isForceAuthentication() {

        return forceAuthentication;
    }

    public void setForceAuthentication(final boolean forceAuthentication) {

        this.forceAuthentication = forceAuthentication;
    }

    public Set<String> getDevices() {

        return devices;
    }

    public void setDevices(final Set<String> devices) {

        this.devices = devices;
    }

    public Map<String, List<Serializable>> getSubjectAttributes() {

        return subjectAttributes;
    }

    public void setSubjectAttributes(final Map<String, List<Serializable>> subjectAttributes) {

        this.subjectAttributes = subjectAttributes;
    }

    @Override
    public String toString() {

        return String.format( "{authn: %s, forceReg=%s forceAuth=%s, dev=%s}", //
                super.toString(), isForceRegistration(), isForceAuthentication(), getDevices() );
    }
}
