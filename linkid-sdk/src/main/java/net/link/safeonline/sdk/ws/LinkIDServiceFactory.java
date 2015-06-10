/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import static net.link.util.util.ObjectUtils.ifNotNullElseNullable;

import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.safeonline.sdk.api.ws.attrib.LinkIDAttributeClient;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthServiceClient;
import net.link.safeonline.sdk.api.ws.capture.LinkIDCaptureServiceClient;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDConfigurationServiceClient;
import net.link.safeonline.sdk.api.ws.data.client.LinkIDDataClient;
import net.link.safeonline.sdk.api.ws.haws.LinkIDHawsServiceClient;
import net.link.safeonline.sdk.api.ws.idmapping.LinkIDNameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.ltqr.LinkIDLTQRServiceClient;
import net.link.safeonline.sdk.api.ws.mandate.LinkIDMandateServiceClient;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentServiceClient;
import net.link.safeonline.sdk.api.ws.sts.LinkIDSecurityTokenServiceClient;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletServiceClient;
import net.link.safeonline.sdk.api.ws.xkms2.LinkIDXkms2Client;
import net.link.safeonline.sdk.configuration.LinkIDConfigUtils;
import net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder;
import net.link.safeonline.sdk.ws.attrib.LinkIDAttributeClientImpl;
import net.link.safeonline.sdk.ws.auth.LinkIDAuthServiceClientImpl;
import net.link.safeonline.sdk.ws.capture.LinkIDCaptureServiceClientImpl;
import net.link.safeonline.sdk.ws.configuration.LinkIDConfigurationServiceClientImpl;
import net.link.safeonline.sdk.ws.data.LinkIDDataClientImpl;
import net.link.safeonline.sdk.ws.haws.LinkIDHawsServiceClientImpl;
import net.link.safeonline.sdk.ws.idmapping.LinkIDNameIdentifierMappingClientImpl;
import net.link.safeonline.sdk.ws.ltqr.LinkIDLTQRServiceClientImpl;
import net.link.safeonline.sdk.ws.mandate.LinkIDMandateServiceClientImpl;
import net.link.safeonline.sdk.ws.payment.LinkIDPaymentServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.LinkIDSecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.wallet.LinkIDWalletServiceClientImpl;
import net.link.safeonline.sdk.ws.xkms2.LinkIDXkms2ClientImpl;
import net.link.util.config.KeyProvider;
import net.link.util.util.NSupplier;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * <h2>{@link LinkIDServiceFactory}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDServiceFactory extends LinkIDAbstractServiceFactory {

    private static LinkIDAbstractServiceFactory instance = new LinkIDServiceFactory();

    protected LinkIDServiceFactory() {

    }

    private static LinkIDAbstractServiceFactory getInstance() {

        return instance;
    }

    private static String getWsBase() {

        return String.format( "%s/%s", LinkIDSDKConfigHolder.config().web().linkIDBase(), LinkIDConstants.LINKID_PATH_WS_BASE );
    }

    private static String getWsUsernameBase() {

        return String.format( "%s/%s", LinkIDSDKConfigHolder.config().web().linkIDBase(), LinkIDConstants.LINKID_PATH_WS_USERNAME_BASE );
    }

    public static String getWsBase(final String linkIDBase) {

        return String.format( "%s/%s", linkIDBase, LinkIDConstants.LINKID_PATH_WS_BASE );
    }

    public static String getWsUsernameBase(final String linkIDBase) {

        return String.format( "%s/%s", linkIDBase, LinkIDConstants.LINKID_PATH_WS_USERNAME_BASE );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDAttributeClient getAttributeService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getAttributeService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getAttributeService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID attribute web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDAttributeClient getAttributeService(final String wsUsername, final String wsPassword) {

        return new LinkIDAttributeClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDAttributeClient getAttributeService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                            final X509Certificate[] sslCertificates) {

        return getInstance()._getAttributeService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDAttributeClient getAttributeService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return getInstance()._getAttributeService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDAttributeClient _getAttributeService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDAttributeClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID data web service.
     *
     * @return proxy to the linkID data web service.
     */
    public static LinkIDDataClient getDataService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getDataService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getDataService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID data web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID data web service.
     */
    public static LinkIDDataClient getDataService(final String wsUsername, final String wsPassword) {

        return new LinkIDDataClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ), new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return wsUsername;
            }

            @Override
            public String getPassword() {

                return wsPassword;
            }

            @Nullable
            @Override
            public String handle(final String username) {

                return null;
            }

            @Override
            public boolean isInboundHeaderOptional() {

                return true;
            }
        } );
    }

    /**
     * Retrieve a proxy to the linkID data web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID data web service.
     */
    public static LinkIDDataClient getDataService(@Nullable final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                  @Nullable final X509Certificate[] sslCertificates) {

        return getInstance()._getDataService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID data web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID data web service.
     */
    public static LinkIDDataClient getDataService(final WSSecurityConfiguration configuration, @Nullable X509Certificate[] sslCertificates) {

        return getInstance()._getDataService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDDataClient _getDataService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDDataClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service.
     *
     * @return proxy to the linkID ID mapping web service.
     */
    public static LinkIDNameIdentifierMappingClient getIdMappingService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getIdMappingService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getIdMappingService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID ID mapping web service.
     */
    public static LinkIDNameIdentifierMappingClient getIdMappingService(final String wsUsername, final String wsPassword) {

        return new LinkIDNameIdentifierMappingClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDNameIdentifierMappingClient getIdMappingService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                        final X509Certificate[] sslCertificates) {

        return getInstance()._getIdMappingService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDNameIdentifierMappingClient getIdMappingService(final WSSecurityConfiguration configuration,
                                                                        @Nullable X509Certificate[] sslCertificates) {

        return getInstance()._getIdMappingService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDNameIdentifierMappingClient _getIdMappingService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDNameIdentifierMappingClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDSecurityTokenServiceClient getStsService() {

        return getStsService( new LinkIDSDKWSSecurityConfiguration(), null );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDSecurityTokenServiceClient getStsService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                 final X509Certificate[] sslCertificates) {

        return getInstance()._getStsService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDSecurityTokenServiceClient getStsService(final WSSecurityConfiguration configuration, @Nullable X509Certificate[] sslCertificates) {

        return getInstance()._getStsService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDSecurityTokenServiceClient _getStsService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDSecurityTokenServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID XKMS2 web service.
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static LinkIDXkms2Client getXkms2Client() {

        return getXkms2Client( LinkIDSDKConfigHolder.config().linkID().app().keyProvider() );
    }

    /**
     * Retrieve a proxy to the linkID XKMS 2 web service.
     *
     * @param keyProvider The key provider that provides the keys and certificates used by WS-Security for authentication and validation.
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static LinkIDXkms2Client getXkms2Client(@NotNull final KeyProvider keyProvider) {

        return getInstance()._getXkms2Client( new X509Certificate[] { keyProvider.getTrustedCertificate( LinkIDConfigUtils.SSL_ALIAS ) } );
    }

    /**
     * Retrieve a proxy to the linkID XKMS 2 web service.
     *
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static LinkIDXkms2Client getXkms2Client(X509Certificate[] sslCertificates) {

        return getInstance()._getXkms2Client( sslCertificates );
    }

    @Override
    protected LinkIDXkms2Client _getXkms2Client(X509Certificate[] sslCertificates) {

        return new LinkIDXkms2ClientImpl( getWsBase(), getSSLCertificates( sslCertificates ) );
    }

    /**
     * Retrieve a proxy to the linkID LTQR web service.
     *
     * @return proxy to the linkID LTQR web service.
     */
    public static LinkIDLTQRServiceClient getLtqrService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getLtqrService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getLtqrServiceClient( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID LTQR web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID LTQR web service.
     */
    public static LinkIDLTQRServiceClient getLtqrService(final String wsUsername, final String wsPassword) {

        return new LinkIDLTQRServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID long term QR web service.
     *
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID long term QR web service.
     */
    public static LinkIDLTQRServiceClient getLtqrServiceClient(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                               final X509Certificate[] sslCertificates) {

        return getInstance()._getLtqrServiceClient( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDLTQRServiceClient getLtqrServiceClient(final WSSecurityConfiguration configuration, @Nullable X509Certificate[] sslCertificates) {

        return getInstance()._getLtqrServiceClient( configuration, sslCertificates );
    }

    @Override
    protected LinkIDLTQRServiceClient _getLtqrServiceClient(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDLTQRServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID haws web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID haws web service.
     */
    public static LinkIDHawsServiceClient<AuthnRequest, Response> getHawsService(final String wsUsername, final String wsPassword) {

        return new LinkIDHawsServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID haws web service.
     *
     * @return proxy to the linkID haws web service.
     */
    public static LinkIDHawsServiceClient<AuthnRequest, Response> getHawsService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getHawsService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getHawsService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDHawsServiceClient<AuthnRequest, Response> getHawsService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                                 final X509Certificate[] sslCertificates) {

        return getInstance()._getHawsService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LinkIDHawsServiceClient<AuthnRequest, Response> getHawsService(final WSSecurityConfiguration configuration,
                                                                                 X509Certificate[] sslCertificates) {

        return getInstance()._getHawsService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDHawsServiceClient<AuthnRequest, Response> _getHawsService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDHawsServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID auth web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID auth web service.
     */
    public static LinkIDAuthServiceClient<AuthnRequest, Response> getAuthService(final String wsUsername, final String wsPassword) {

        return new LinkIDAuthServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID auth web service.
     *
     * @return proxy to the linkID auth web service.
     */
    public static LinkIDAuthServiceClient<AuthnRequest, Response> getAuthService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getAuthService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getAuthService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID auth web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID auth web service.
     */
    public static LinkIDAuthServiceClient<AuthnRequest, Response> getAuthService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                                 final X509Certificate[] sslCertificates) {

        return getInstance()._getAuthService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID auth web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID auth web service.
     */
    public static LinkIDAuthServiceClient<AuthnRequest, Response> getAuthService(final WSSecurityConfiguration configuration,
                                                                                 X509Certificate[] sslCertificates) {

        return getInstance()._getAuthService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDAuthServiceClient<AuthnRequest, Response> _getAuthService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDAuthServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    private static X509Certificate[] getSSLCertificates(final X509Certificate[] sslCertificates) {

        return ifNotNullElseNullable( sslCertificates, new NSupplier<X509Certificate[]>() {
            public X509Certificate[] get() {

                try {
                    return new X509Certificate[] {
                            LinkIDSDKConfigHolder.config().linkID().app().keyProvider().getTrustedCertificate( LinkIDConfigUtils.SSL_ALIAS )
                    };
                }
                catch (Throwable t) {
                    // ignore
                    return null;
                }
            }
        } );
    }

    /**
     * Retrieve a proxy to the linkID mandate web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID mandate web service.
     */
    public static LinkIDMandateServiceClient getMandateService(final String wsUsername, final String wsPassword) {

        return new LinkIDMandateServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID mandate web service.
     *
     * @return proxy to the linkID mandate web service.
     */
    public static LinkIDMandateServiceClient getMandateService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getMandateService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getMandateService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID mandate web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID mandate web service.
     */
    public static LinkIDMandateServiceClient getMandateService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                               final X509Certificate[] sslCertificates) {

        return getInstance()._getMandateService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID mandate web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID mandate web service.
     */
    public static LinkIDMandateServiceClient getMandateService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return getInstance()._getMandateService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDMandateServiceClient _getMandateService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDMandateServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID capture web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID capture web service.
     */
    public static LinkIDCaptureServiceClient getCaptureService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                               final X509Certificate[] sslCertificates) {

        return getInstance()._getCaptureService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID capture web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID capture web service.
     */
    public static LinkIDCaptureServiceClient getCaptureService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return getInstance()._getCaptureService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDCaptureServiceClient _getCaptureService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDCaptureServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID capture web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID capture web service.
     */
    public static LinkIDCaptureServiceClient getCaptureService(final String wsUsername, final String wsPassword) {

        return new LinkIDCaptureServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID capture web service.
     *
     * @return proxy to the linkID capture web service.
     */
    public static LinkIDCaptureServiceClient getCaptureService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getCaptureService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getCaptureService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID wallet web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID wallet web service.
     */
    public static LinkIDWalletServiceClient getWalletService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                             final X509Certificate[] sslCertificates) {

        return getInstance()._getWalletService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID wallet web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID wallet web service.
     */
    public static LinkIDWalletServiceClient getWalletService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return getInstance()._getWalletService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDWalletServiceClient _getWalletService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDWalletServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID wallet web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID wallet web service.
     */
    public static LinkIDWalletServiceClient getWalletService(final String wsUsername, final String wsPassword) {

        return new LinkIDWalletServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID wallet web service.
     *
     * @return proxy to the linkID wallet web service.
     */
    public static LinkIDWalletServiceClient getWalletService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getWalletService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getWalletService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID payment web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID payment web service.
     */
    public static LinkIDPaymentServiceClient getPaymentService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                               final X509Certificate[] sslCertificates) {

        return getInstance()._getPaymentService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID payment web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID payment web service.
     */
    public static LinkIDPaymentServiceClient getPaymentService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return getInstance()._getPaymentService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDPaymentServiceClient _getPaymentService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDPaymentServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID payment web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID payment web service.
     */
    public static LinkIDPaymentServiceClient getPaymentService(final String wsUsername, final String wsPassword) {

        return new LinkIDPaymentServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID payment web service.
     *
     * @return proxy to the linkID payment web service.
     */
    public static LinkIDPaymentServiceClient getPaymentService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getPaymentService( LinkIDSDKConfigHolder.config().linkID().app().username(), LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getPaymentService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID configuration web service.
     *
     * @param trustedDN       The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider     The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                        validation.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID configuration web service.
     */
    public static LinkIDConfigurationServiceClient getConfigurationService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                           final X509Certificate[] sslCertificates) {

        return getInstance()._getConfigurationService( new LinkIDSDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificates );
    }

    /**
     * Retrieve a proxy to the linkID configuration web service.
     *
     * @param configuration   Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificates The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                        certificate.
     *
     * @return proxy to the linkID configuration web service.
     */
    public static LinkIDConfigurationServiceClient getConfigurationService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return getInstance()._getConfigurationService( configuration, sslCertificates );
    }

    @Override
    protected LinkIDConfigurationServiceClient _getConfigurationService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDConfigurationServiceClientImpl( getWsBase(), getSSLCertificates( sslCertificates ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID configuration web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID configuration web service.
     */
    public static LinkIDConfigurationServiceClient getConfigurationService(final String wsUsername, final String wsPassword) {

        return new LinkIDConfigurationServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificates( null ),
                new AbstractWSSecurityUsernameTokenCallback() {
                    @Override
                    public String getUsername() {

                        return wsUsername;
                    }

                    @Override
                    public String getPassword() {

                        return wsPassword;
                    }

                    @Nullable
                    @Override
                    public String handle(final String username) {

                        return null;
                    }

                    @Override
                    public boolean isInboundHeaderOptional() {

                        return true;
                    }
                } );
    }

    /**
     * Retrieve a proxy to the linkID configuration web service.
     *
     * @return proxy to the linkID configuration web service.
     */
    public static LinkIDConfigurationServiceClient getConfigurationService() {

        if (null != LinkIDSDKConfigHolder.config().linkID().app().username()) {

            return getConfigurationService( LinkIDSDKConfigHolder.config().linkID().app().username(),
                    LinkIDSDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getConfigurationService( new LinkIDSDKWSSecurityConfiguration(), null );
        }
    }
}
