/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.ltqr;

import com.google.common.collect.Lists;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.ltqr.*;
import net.link.safeonline.sdk.api.ltqr.ErrorCode;
import net.link.safeonline.sdk.api.ltqr.*;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import net.link.safeonline.sdk.api.ws.ltqr.LTQRServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.ltqr.LTQRServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.jetbrains.annotations.Nullable;


public class LTQRServiceClientImpl extends AbstractWSClient<LTQRServicePort> implements LTQRServiceClient {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security configuration
     */
    public LTQRServiceClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        this( location, sslCertificate );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the ltqr web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LTQRServiceClientImpl(final String location, final X509Certificate sslCertificate, final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificate );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LTQRServiceClientImpl(final String location, final X509Certificate sslCertificate) {

        super( LTQRServiceFactory.newInstance().getLTQRServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.ltqr.path" ) ) );
    }

    @Override
    public LTQRSession push(@Nullable final PaymentContextDO paymentContextDO, final boolean oneTimeUse, @Nullable final Date expiryDate,
                            @Nullable final Long expiryDuration)
            throws PushException {

        PushRequest request = new PushRequest();

        // payment context
        if (null != paymentContextDO) {

            PaymentContext paymentContext = new PaymentContext();
            paymentContext.setAmount( paymentContextDO.getAmount() );
            paymentContext.setCurrency( convert( paymentContextDO.getCurrency() ) );
            paymentContext.setDescription( paymentContextDO.getDescription() );
            paymentContext.setOrderReference( paymentContextDO.getOrderReference() );
            paymentContext.setPaymentProfile( paymentContextDO.getPaymentProfile() );
            paymentContext.setValidationTime( paymentContextDO.getPaymentValidationTime() );
            paymentContext.setAllowDeferredPay( paymentContextDO.isAllowDeferredPay() );

            request.setPaymentContext( paymentContext );
        }

        // configuration
        request.setOneTimeUse( oneTimeUse );
        if (null != expiryDate) {
            request.setExpiryDate( SDKUtils.convert( expiryDate ) );
        }
        request.setExpiryDuration( expiryDuration );

        // operate
        PushResponse response = getPort().push( request );

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

            return new LTQRSession( qrCodeImage, response.getSuccess().getQrContent(), response.getSuccess().getOrderReference() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public List<LTQRClientSession> pull(@Nullable final List<String> orderReferences, @Nullable final List<String> clientSessionIds)
            throws PullException {

        PullRequest request = new PullRequest();

        if (null != orderReferences && !orderReferences.isEmpty()) {
            request.getOrderReferences().addAll( orderReferences );
        }

        if (null != clientSessionIds && !clientSessionIds.isEmpty()) {
            request.getClientSessionIds().addAll( clientSessionIds );
        }

        // operate
        PullResponse response = getPort().pull( request );

        // convert response
        if (null != response.getError()) {
            throw new PullException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            List<LTQRClientSession> clientSessions = Lists.newLinkedList();

            for (ClientSession clientSession : response.getSuccess().getSessions()) {

                clientSessions.add( new LTQRClientSession( clientSession.getOrderReference(), clientSession.getClientSessionId(), clientSession.getUserId(),
                        clientSession.getCreated().toGregorianCalendar().getTime(), convert( clientSession.getPaymentStatus() ) ) );
            }

            return clientSessions;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void remove(@Nullable final List<String> orderReferences, @Nullable final List<String> clientSessionIds)
            throws RemoveException {

        RemoveRequest request = new RemoveRequest();

        if (null == orderReferences || orderReferences.isEmpty()) {
            throw new InternalInconsistencyException( "orderReferences list cannot be empty!" );
        }

        request.getOrderReferences().addAll( orderReferences );

        if (null != clientSessionIds && !clientSessionIds.isEmpty()) {
            request.getClientSessionIds().addAll( clientSessionIds );
        }

        // operate
        RemoveResponse response = getPort().remove( request );

        // convert response
        if (null != response.getError()) {
            throw new RemoveException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private LTQRPaymentState convert(final LTQRPaymentStatusType wsPaymentStatusType) {

        if (null == wsPaymentStatusType)
            return null;

        switch (wsPaymentStatusType) {

            case STARTED:
                return LTQRPaymentState.STARTED;
            case AUTHORIZED:
                return LTQRPaymentState.PAYED;
            case FAILED:
                return LTQRPaymentState.FAILED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected payment status type %s!", wsPaymentStatusType.name() ) );
    }

    private ErrorCode convert(final net.lin_k.safe_online.ltqr.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return ErrorCode.ERROR_CREDENTIALS_INVALID;
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
