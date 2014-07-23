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
import net.link.safeonline.sdk.api.ws.attrib.client.AttributeClient;
import net.link.safeonline.sdk.api.ws.auth.AuthServiceClient;
import net.link.safeonline.sdk.api.ws.data.client.DataClient;
import net.link.safeonline.sdk.api.ws.haws.HawsServiceClient;
import net.link.safeonline.sdk.api.ws.idmapping.client.NameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.ltqr.LTQRServiceClient;
import net.link.safeonline.sdk.api.ws.mandate.MandateServiceClient;
import net.link.safeonline.sdk.api.ws.payment.PaymentServiceClient;
import net.link.safeonline.sdk.api.ws.sts.client.SecurityTokenServiceClient;
import net.link.safeonline.sdk.api.ws.xkms2.client.Xkms2Client;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.safeonline.sdk.configuration.SDKConfigHolder;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.auth.AuthServiceClientImpl;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.haws.HawsServiceClientImpl;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.sdk.ws.ltqr.LTQRServiceClientImpl;
import net.link.safeonline.sdk.ws.mandate.MandateServiceClientImpl;
import net.link.safeonline.sdk.ws.payment.PaymentServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.xkms2.Xkms2ClientImpl;
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
public class LinkIDServiceFactory extends ServiceFactory {

    private static ServiceFactory instance = new LinkIDServiceFactory();

    protected LinkIDServiceFactory() {

    }

    private static ServiceFactory getInstance() {

        return instance;
    }

    private static String getWsBase() {

        return String.format( "%s/%s", SDKConfigHolder.config().web().linkIDBase(), LinkIDConstants.LINKID_PATH_WS_BASE );
    }

