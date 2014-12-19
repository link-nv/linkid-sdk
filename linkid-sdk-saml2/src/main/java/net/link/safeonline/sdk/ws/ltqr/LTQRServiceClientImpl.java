/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.ltqr;

import com.google.common.collect.Lists;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.common.Callback;
import net.lin_k.safe_online.common.PaymentContext;
import net.lin_k.safe_online.ltqr._2.ChangeRequest;
import net.lin_k.safe_online.ltqr._2.ChangeResponse;
import net.lin_k.safe_online.ltqr._2.ClientSession;
import net.lin_k.safe_online.ltqr._2.LTQRPaymentStatusType;
import net.lin_k.safe_online.ltqr._2.LTQRServicePort;
import net.lin_k.safe_online.ltqr._2.PullRequest;
import net.lin_k.safe_online.ltqr._2.PullResponse;
import net.lin_k.safe_online.ltqr._2.PushRequest;
import net.lin_k.safe_online.ltqr._2.PushResponse;
import net.lin_k.safe_online.ltqr._2.RemoveRequest;
import net.lin_k.safe_online.ltqr._2.RemoveResponse;
import net.link.safeonline.sdk.api.callback.CallbackDO;
import net.link.safeonline.sdk.api.ltqr.ChangeErrorCode;
import net.link.safeonline.sdk.api.ltqr.ChangeException;
import net.link.safeonline.sdk.api.ltqr.ErrorCode;
import net.link.safeonline.sdk.api.ltqr.LTQRClientSession;
import net.link.safeonline.sdk.api.ltqr.LTQRPaymentState;
import net.link.safeonline.sdk.api.ltqr.LTQRSession;
import net.link.safeonline.sdk.api.ltqr.PullException;
import net.link.safeonline.sdk.api.ltqr.PushException;
import net.link.safeonline.sdk.api.ltqr.RemoveException;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import net.link.safeonline.sdk.api.ws.ltqr.LTQRServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.ltqr.LTQRServiceFactory;
import net.link.util.InternalInconsistencyException;
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
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LTQRServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LTQRServiceClientImpl(final String location, final X509Certificate[] sslCertificates, final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LTQRServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LTQRServiceFactory.newInstance().getLTQRServicePort(), sslCertificates );

        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.ltqr.path" ) ) );
    }

    @Override
    public LTQRSession push(@Nullable String authenticationMessage, @Nullable String finishedMessage, @Nullable final PaymentContextDO paymentContextDO,
                            final boolean oneTimeUse, @Nullable final Date expiryDate, @Nullable final Long expiryDuration,
                            @Nullable final CallbackDO callbackDO)
            throws PushException {

        PushRequest request = new PushRequest();

        // custom msgs
        request.setAuthenticationMessage( authenticationMessage );
        request.setFinishedMessage( finishedMessage );

        // payment context
        if (null != paymentContextDO) {

            PaymentContext paymentContext = new PaymentContext();
            paymentContext.setAmount( paymentContextDO.getAmount() );
            paymentContext.setCurrency( SDKUtils.convert( paymentContextDO.getCurrency() ) );
            paymentContext.setDescription( paymentContextDO.getDescription() );
            paymentContext.setOrderReference( paymentContextDO.getOrderReference() );
            paymentContext.setPaymentProfile( paymentContextDO.getPaymentProfile() );
            paymentContext.setValidationTime( paymentContextDO.getPaymentValidationTime() );
            paymentContext.setAllowDeferredPay( paymentContextDO.isAllowDeferredPay() );

            request.setPaymentContext( paymentContext );
        }

        // callback
        if (null != callbackDO) {

            Callback callback = new Callback();
            callback.setLocation( callbackDO.getLocation() );
            callback.setAppSessionId( callbackDO.getAppSessionId() );
            callback.setInApp( callbackDO.isInApp() );
            request.setCallback( callback );
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

            return new LTQRSession( decodeQR( response.getSuccess().getEncodedQR() ), response.getSuccess().getQrContent(),
                    response.getSuccess().getLtqrReference(), response.getSuccess().getPaymentOrderReference() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LTQRSession change(final String ltqrReference, @Nullable String authenticationMessage, @Nullable String finishedMessage,
                              @Nullable final PaymentContextDO paymentContextDO, @Nullable final Date expiryDate, @Nullable final Long expiryDuration,
                              @Nullable final CallbackDO callbackDO)
            throws ChangeException {

        ChangeRequest request = new ChangeRequest();

        request.setLtqrReference( ltqrReference );

        // custom msgs
        request.setAuthenticationMessage( authenticationMessage );
        request.setFinishedMessage( finishedMessage );

        // payment context
        if (null != paymentContextDO) {

            PaymentContext paymentContext = new PaymentContext();
            paymentContext.setAmount( paymentContextDO.getAmount() );
            paymentContext.setCurrency( SDKUtils.convert( paymentContextDO.getCurrency() ) );
            paymentContext.setDescription( paymentContextDO.getDescription() );
            paymentContext.setOrderReference( paymentContextDO.getOrderReference() );
            paymentContext.setPaymentProfile( paymentContextDO.getPaymentProfile() );
            paymentContext.setValidationTime( paymentContextDO.getPaymentValidationTime() );
            paymentContext.setAllowDeferredPay( paymentContextDO.isAllowDeferredPay() );

            request.setPaymentContext( paymentContext );
        }

        // callback
        if (null != callbackDO) {

            Callback callback = new Callback();
            callback.setLocation( callbackDO.getLocation() );
            callback.setAppSessionId( callbackDO.getAppSessionId() );
            callback.setInApp( callbackDO.isInApp() );
            request.setCallback( callback );
        }

        // configuration
        if (null != expiryDate) {
            request.setExpiryDate( SDKUtils.convert( expiryDate ) );
        }
        if (null != expiryDuration) {
            request.setExpiryDuration( expiryDuration );
        }

        // operate
        ChangeResponse response = getPort().change( request );

        // convert response
        if (null != response.getError()) {
            throw new ChangeException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            return new LTQRSession( decodeQR( response.getSuccess().getEncodedQR() ), response.getSuccess().getQrContent(),
                    response.getSuccess().getLtqrReference(), response.getSuccess().getPaymentOrderReference() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public List<LTQRClientSession> pull(@Nullable final List<String> ltqrReferences, @Nullable final List<String> paymentOrderReferences,
                                        @Nullable final List<String> clientSessionIds)
            throws PullException {

        PullRequest request = new PullRequest();

        if (null != ltqrReferences && !ltqrReferences.isEmpty()) {
            request.getLtqrReferences().addAll( ltqrReferences );
        }

        if (null != paymentOrderReferences && !paymentOrderReferences.isEmpty()) {
            request.getPaymentOrderReferences().addAll( paymentOrderReferences );
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

                clientSessions.add(
                        new LTQRClientSession( decodeQR( clientSession.getEncodedQR() ), clientSession.getQrContent(), clientSession.getLtqrReference(),
                                clientSession.getClientSessionId(), clientSession.getUserId(), clientSession.getCreated().toGregorianCalendar().getTime(),
                                convert( clientSession.getPaymentStatus() ), clientSession.getPaymentOrderReference() ) );
            }

            return clientSessions;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void remove(@Nullable final List<String> ltqrReferences, @Nullable final List<String> paymentOrderReferences,
                       @Nullable final List<String> clientSessionIds)
            throws RemoveException {

        RemoveRequest request = new RemoveRequest();

        if (null == ltqrReferences || ltqrReferences.isEmpty()) {
            throw new InternalInconsistencyException( "Removing LTQR session requires the LTQR references to be not empty" );
        }

        request.getLtqrReferences().addAll( ltqrReferences );

        if (null != paymentOrderReferences && !paymentOrderReferences.isEmpty()) {
            request.getPaymentOrderReferences().addAll( paymentOrderReferences );
        }

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

        if (null == wsPaymentStatusType) {
            return null;
        }

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

    private ErrorCode convert(final net.lin_k.safe_online.ltqr._2.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return ErrorCode.ERROR_CREDENTIALS_INVALID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private ChangeErrorCode convert(final net.lin_k.safe_online.ltqr._2.ChangeErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return ChangeErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_NOT_FOUND:
                return ChangeErrorCode.ERROR_NOT_FOUND;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private byte[] decodeQR(final String encodedQR) {

        // convert base64 encoded QR image
        byte[] qrCodeImage;
        try {
            qrCodeImage = Base64.decode( encodedQR );
        }
        catch (Base64DecodingException e) {
            throw new InternalInconsistencyException( "Could not decode the QR image!" );
        }
        return qrCodeImage;
    }
}
