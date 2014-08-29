/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.payment;

import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.payment.PaymentGetStatusRequest;
import net.lin_k.safe_online.payment.PaymentServicePort;
import net.lin_k.safe_online.payment.PaymentStatusResponse;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.payment.PaymentState;
import net.link.safeonline.sdk.api.ws.payment.PaymentServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.payment.PaymentServiceFactory;
import net.link.util.ws.AbstractWSClient;


public class PaymentServiceClientImpl extends AbstractWSClient<PaymentServicePort> implements PaymentServiceClient {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the payment web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public PaymentServiceClientImpl(String location, X509Certificate sslCertificate) {

        super( PaymentServiceFactory.newInstance().getPaymentServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.payment.path" ) ) );
    }

    @Override
    public PaymentState getStatus(final String orderReference)
            throws WSClientTransportException {

        PaymentGetStatusRequest request = new PaymentGetStatusRequest();
        request.setOrderReference( orderReference );

        try {
            PaymentStatusResponse statusResponse = getPort().getStatus( request );
            return SDKUtils.convert( statusResponse.getPaymentStatus() );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }
}
