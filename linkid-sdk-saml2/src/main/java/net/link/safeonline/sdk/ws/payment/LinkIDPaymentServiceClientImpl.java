/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.payment;

import com.google.common.collect.Lists;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.common.PaymentTransactionV20;
import net.lin_k.safe_online.common.WalletTransactionV20;
import net.lin_k.safe_online.payment._3.PaymentServicePort;
import net.lin_k.safe_online.payment._3.PaymentStatusRequest;
import net.lin_k.safe_online.payment._3.PaymentStatusResponse;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentTransaction;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentDetails;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentServiceClient;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.payment.LinkIDPaymentServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class LinkIDPaymentServiceClientImpl extends AbstractWSClient<PaymentServicePort> implements LinkIDPaymentServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the payment web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDPaymentServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the payment web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDPaymentServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                          final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDPaymentServiceClientImpl(String location, X509Certificate[] sslCertificates) {

        super( LinkIDPaymentServiceFactory.newInstance().getPaymentServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.payment.path" ) ) );
    }

    @Override
    public LinkIDPaymentStatus getStatus(final String orderReference)
            throws LinkIDWSClientTransportException {

        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setOrderReference( orderReference );

        try {
            // operate
            PaymentStatusResponse statusResponse = getPort().status( request );

            // parse
            List<LinkIDPaymentTransaction> transactions = Lists.newLinkedList();
            for (PaymentTransactionV20 paymentTransaction : statusResponse.getPaymentDetails().getPaymentTransactions()) {
                transactions.add( new LinkIDPaymentTransaction( LinkIDSDKUtils.convert( paymentTransaction.getPaymentMethodType() ),
                        paymentTransaction.getPaymentMethod(), LinkIDSDKUtils.convert( paymentTransaction.getPaymentState() ),
                        LinkIDSDKUtils.convert( paymentTransaction.getCreationDate() ), LinkIDSDKUtils.convert( paymentTransaction.getAuthorizationDate() ),
                        LinkIDSDKUtils.convert( paymentTransaction.getCapturedDate() ), paymentTransaction.getDocdataReference(),
                        paymentTransaction.getAmount(), LinkIDSDKUtils.convert( paymentTransaction.getCurrency() ) ) );
            }

            List<LinkIDWalletTransaction> walletTransactions = Lists.newLinkedList();
            for (WalletTransactionV20 walletTransaction : statusResponse.getPaymentDetails().getWalletTransactions()) {
                walletTransactions.add(
                        new LinkIDWalletTransaction( walletTransaction.getWalletId(), LinkIDSDKUtils.convert( walletTransaction.getCreationDate() ),
                                walletTransaction.getTransactionId(), walletTransaction.getAmount(),
                                LinkIDSDKUtils.convert( walletTransaction.getCurrency() ) ) );
            }

            return new LinkIDPaymentStatus( statusResponse.getOrderReference(), statusResponse.getUserId(),
                    LinkIDSDKUtils.convert( statusResponse.getPaymentStatus() ), statusResponse.isAuthorized(), statusResponse.isCaptured(),
                    statusResponse.getAmountPayed(), statusResponse.getAmount(), LinkIDSDKUtils.convert( statusResponse.getCurrency() ),
                    statusResponse.getDescription(), statusResponse.getProfile(), LinkIDSDKUtils.convert( statusResponse.getCreated() ),
                    statusResponse.getMandateReference(), new LinkIDPaymentDetails( transactions, walletTransactions ) );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }
}
