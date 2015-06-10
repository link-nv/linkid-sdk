/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.util;

import java.lang.reflect.Field;
import java.security.cert.X509Certificate;
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
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.safeonline.sdk.ws.LinkIDAbstractServiceFactory;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * <h2>{@link LinkIDDummyServiceFactory}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Mar 3, 2009</i> </p>
 *
 * @author lhunath
 */
public class LinkIDDummyServiceFactory extends LinkIDAbstractServiceFactory {

    private static final LinkIDDummyServiceFactory instance = new LinkIDDummyServiceFactory();

    protected LinkIDDummyServiceFactory() {

    }

    private static LinkIDDummyServiceFactory getInstance() {

        return instance;
    }

    public static void install()
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Field linkidInstance = LinkIDServiceFactory.class.getDeclaredField( "instance" );
        linkidInstance.setAccessible( true );
        linkidInstance.set( null, getInstance() );
    }

    @Override
    protected LinkIDAttributeClient _getAttributeService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDDummyAttributeClient();
    }

    @Override
    protected LinkIDDataClient _getDataService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDNameIdentifierMappingClient _getIdMappingService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new LinkIDDummyNameIdentifierMappingClient();
    }

    @Override
    protected LinkIDSecurityTokenServiceClient _getStsService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDPaymentServiceClient _getPaymentService(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDXkms2Client _getXkms2Client(X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDLTQRServiceClient _getLtqrServiceClient(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDMandateServiceClient _getMandateService(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDHawsServiceClient<AuthnRequest, Response> _getHawsService(final WSSecurityConfiguration configuration,
                                                                        @Nullable final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDAuthServiceClient<AuthnRequest, Response> _getAuthService(final WSSecurityConfiguration configuration,
                                                                        @Nullable final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDCaptureServiceClient _getCaptureService(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDWalletServiceClient _getWalletService(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LinkIDConfigurationServiceClient _getConfigurationService(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }
}
