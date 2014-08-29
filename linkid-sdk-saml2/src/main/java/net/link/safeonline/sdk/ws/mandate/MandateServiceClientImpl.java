/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.mandate;

import java.security.cert.X509Certificate;
import java.util.Locale;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.common.PaymentContext;
import net.lin_k.safe_online.mandate.MandatePaymentRequest;
import net.lin_k.safe_online.mandate.MandatePaymentResponse;
import net.lin_k.safe_online.mandate.MandateServicePort;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import net.link.safeonline.sdk.api.ws.mandate.ErrorCode;
import net.link.safeonline.sdk.api.ws.mandate.MandateServiceClient;
import net.link.safeonline.sdk.api.ws.mandate.PayException;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.mandate.MandateServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class MandateServiceClientImpl extends AbstractWSClient<MandateServicePort> implements MandateServiceClient {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the mandate web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security configuration
     */
    public MandateServiceClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        this( location, sslCertificate );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the mandate web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public MandateServiceClientImpl(final String location, final X509Certificate sslCertificate, final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificate );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private MandateServiceClientImpl(final String location, final X509Certificate sslCertificate) {

        super( MandateServiceFactory.newInstance().getMandateServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.mandate.path" ) ) );
    }

    @Override
    public String pay(final String mandateReference, final PaymentContextDO paymentContextDO, final Locale locale)
            throws PayException {

        MandatePaymentRequest request = new MandatePaymentRequest();

        PaymentContext paymentContext = new PaymentContext();
        paymentContext.setAmount( paymentContextDO.getAmount() );
        paymentContext.setCurrency( SDKUtils.convert( paymentContextDO.getCurrency() ) );
        paymentContext.setDescription( paymentContextDO.getDescription() );
        paymentContext.setOrderReference( paymentContextDO.getOrderReference() );
        paymentContext.setPaymentProfile( paymentContextDO.getPaymentProfile() );
        request.setPaymentContext( paymentContext );

        request.setMandateReference( mandateReference );

        if (null != locale) {
            request.setLanguage( locale.getLanguage() );
        } else {
            request.setLanguage( Locale.ENGLISH.getLanguage() );
        }

        // operate
        MandatePaymentResponse response = getPort().pay( request );

        if (null != response.getError()) {
            throw new PayException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getOrderReference();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private ErrorCode convert(final net.lin_k.safe_online.mandate.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_MANDATE_ARCHIVED:
                return ErrorCode.ERROR_MANDATE_ARCHIVED;
            case ERROR_MANDATE_UNKNOWN:
                return ErrorCode.ERROR_MANDATE_UNKNOWN;
            case ERROR_MANDATE_PAYMENT_FAILED:
                return ErrorCode.ERROR_MANDATE_PAYMENT_FAILED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