    private static String getWsUsernameBase() {

        return String.format( "%s/%s", SDKConfigHolder.config().web().linkIDBase(), LinkIDConstants.LINKID_PATH_WS_USERNAME_BASE );
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
    public static AttributeClient getAttributeService() {

        return getInstance()._getAttributeService( new SDKWSSecurityConfiguration(), null );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                       validation.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static AttributeClient getAttributeService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                      final X509Certificate sslCertificate) {

        return getInstance()._getAttributeService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static AttributeClient getAttributeService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return getInstance()._getAttributeService( configuration, sslCertificate );
    }

    @Override
    protected AttributeClient _getAttributeService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new AttributeClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static DataClient getDataService() {

        return getDataService( new SDKWSSecurityConfiguration(), null );
    }

    /**
     * Retrieve a proxy to the linkID data web service.
     *
     * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                       validation.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID data web service.
     */
    public static DataClient getDataService(@Nullable final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                            @Nullable final X509Certificate sslCertificate) {

        return getInstance()._getDataService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID data web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID data web service.
     */
    public static DataClient getDataService(final WSSecurityConfiguration configuration, @Nullable X509Certificate sslCertificate) {

        return getInstance()._getDataService( configuration, sslCertificate );
    }

    @Override
    protected DataClient _getDataService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new DataClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NameIdentifierMappingClient getIdMappingService() {

        return getIdMappingService( new SDKWSSecurityConfiguration(), null );
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service.
     *
     * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                       validation.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NameIdentifierMappingClient getIdMappingService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                  final X509Certificate sslCertificate) {

        return getInstance()._getIdMappingService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NameIdentifierMappingClient getIdMappingService(final WSSecurityConfiguration configuration, @Nullable X509Certificate sslCertificate) {

        return getInstance()._getIdMappingService( configuration, sslCertificate );
    }

    @Override
    protected NameIdentifierMappingClient _getIdMappingService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new NameIdentifierMappingClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static SecurityTokenServiceClient getStsService() {

        return getStsService( new SDKWSSecurityConfiguration(), null );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                       validation.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static SecurityTokenServiceClient getStsService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                           final X509Certificate sslCertificate) {

        return getInstance()._getStsService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static SecurityTokenServiceClient getStsService(final WSSecurityConfiguration configuration, @Nullable X509Certificate sslCertificate) {

        return getInstance()._getStsService( configuration, sslCertificate );
    }

    @Override
    protected SecurityTokenServiceClient _getStsService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new SecurityTokenServiceClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID session tracking web service.
     *
     * @return proxy to the linkID session tracking web service.
     */
    public static PaymentServiceClient getPaymentService() {

        return getPaymentService( null );
    }

    /**
     * Retrieve a proxy to the linkID payment web service.
     *
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID session tracking web service.
     */
    public static PaymentServiceClient getPaymentService(@Nullable X509Certificate sslCertificate) {

        return getInstance()._getPaymentService( sslCertificate );
    }

    @Override
    protected PaymentServiceClient _getPaymentService(final X509Certificate sslCertificate) {

        return new PaymentServiceClientImpl( getWsBase(), getSSLCertificate( sslCertificate ) );
    }

    /**
     * Retrieve a proxy to the linkID XKMS2 web service.
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static Xkms2Client getXkms2Client() {

        return getXkms2Client( SDKConfigHolder.config().linkID().app().keyProvider() );
    }

    /**
     * Retrieve a proxy to the linkID XKMS 2 web service.
     *
     * @param keyProvider The key provider that provides the keys and certificates used by WS-Security for authentication and validation.
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static Xkms2Client getXkms2Client(@NotNull final KeyProvider keyProvider) {

        return getInstance()._getXkms2Client( keyProvider.getTrustedCertificate( ConfigUtils.SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID XKMS 2 web service.
     *
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static Xkms2Client getXkms2Client(X509Certificate sslCertificate) {

        return getInstance()._getXkms2Client( sslCertificate );
    }

    @Override
    protected Xkms2Client _getXkms2Client(X509Certificate sslCertificate) {

        return new Xkms2ClientImpl( getWsBase(), getSSLCertificate( sslCertificate ) );
    }

    /**
     * Retrieve a proxy to the linkID long term QR web service.
     *
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID long term QR web service.
     */
    public static LTQRServiceClient getLtqrServiceClient(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                         final X509Certificate sslCertificate) {

        return getInstance()._getLtqrServiceClient( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static LTQRServiceClient getLtqrServiceClient(final WSSecurityConfiguration configuration, @Nullable X509Certificate sslCertificate) {

        return getInstance()._getLtqrServiceClient( configuration, sslCertificate );
    }

    @Override
    protected LTQRServiceClient _getLtqrServiceClient(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new LTQRServiceClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID haws web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID haws web service.
     */
    public static HawsServiceClient<AuthnRequest, Response> getHawsService(final String wsUsername, final String wsPassword) {

        return new HawsServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificate( null ), new AbstractWSSecurityUsernameTokenCallback() {
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
    public static HawsServiceClient<AuthnRequest, Response> getHawsService() {

        if (null != SDKConfigHolder.config().linkID().app().username()) {

            return getHawsService( SDKConfigHolder.config().linkID().app().username(), SDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getHawsService( new SDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                       validation.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static HawsServiceClient<AuthnRequest, Response> getHawsService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                           final X509Certificate sslCertificate) {

        return getInstance()._getHawsService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static HawsServiceClient<AuthnRequest, Response> getHawsService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return getInstance()._getHawsService( configuration, sslCertificate );
    }

    @Override
    protected HawsServiceClient<AuthnRequest, Response> _getHawsService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new HawsServiceClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }

    /**
     * Retrieve a proxy to the linkID auth web service, using the WS-Security Username token profile
     *
     * @return proxy to the linkID auth web service.
     */
    public static AuthServiceClient<AuthnRequest, Response> getAuthService(final String wsUsername, final String wsPassword) {

        return new AuthServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificate( null ), new AbstractWSSecurityUsernameTokenCallback() {
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
    public static AuthServiceClient<AuthnRequest, Response> getAuthService() {

        if (null != SDKConfigHolder.config().linkID().app().username()) {

            return getAuthService( SDKConfigHolder.config().linkID().app().username(), SDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getAuthService( new SDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID auth web service.
     *
     * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                       validation.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID auth web service.
     */
    public static AuthServiceClient<AuthnRequest, Response> getAuthService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                                           final X509Certificate sslCertificate) {

        return getInstance()._getAuthService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID auth web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID auth web service.
     */
    public static AuthServiceClient<AuthnRequest, Response> getAuthService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return getInstance()._getAuthService( configuration, sslCertificate );
    }

    @Override
    protected AuthServiceClient<AuthnRequest, Response> _getAuthService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new AuthServiceClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }

    private static X509Certificate getSSLCertificate(final X509Certificate sslCertificate) {

        return ifNotNullElseNullable( sslCertificate, new NSupplier<X509Certificate>() {
            public X509Certificate get() {

                try {
                    return SDKConfigHolder.config().linkID().app().keyProvider().getTrustedCertificate( ConfigUtils.SSL_ALIAS );
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
    public static MandateServiceClient getMandateService(final String wsUsername, final String wsPassword) {

        return new MandateServiceClientImpl( getWsUsernameBase(), LinkIDServiceFactory.getSSLCertificate( null ),
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
    public static MandateServiceClient getMandateService() {

        if (null != SDKConfigHolder.config().linkID().app().username()) {

            return getMandateService( SDKConfigHolder.config().linkID().app().username(), SDKConfigHolder.config().linkID().app().password() );
        } else {

            return getInstance()._getMandateService( new SDKWSSecurityConfiguration(), null );
        }
    }

    /**
     * Retrieve a proxy to the linkID mandate web service.
     *
     * @param trustedDN      The DN of the certificate that incoming WS-Security messages are signed with.
     * @param keyProvider    The key provider that provides the keys and certificates used by WS-Security for authentication and
     *                       validation.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID mandate web service.
     */
    public static MandateServiceClient getMandateService(final X500Principal trustedDN, @NotNull final KeyProvider keyProvider,
                                                         final X509Certificate sslCertificate) {

        return getInstance()._getMandateService( new SDKWSSecurityConfiguration( trustedDN, keyProvider ), sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID mandate web service.
     *
     * @param configuration  Configuration of the WS-Security layer that secures the transport.
     * @param sslCertificate The server's SSL certificate.  If not {@code null}, validates whether SSL is encrypted using the given
     *                       certificate.
     *
     * @return proxy to the linkID mandate web service.
     */
    public static MandateServiceClient getMandateService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return getInstance()._getMandateService( configuration, sslCertificate );
    }

    @Override
    protected MandateServiceClient _getMandateService(final WSSecurityConfiguration configuration, X509Certificate sslCertificate) {

        return new MandateServiceClientImpl( getWsBase(), getSSLCertificate( sslCertificate ), configuration );
    }
}