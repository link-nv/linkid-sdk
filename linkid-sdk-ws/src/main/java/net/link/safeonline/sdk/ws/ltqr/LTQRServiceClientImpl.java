package net.link.safeonline.sdk.ws.ltqr;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.*;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.ltqr.*;
import net.link.safeonline.sdk.api.ltqr.*;
import net.link.safeonline.sdk.api.ltqr.LTQRSession;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import net.link.safeonline.sdk.api.ws.ltqr.LTQRServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.ltqr.LTQRServiceFactory;
import net.link.util.ws.AbstractWSClient;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.jetbrains.annotations.Nullable;


public class LTQRServiceClientImpl extends AbstractWSClient<LTQRServicePort> implements LTQRServiceClient {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the ltqr web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LTQRServiceClientImpl(String location, X509Certificate sslCertificate) {

        super( LTQRServiceFactory.newInstance().getLTQRServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.ltqr.path" ) ) );
    }

    @Override
    public LTQRSession push(final LTQRServiceProvider ltqrServiceProvider, @Nullable final PaymentContextDO paymentContextDO, final long timesUsable,
                            @Nullable final Date expiryDate, @Nullable final Long expiryDuration)
            throws PushException {

        PushRequest pushRequest = new PushRequest();

        // service provider credentials
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setUsername( ltqrServiceProvider.getUsername() );
        serviceProvider.setPassword( ltqrServiceProvider.getPassword() );
        pushRequest.setServiceProvider( serviceProvider );

        // payment context
        if (null != paymentContextDO) {

            PaymentContext paymentContext = new PaymentContext();
            paymentContext.setAmount( paymentContextDO.getAmount() );
            paymentContext.setCurrency( convert( paymentContextDO.getCurrency() ) );
            paymentContext.setDescription( paymentContextDO.getDescription() );
            paymentContext.setPaymentProfile( paymentContextDO.getPaymentProfile() );
            paymentContext.setValidationTime( paymentContextDO.getPaymentValidationTime() );
            paymentContext.setShowAddPaymentMethodLink( paymentContextDO.isShowAddPaymentMethodLink() );
            paymentContext.setAllowDeferredPay( paymentContextDO.isAllowDeferredPay() );

            pushRequest.setPaymentContext( paymentContext );
        }

        // configuration
        pushRequest.setTimesUsable( timesUsable );
        if (null != expiryDate) {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime( expiryDate );
            try {
                pushRequest.setExpiryDate( DatatypeFactory.newInstance().newXMLGregorianCalendar( c ) );
            }
            catch (DatatypeConfigurationException e) {
                throw new InternalInconsistencyException( e );
            }
        }
        pushRequest.setExpiryDuration( expiryDuration );

        // operate
        PushResponse response = getPort().push( pushRequest );

        // convert response
        if (null != response.getError()) {
            throw new PushException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            // convert base64 encoded QR image
            byte[] qrCodeImage;
            try {
                qrCodeImage = Base64.decode( response.getSuccess().getEncodedQR() );
            }
            catch (Base64DecodingException e) {
                throw new InternalInconsistencyException( "Could not decode the QR image!" );
            }

            return new LTQRSession( qrCodeImage, response.getSuccess().getQrContent(), response.getSuccess().getSessionId() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private PushErrorCode convert(final ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return PushErrorCode.ERROR_CREDENTIALS_INVALID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private Currency convert(final net.link.safeonline.sdk.api.payment.Currency currency) {

        switch (currency) {

            case EUR:
                return Currency.EUR;
        }

        throw new InternalInconsistencyException( String.format( "Currency %s is not supported!", currency.name() ) );
    }
}
