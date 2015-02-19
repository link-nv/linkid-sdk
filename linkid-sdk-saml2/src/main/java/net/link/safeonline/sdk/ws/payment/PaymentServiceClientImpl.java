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
import net.lin_k.safe_online.payment._3.PaymentServicePort;
import net.lin_k.safe_online.payment._3.PaymentStatusRequest;
import net.lin_k.safe_online.payment._3.PaymentStatusResponse;
import net.lin_k.safe_online.payment._3.PaymentTransaction;
import net.lin_k.safe_online.payment._3.WalletTransaction;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.payment.PaymentDetails;
import net.link.safeonline.sdk.api.ws.payment.PaymentServiceClient;
import net.link.safeonline.sdk.api.ws.payment.PaymentStatusDO;
import net.link.safeonline.sdk.api.ws.payment.PaymentTransactionDO;
import net.link.safeonline.sdk.api.ws.payment.WalletTransactionDO;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.payment.PaymentServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class PaymentServiceClientImpl extends AbstractWSClient<PaymentServicePort> implements PaymentServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the payment web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public PaymentServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the payment web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public PaymentServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                    final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private PaymentServiceClientImpl(String location, X509Certificate[] sslCertificates) {

        super( PaymentServiceFactory.newInstance().getPaymentServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.payment.path" ) ) );
    }

    @Override
    public PaymentStatusDO getStatus(final String orderReference)
            throws WSClientTransportException {

        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setOrderReference( orderReference );

        try {
            // operate
            PaymentStatusResponse statusResponse = getPort().status( request );

            // parse
            List<PaymentTransactionDO> transactions = Lists.newLinkedList();
            for (PaymentTransaction paymentTransaction : statusResponse.getPaymentDetails().getPaymentTransactions()) {
                transactions.add( new PaymentTransactionDO( SDKUtils.convert( paymentTransaction.getPaymentStatus() ),
                        SDKUtils.convert( paymentTransaction.getCreationDate() ), SDKUtils.convert( paymentTransaction.getAuthorizationDate() ),
                        SDKUtils.convert( paymentTransaction.getCaptureDate() ), paymentTransaction.getDocdataReference(), paymentTransaction.getAmount(),
                        SDKUtils.convert( paymentTransaction.getCurrency() ) ) );
            }

            List<WalletTransactionDO> walletTransactions = Lists.newLinkedList();
            for (WalletTransaction walletTransaction : statusResponse.getPaymentDetails().getWalletTransactions()) {
                walletTransactions.add( new WalletTransactionDO( walletTransaction.getWalletId(), SDKUtils.convert( walletTransaction.getCreationDate() ),
                        walletTransaction.getTransactionId(), walletTransaction.getAmount(), SDKUtils.convert( walletTransaction.getCurrency() ) ) );
            }

            return new PaymentStatusDO( SDKUtils.convert( statusResponse.getPaymentStatus() ), statusResponse.isCaptured(), statusResponse.getAmountPayed(),
                    new PaymentDetails( transactions, walletTransactions ) );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }
}
