/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.ltqr;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.common.Callback;
import net.lin_k.safe_online.common.PaymentContext;
import net.lin_k.safe_online.common.PaymentContextV20;
import net.lin_k.safe_online.ltqr._5.ChangeRequest;
import net.lin_k.safe_online.ltqr._5.ChangeResponse;
import net.lin_k.safe_online.ltqr._5.ClientSession;
import net.lin_k.safe_online.ltqr._5.InfoRequest;
import net.lin_k.safe_online.ltqr._5.InfoResponse;
import net.lin_k.safe_online.ltqr._5.LTQRInfo;
import net.lin_k.safe_online.ltqr._5.LTQRPaymentStatusType;
import net.lin_k.safe_online.ltqr._5.LTQRServicePort;
import net.lin_k.safe_online.ltqr._5.PollingConfiguration;
import net.lin_k.safe_online.ltqr._5.PullRequest;
import net.lin_k.safe_online.ltqr._5.PullResponse;
import net.lin_k.safe_online.ltqr._5.PushRequest;
import net.lin_k.safe_online.ltqr._5.PushResponse;
import net.lin_k.safe_online.ltqr._5.RemoveRequest;
import net.lin_k.safe_online.ltqr._5.RemoveResponse;
import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.ltqr.LinkIDChangeErrorCode;
import net.link.safeonline.sdk.api.ltqr.LinkIDChangeException;
import net.link.safeonline.sdk.api.ltqr.LinkIDErrorCode;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRClientSession;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRInfo;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRInfoException;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRPaymentState;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRPollingConfiguration;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ltqr.LinkIDPullException;
import net.link.safeonline.sdk.api.ltqr.LinkIDPushException;
import net.link.safeonline.sdk.api.ltqr.LinkIDRemoveException;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentAmount;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMandate;
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
                                  @Nullable final List<String> identityProfiles, @Nullable final Long sessionExpiryOverride, @Nullable final String theme,
                                  @Nullable final String mobileLandingSuccess, @Nullable final String mobileLandingError,
                                  @Nullable final String mobileLandingCancel, @Nullable final LinkIDLTQRPollingConfiguration pollingConfiguration,
                                  boolean waitForUnlock)
            throws LinkIDPushException {

        PushRequest request = new PushRequest();

        // custom msgs
        request.setAuthenticationMessage( authenticationMessage );
        request.setFinishedMessage( finishedMessage );

        // payment context
        request.setPaymentContext( convert( linkIDPaymentContext ) );

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

        if (null != mobileLandingSuccess) {
            request.setMobileLandingSuccess( mobileLandingSuccess );
        }
        if (null != mobileLandingError) {
            request.setMobileLandingError( mobileLandingError );
        }
        if (null != mobileLandingCancel) {
            request.setMobileLandingCancel( mobileLandingCancel );
        }

        // polling configuration
        request.setPollingConfiguration( convert( pollingConfiguration ) );

        // configuration
        request.setOneTimeUse( oneTimeUse );
        if (null != expiryDate) {
            request.setExpiryDate( LinkIDSDKUtils.convert( expiryDate ) );
        }
        request.setExpiryDuration( expiryDuration );
        request.setWaitForUnlock( waitForUnlock );

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
                                    @Nullable final List<String> identityProfiles, @Nullable final Long sessionExpiryOverride, @Nullable final String theme,
                                    @Nullable final String mobileLandingSuccess, @Nullable final String mobileLandingError,
                                    @Nullable final String mobileLandingCancel, final boolean resetUsed,
                                    @Nullable final LinkIDLTQRPollingConfiguration pollingConfiguration, boolean waitForUnlock, boolean unlock)
            throws LinkIDChangeException {

        ChangeRequest request = new ChangeRequest();

        request.setLtqrReference( ltqrReference );

        // custom msgs
        request.setAuthenticationMessage( authenticationMessage );
        request.setFinishedMessage( finishedMessage );

        // payment context
        request.setPaymentContext( convert( linkIDPaymentContext ) );

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

        if (null != mobileLandingSuccess) {
            request.setMobileLandingSuccess( mobileLandingSuccess );
        }
        if (null != mobileLandingError) {
            request.setMobileLandingError( mobileLandingError );
        }
        if (null != mobileLandingCancel) {
            request.setMobileLandingCancel( mobileLandingCancel );
        }

        // polling configuration
        request.setPollingConfiguration( convert( pollingConfiguration ) );

        // configuration
        if (null != expiryDate) {
            request.setExpiryDate( LinkIDSDKUtils.convert( expiryDate ) );
        }
        if (null != expiryDuration) {
            request.setExpiryDuration( expiryDuration );
        }
        request.setResetUsed( resetUsed );
        request.setWaitForUnlock( waitForUnlock );
        request.setUnlock( unlock );

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

    @Override
    public List<LinkIDLTQRInfo> info(final List<String> ltqrReferences)
            throws LinkIDLTQRInfoException {

        InfoRequest request = new InfoRequest();

        if (null == ltqrReferences || ltqrReferences.isEmpty()) {
            throw new InternalInconsistencyException( "No LTQR references to fetch information for!" );
        }

        request.getLtqrReferences().addAll( ltqrReferences );

        // operate
        InfoResponse response = getPort().info( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDLTQRInfoException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            List<LinkIDLTQRInfo> infos = Lists.newLinkedList();
            for (LTQRInfo ltqrInfo : response.getSuccess().getResults()) {

                infos.add( new LinkIDLTQRInfo( ltqrInfo.getLtqrReference(), ltqrInfo.getSessionId(), ltqrInfo.getCreated().toGregorianCalendar().getTime(),
                        decodeQR( ltqrInfo.getEncodedQR() ), ltqrInfo.getQrContent(), ltqrInfo.getAuthenticationMessage(), ltqrInfo.getFinishedMessage(),
                        ltqrInfo.isOneTimeUse(), null != ltqrInfo.getExpiryDate()? ltqrInfo.getExpiryDate().toGregorianCalendar().getTime(): null,
                        ltqrInfo.getExpiryDuration(), getPaymentContext( ltqrInfo.getPaymentContext() ), getCallback( ltqrInfo.getCallback() ),
                        getIdentityProfiles( ltqrInfo.getIdentityProfiles() ), ltqrInfo.getSessionExpiryOverride(), ltqrInfo.getTheme(),
                        ltqrInfo.getMobileLandingSuccess(), ltqrInfo.getMobileLandingError(), ltqrInfo.getMobileLandingCancel(),
                        LinkIDSDKUtils.getPollingConfiguration( ltqrInfo.getPollingConfiguration() ), ltqrInfo.isWaitForUnlock(), ltqrInfo.isLocked() ) );
            }
            return infos;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private Set<String> getIdentityProfiles(@Nullable final List<String> identityProfiles) {

        if (null == identityProfiles)
            return null;

        return Sets.newHashSet( identityProfiles );
    }

    private LinkIDCallback getCallback(@Nullable final Callback callback) {

        if (null == callback)
            return null;

        return new LinkIDCallback( callback.getLocation(), callback.getAppSessionId(), callback.isInApp() );
    }

    private LinkIDPaymentContext getPaymentContext(@Nullable final PaymentContext paymentContext) {

        if (null == paymentContext)
            return null;

        LinkIDPaymentContext.Builder builder = new LinkIDPaymentContext.Builder(
                new LinkIDPaymentAmount( paymentContext.getAmount(), LinkIDSDKUtils.convert( paymentContext.getCurrency() ),
                        paymentContext.getWalletCoin() ) ).description( paymentContext.getDescription() )
                                                          .orderReference( paymentContext.getOrderReference() )
                                                          .paymentProfile( paymentContext.getPaymentProfile() )
                                                          .paymentValidationTime( paymentContext.getValidationTime() );

        if (paymentContext.isMandate()) {
            builder = builder.mandate( new LinkIDPaymentMandate( paymentContext.getMandateDescription(), paymentContext.getMandateReference() ) );
        }

        return builder.build();
    }

    private PaymentContextV20 convert(@Nullable final LinkIDPaymentContext linkIDPaymentContext) {

        if (null == linkIDPaymentContext)
            return null;

        PaymentContextV20 paymentContext = new PaymentContextV20();
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
        paymentContext.setAllowPartial( linkIDPaymentContext.isAllowPartial() );
        paymentContext.setOnlyWallets( linkIDPaymentContext.isOnlyWallets() );
        paymentContext.setMandate( null != linkIDPaymentContext.getMandate() );
        if (null != linkIDPaymentContext.getMandate()) {
            paymentContext.setMandateDescription( linkIDPaymentContext.getMandate().getDescription() );
            paymentContext.setMandateReference( linkIDPaymentContext.getMandate().getReference() );
        }

        return paymentContext;
    }

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

    private LinkIDErrorCode convert(final net.lin_k.safe_online.ltqr._5.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_UNEXPECTED:
                return LinkIDErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDChangeErrorCode convert(final net.lin_k.safe_online.ltqr._5.ChangeErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CREDENTIALS_INVALID:
                return LinkIDChangeErrorCode.ERROR_CREDENTIALS_INVALID;
            case ERROR_NOT_FOUND:
                return LinkIDChangeErrorCode.ERROR_NOT_FOUND;
            case ERROR_UNEXPECTED:
                return LinkIDChangeErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDChangeErrorCode.ERROR_MAINTENANCE;
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

    @Nullable
    private PollingConfiguration convert(@Nullable final LinkIDLTQRPollingConfiguration pollingConfiguration) {

        if (null != pollingConfiguration) {
            PollingConfiguration wsPollingConfiguration = new PollingConfiguration();

            if (pollingConfiguration.getPollAttempts() > LinkIDConstants.LINKID_LTQR_POLLING_ATTEMPTS_MINIMUM) {
                wsPollingConfiguration.setPollAttempts( pollingConfiguration.getPollAttempts() );
            }
            if (pollingConfiguration.getPollInterval() > LinkIDConstants.LINKID_LTQR_POLLING_INTERVAL_MINIMUM) {
                wsPollingConfiguration.setPollInterval( pollingConfiguration.getPollInterval() );
            }
            if (pollingConfiguration.getPaymentPollAttempts() > LinkIDConstants.LINKID_LTQR_POLLING_ATTEMPTS_MINIMUM) {
                wsPollingConfiguration.setPaymentPollAttempts( pollingConfiguration.getPaymentPollAttempts() );
            }
            if (pollingConfiguration.getPaymentPollInterval() > LinkIDConstants.LINKID_LTQR_POLLING_INTERVAL_MINIMUM) {
                wsPollingConfiguration.setPaymentPollInterval( pollingConfiguration.getPaymentPollInterval() );
            }

            return wsPollingConfiguration;
        }

        return null;

    }
}
