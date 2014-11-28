/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.util;

import java.lang.reflect.Field;
import java.security.cert.X509Certificate;
import net.link.safeonline.sdk.api.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.api.ws.auth.AuthServiceClient;
import net.link.safeonline.sdk.api.ws.data.client.DataClient;
import net.link.safeonline.sdk.api.ws.haws.HawsServiceClient;
import net.link.safeonline.sdk.api.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.ltqr.LTQRServiceClient;
import net.link.safeonline.sdk.api.ws.mandate.MandateServiceClient;
import net.link.safeonline.sdk.api.ws.payment.PaymentServiceClient;
import net.link.safeonline.sdk.api.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.api.ws.xkms2.Xkms2Client;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.safeonline.sdk.ws.ServiceFactory;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * <h2>{@link DummyServiceFactory}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Mar 3, 2009</i> </p>
 *
 * @author lhunath
 */
public class DummyServiceFactory extends ServiceFactory {

    private static final DummyServiceFactory instance = new DummyServiceFactory();

    protected DummyServiceFactory() {

    }

    private static DummyServiceFactory getInstance() {

        return instance;
    }

    public static void install()
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Field linkidInstance = LinkIDServiceFactory.class.getDeclaredField( "instance" );
        linkidInstance.setAccessible( true );
        linkidInstance.set( null, getInstance() );
    }

    @Override
    protected AttributeClient _getAttributeService(final WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new DummyAttributeClient();
    }

    @Override
    protected DataClient _getDataService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected NameIdentifierMappingClient _getIdMappingService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        return new DummyNameIdentifierMappingClient();
    }

    @Override
    protected SecurityTokenServiceClient _getStsService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected PaymentServiceClient _getPaymentService(final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected Xkms2Client _getXkms2Client(X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected LTQRServiceClient _getLtqrServiceClient(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected MandateServiceClient _getMandateService(final WSSecurityConfiguration configuration, final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected HawsServiceClient<AuthnRequest, Response> _getHawsService(final WSSecurityConfiguration configuration,
                                                                        @Nullable final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    protected AuthServiceClient<AuthnRequest, Response> _getAuthService(final WSSecurityConfiguration configuration,
                                                                        @Nullable final X509Certificate[] sslCertificates) {

        throw new UnsupportedOperationException( "Not yet implemented" );
    }
}
