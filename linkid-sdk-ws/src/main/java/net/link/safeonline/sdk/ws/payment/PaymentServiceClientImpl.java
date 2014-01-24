package net.link.safeonline.sdk.ws.payment;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.payment.*;
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
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.payment.path" ) ) );
    }

    @Override
    public PaymentState getStatus(final String transactionId)
            throws WSClientTransportException {

        PaymentStatusRequest statusRequest = new PaymentStatusRequest();
        statusRequest.setTransactionId( transactionId );

        try {
            PaymentStatusResponse statusResponse = getPort().status( statusRequest );
            return convert( statusResponse.getPaymentStatus() );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private PaymentState convert(final PaymentStatusType paymentStatusType) {

        if (null == paymentStatusType)
            return PaymentState.STARTED;

        switch (paymentStatusType) {

            case STARTED:
                return PaymentState.STARTED;
            case AUTHORIZED:
                return PaymentState.PAYED;
            case FAILED:
                return PaymentState.FAILED;
            case REFUNDED:
                return PaymentState.REFUNDED;
            case REFUND_STARTED:
                return PaymentState.REFUND_STARTED;
        }

        throw new InternalInconsistencyException( String.format( "Payment state type %s it not supported!", paymentStatusType ) );
    }
}
