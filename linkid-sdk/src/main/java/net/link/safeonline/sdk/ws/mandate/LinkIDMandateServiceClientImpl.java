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
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.ws.mandate.LinkIDErrorCode;
import net.link.safeonline.sdk.api.ws.mandate.LinkIDMandateServiceClient;
import net.link.safeonline.sdk.api.ws.mandate.LinkIDPayException;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.mandate.LinkIDMandateServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.jetbrains.annotations.Nullable;


public class LinkIDMandateServiceClientImpl extends AbstractWSClient<MandateServicePort> implements LinkIDMandateServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDMandateServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDMandateServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                          final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDMandateServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDMandateServiceFactory.newInstance().getMandateServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.mandate.path" ) ) );
    }

    @Override
    public String pay(final String mandateReference, final LinkIDPaymentContext linkIDPaymentContext, final Locale locale)
            throws LinkIDPayException {

        MandatePaymentRequest request = new MandatePaymentRequest();

        request.setPaymentContext( convert( linkIDPaymentContext ) );

        request.setMandateReference( mandateReference );

        if (null != locale) {
            request.setLanguage( locale.getLanguage() );
        } else {
            request.setLanguage( Locale.ENGLISH.getLanguage() );
        }

        // operate
        MandatePaymentResponse response = getPort().pay( request );

        if (null != response.getError()) {
            throw new LinkIDPayException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getOrderReference();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private PaymentContext convert(@Nullable final LinkIDPaymentContext linkIDPaymentContext) {

        if (null == linkIDPaymentContext)
            return null;

        PaymentContext paymentContext = new PaymentContext();
        paymentContext.setAmount( linkIDPaymentContext.getAmount().getAmount() );
        if (null != linkIDPaymentContext.getAmount().getCurrency()) {
            paymentContext.setCurrency( LinkIDSDKUtils.convert( linkIDPaymentContext.getAmount().getCurrency() ) );
        }
        if (null != linkIDPaymentContext.getAmount().getWalletCoin()) {
            paymentContext.setWalletCoin( linkIDPaymentContext.getAmount().getWalletCoin() );
        }
        paymentContext.setDescription( linkIDPaymentContext.getDescription() );
        paymentContext.setOrderReference( linkIDPaymentContext.getOrderReference() );
        paymentContext.setPaymentProfile( linkIDPaymentContext.getPaymentProfile() );
        paymentContext.setValidationTime( linkIDPaymentContext.getPaymentValidationTime() );
        paymentContext.setAllowDeferredPay( linkIDPaymentContext.isAllowDeferredPay() );
        paymentContext.setAllowPartial( linkIDPaymentContext.isAllowPartial() );
        paymentContext.setOnlyWallets( linkIDPaymentContext.isOnlyWallets() );
        paymentContext.setMandate( null != linkIDPaymentContext.getMandate() );
        if (null != linkIDPaymentContext.getMandate()) {
            paymentContext.setMandateDescription( linkIDPaymentContext.getMandate().getDescription() );
            paymentContext.setMandateReference( linkIDPaymentContext.getMandate().getReference() );
        }

        return paymentContext;
    }

    private LinkIDErrorCode convert(final net.lin_k.safe_online.mandate.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_MANDATE_ARCHIVED:
                return LinkIDErrorCode.ERROR_MANDATE_ARCHIVED;
            case ERROR_MANDATE_UNKNOWN:
                return LinkIDErrorCode.ERROR_MANDATE_UNKNOWN;
            case ERROR_MANDATE_PAYMENT_FAILED:
                return LinkIDErrorCode.ERROR_MANDATE_PAYMENT_FAILED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
