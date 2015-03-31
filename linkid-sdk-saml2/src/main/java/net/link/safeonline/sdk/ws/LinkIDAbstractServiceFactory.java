/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

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
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * <h2>{@link LinkIDAbstractServiceFactory}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class LinkIDAbstractServiceFactory {

    protected abstract LinkIDAttributeClient _getAttributeService(WSSecurityConfiguration configuration, @Nullable X509Certificate[] sslCertificates);

    protected abstract LinkIDDataClient _getDataService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDNameIdentifierMappingClient _getIdMappingService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDSecurityTokenServiceClient _getStsService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDPaymentServiceClient _getPaymentService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDXkms2Client _getXkms2Client(X509Certificate[] sslCertificates);

    protected abstract LinkIDLTQRServiceClient _getLtqrServiceClient(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDMandateServiceClient _getMandateService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDHawsServiceClient<AuthnRequest, Response> _getHawsService(WSSecurityConfiguration configuration,
                                                                                 @Nullable X509Certificate[] sslCertificates);

    protected abstract LinkIDAuthServiceClient<AuthnRequest, Response> _getAuthService(WSSecurityConfiguration configuration,
                                                                                 @Nullable X509Certificate[] sslCertificates);

    protected abstract LinkIDCaptureServiceClient _getCaptureService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDWalletServiceClient _getWalletService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);

    protected abstract LinkIDConfigurationServiceClient _getConfigurationService(WSSecurityConfiguration configuration, X509Certificate[] sslCertificates);
}
