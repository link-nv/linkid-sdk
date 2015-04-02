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
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.ltqr.LinkIDChangeErrorCode;
import net.link.safeonline.sdk.api.ltqr.LinkIDChangeException;
import net.link.safeonline.sdk.api.ltqr.LinkIDErrorCode;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRClientSession;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRPaymentState;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ltqr.LinkIDPullException;
import net.link.safeonline.sdk.api.ltqr.LinkIDPushException;
import net.link.safeonline.sdk.api.ltqr.LinkIDRemoveException;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.ws.ltqr.LinkIDLTQRServiceClient;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.ltqr.LinkIDLTQRServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.jetbrains.annotations.Nullable;


public class LinkIDLTQRServiceClientImpl extends AbstractWSClient<LTQRServicePort> implements LinkIDLTQRServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDLTQRServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDLTQRServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                       final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDLTQRServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDLTQRServiceFactory.newInstance().getLTQRServicePort(), sslCertificates );

        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.ltqr.path" ) ) );
    }

    @Override
    public LinkIDLTQRSession push(@Nullable String authenticationMessage, @Nullable String finishedMessage,
                                  @Nullable final LinkIDPaymentContext linkIDPaymentContext, final boolean oneTimeUse, @Nullable final Date expiryDate,
                                  @Nullable final Long expiryDuration, @Nullable final LinkIDCallback linkIDCallback,
                                  @Nullable final List<String> identityProfiles, @Nullable final Long sessionExpiryOverride, @Nullable final String theme)
            throws LinkIDPushException {

        PushRequest request = new PushRequest();

        // custom msgs
        request.setAuthenticationMessage( authenticationMessage );
        request.setFinishedMessage( finishedMessage );

        // payment context
        if (null != linkIDPaymentContext) {

            PaymentContext paymentContext = new PaymentContext();
            paymentContext.setAmount( linkIDPaymentContext.getAmount() );
            paymentContext.setCurrency( LinkIDSDKUtils.convert( linkIDPaymentContext.getCurrency() ) );
            paymentContext.setDescription( linkIDPaymentContext.getDescription() );
            paymentContext.setOrderReference( linkIDPaymentContext.getOrderReference() );
            paymentContext.setPaymentProfile( linkIDPaymentContext.getPaymentProfile() );
            paymentContext.setValidationTime( linkIDPaymentContext.getPaymentValidationTime() );
            paymentContext.setAllowDeferredPay( linkIDPaymentContext.isAllowDeferredPay() );
            paymentContext.setAllowPartial( linkIDPaymentContext.isAllowPartial() );
            paymentContext.setOnlyWallets( linkIDPaymentContext.isOnlyWallets() );

            request.setPaymentContext( paymentContext );
        }

        // callback
        if (null != linkIDCallback) {

            Callback callback = new Callback();
            callback.setLocation( linkIDCallback.getLocation() );
            callback.setAppSessionId( linkIDCallback.getAppSessionId() );
            callback.setInApp( linkIDCallback.isInApp() );
            request.setCallback( callback );
        }

        // identity profiles
        if (null != identityProfiles && !identityProfiles.isEmpty()) {

            for (String identityProfile : identityProfiles) {
                request.getIdentityProfiles().add( identityProfile );
            }
        }

        if (null != sessionExpiryOverride) {
            request.setSessionExpiryOverride( sessionExpiryOverride );
        }
        if (null != theme) {
            request.setTheme( theme );
        }

        // configuration
        request.setOneTimeUse( oneTimeUse );
        if (null != expiryDate) {
            request.setExpiryDate( LinkIDSDKUtils.convert( expiryDate ) );
        }
        request.setExpiryDuration( expiryDuration );

        // operate
        PushResponse response = getPort().push( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDPushException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return new LinkIDLTQRSession( decodeQR( response.getSuccess().getEncodedQR() ), response.getSuccess().getQrContent(),
                    response.getSuccess().getLtqrReference(), response.getSuccess().getPaymentOrderReference() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDLTQRSession change(final String ltqrReference, @Nullable String authenticationMessage, @Nullable String finishedMessage,
                                    @Nullable final LinkIDPaymentContext linkIDPaymentContext, @Nullable final Date expiryDate,
                                    @Nullable final Long expiryDuration, @Nullable final LinkIDCallback linkIDCallback,
                                    @Nullable final List<String> identityProfiles, @Nullable final Long sessionExpiryOverride, @Nullable final String theme)
            throws LinkIDChangeException {

        ChangeRequest request = new ChangeRequest();

        request.setLtqrReference( ltqrReference );

        // custom msgs
        request.setAuthenticationMessage( authenticationMessage );
        request.setFinishedMessage( finishedMessage );

        // payment context
        if (null != linkIDPaymentContext) {

            PaymentContext paymentContext = new PaymentContext();
            paymentContext.setAmount( linkIDPaymentContext.getAmount() );
            paymentContext.setCurrency( LinkIDSDKUtils.convert( linkIDPaymentContext.getCurrency() ) );
            paymentContext.setDescription( linkIDPaymentContext.getDescription() );
            paymentContext.setOrderReference( linkIDPaymentContext.getOrderReference() );
            paymentContext.setPaymentProfile( linkIDPaymentContext.getPaymentProfile() );
            paymentContext.setValidationTime( linkIDPaymentContext.getPaymentValidationTime() );
            paymentContext.setAllowDeferredPay( linkIDPaymentContext.isAllowDeferredPay() );
            paymentContext.setAllowPartial( linkIDPaymentContext.isAllowPartial() );
            paymentContext.setOnlyWallets( linkIDPaymentContext.isOnlyWallets() );

            request.setPaymentContext( paymentContext );
        }

        // callback
        if (null != linkIDCallback) {

            Callback callback = new Callback();
            callback.setLocation( linkIDCallback.getLocation() );
            callback.setAppSessionId( linkIDCallback.getAppSessionId() );
            callback.setInApp( linkIDCallback.isInApp() );
            request.setCallback( callback );
        }

        // identity profiles
        if (null != identityProfiles && !identityProfiles.isEmpty()) {

            for (String identityProfile : identityProfiles) {
                request.getIdentityProfiles().add( identityProfile );
            }
        }

        if (null != sessionExpiryOverride) {
            request.setSessionExpiryOverride( sessionExpiryOverride );
        }
        if (null != theme) {
            request.setTheme( theme );
        }

        // configuration
        if (null != expiryDate) {
            request.setExpiryDate( LinkIDSDKUtils.convert( expiryDate ) );
        }
        if (null != expiryDuration) {
            request.setExpiryDuration( expiryDuration );
        }

        // operate
        ChangeResponse response = getPort().change( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDChangeException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            return new LinkIDLTQRSession( decodeQR( response.getSuccess().getEncodedQR() ), response.getSuccess().getQrContent(),
                    response.getSuccess().getLtqrReference(), response.getSuccess().getPaymentOrderReference() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public List<LinkIDLTQRClientSession> pull(@Nullable final List<String> ltqrReferences, @Nullable final List<String> paymentOrderReferences,
                                              @Nullable final List<String> clientSessionIds)
            throws LinkIDPullException {

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
            throw new LinkIDPullException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            List<LinkIDLTQRClientSession> clientSessions = Lists.newLinkedList();

            for (ClientSession clientSession : response.getSuccess().getSessions()) {

                clientSessions.add(
                        new LinkIDLTQRClientSession( decodeQR( clientSession.getEncodedQR() ), clientSession.getQrContent(), clientSession.getLtqrReference(),
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
            throws LinkIDRemoveException {

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
            throw new LinkIDRemoveException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private LinkIDLTQRPaymentState convert(final LTQRPaymentStatusType wsPaymentStatusType) {

        if (null == wsPaymentStatusType) {
            return null;
        }

        switch (wsPaymentStatusType) {

            case STARTED:
                return LinkIDLTQRPaymentState.STARTED;
            case AUTHORIZED:
                return LinkIDLTQRPaymentState.PAYED;
            case FAILED:
                return LinkIDLTQRPaymentState.FAILED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected payment status type %s!", wsPaymentStatusType.name() ) );
    }

    private LinkIDErrorCode convert(final net.lin_k.safe_online.ltqr._2.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDErrorCode.ERROR_CREDENTIALS_INVALID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDChangeErrorCode convert(final net.lin_k.safe_online.ltqr._2.ChangeErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDChangeErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_NOT_FOUND:
                return LinkIDChangeErrorCode.ERROR_NOT_FOUND;
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
