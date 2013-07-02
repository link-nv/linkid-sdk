package net.link.safeonline.sdk.ws.payment;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.payment.*;
import net.link.safeonline.sdk.api.payment.PaymentState;
import net.link.safeonline.sdk.api.ws.payment.PaymentServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.payment.PaymentServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;


public class PaymentServiceClientImpl extends AbstractWSClient<PaymentServicePort> implements PaymentServiceClient {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the payment web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security configuration
     */
    public PaymentServiceClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( PaymentServiceFactory.newInstance().getPaymentServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.payment.path" ) ) );

        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

    @Override
    public PaymentState getStatus(final String transactionId) {

        PaymentStatusRequest statusRequest = new PaymentStatusRequest();
        statusRequest.setTransactionId( transactionId );

        PaymentStatusResponse statusResponse = getPort().status( statusRequest );
        return convert( statusResponse.getPaymentStatus() );
    }

    private PaymentState convert(final PaymentStatusType paymentStatusType) {

        switch (paymentStatusType) {

            case STARTED:
                return PaymentState.STARTED;
            case AUTHORIZED:
                return PaymentState.PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Payment state type %s it not supported!", paymentStatusType ) );
    }
}
