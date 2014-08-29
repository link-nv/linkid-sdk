/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

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
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * <h2>{@link ServiceFactory}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class ServiceFactory {

    protected abstract AttributeClient _getAttributeService(WSSecurityConfiguration configuration, @Nullable X509Certificate sslCertificate);

    protected abstract DataClient _getDataService(WSSecurityConfiguration configuration, X509Certificate sslCertificate);

    protected abstract NameIdentifierMappingClient _getIdMappingService(WSSecurityConfiguration configuration, X509Certificate sslCertificate);

    protected abstract SecurityTokenServiceClient _getStsService(WSSecurityConfiguration configuration, X509Certificate sslCertificate);

    protected abstract PaymentServiceClient _getPaymentService(X509Certificate sslCertificate);

    protected abstract Xkms2Client _getXkms2Client(X509Certificate sslCertificate);

    protected abstract LTQRServiceClient _getLtqrServiceClient(WSSecurityConfiguration configuration, X509Certificate sslCertificate);

    protected abstract MandateServiceClient _getMandateService(WSSecurityConfiguration configuration, X509Certificate sslCertificate);

    protected abstract HawsServiceClient<AuthnRequest, Response> _getHawsService(WSSecurityConfiguration configuration,
                                                                                 @Nullable X509Certificate sslCertificate);

    protected abstract AuthServiceClient<AuthnRequest, Response> _getAuthService(WSSecurityConfiguration configuration,
                                                                                 @Nullable X509Certificate sslCertificate);
}
