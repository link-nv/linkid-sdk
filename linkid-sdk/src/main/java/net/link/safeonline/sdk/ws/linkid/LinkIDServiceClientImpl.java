/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.linkid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.xml.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import net.lin_k.linkid._3_1.core.AuthCancelRequest;
import net.lin_k.linkid._3_1.core.AuthCancelResponse;
import net.lin_k.linkid._3_1.core.AuthPollRequest;
import net.lin_k.linkid._3_1.core.AuthPollResponse;
import net.lin_k.linkid._3_1.core.AuthStartRequest;
import net.lin_k.linkid._3_1.core.AuthStartResponse;
import net.lin_k.linkid._3_1.core.CallbackPullRequest;
import net.lin_k.linkid._3_1.core.CallbackPullResponse;
import net.lin_k.linkid._3_1.core.ConfigLocalization;
import net.lin_k.linkid._3_1.core.ConfigLocalizationRequest;
import net.lin_k.linkid._3_1.core.ConfigLocalizationResponse;
import net.lin_k.linkid._3_1.core.ConfigLocalizationValue;
import net.lin_k.linkid._3_1.core.ConfigThemes;
import net.lin_k.linkid._3_1.core.ConfigThemesRequest;
import net.lin_k.linkid._3_1.core.ConfigThemesResponse;
import net.lin_k.linkid._3_1.core.ConfigWalletApplicationsRequest;
import net.lin_k.linkid._3_1.core.ConfigWalletApplicationsResponse;
import net.lin_k.linkid._3_1.core.LTQRBulkPushRequest;
import net.lin_k.linkid._3_1.core.LTQRBulkPushResponse;
import net.lin_k.linkid._3_1.core.LTQRChangeRequest;
import net.lin_k.linkid._3_1.core.LTQRChangeResponse;
import net.lin_k.linkid._3_1.core.LTQRClientSession;
import net.lin_k.linkid._3_1.core.LTQRInfo;
import net.lin_k.linkid._3_1.core.LTQRInfoRequest;
import net.lin_k.linkid._3_1.core.LTQRInfoResponse;
import net.lin_k.linkid._3_1.core.LTQRPullRequest;
import net.lin_k.linkid._3_1.core.LTQRPullResponse;
import net.lin_k.linkid._3_1.core.LTQRPushContent;
import net.lin_k.linkid._3_1.core.LTQRPushRequest;
import net.lin_k.linkid._3_1.core.LTQRPushResponse;
import net.lin_k.linkid._3_1.core.LTQRPushResponse2;
import net.lin_k.linkid._3_1.core.LTQRRemoveRequest;
import net.lin_k.linkid._3_1.core.LTQRRemoveResponse;
import net.lin_k.linkid._3_1.core.LinkIDServicePort;
import net.lin_k.linkid._3_1.core.MandatePaymentRequest;
import net.lin_k.linkid._3_1.core.MandatePaymentResponse;
import net.lin_k.linkid._3_1.core.ParkingReportRequest;
import net.lin_k.linkid._3_1.core.ParkingReportResponse;
import net.lin_k.linkid._3_1.core.ParkingSession;
import net.lin_k.linkid._3_1.core.PaymentCaptureRequest;
import net.lin_k.linkid._3_1.core.PaymentCaptureResponse;
import net.lin_k.linkid._3_1.core.PaymentOrder;
import net.lin_k.linkid._3_1.core.PaymentRefundRequest;
import net.lin_k.linkid._3_1.core.PaymentRefundResponse;
import net.lin_k.linkid._3_1.core.PaymentReportRequest;
import net.lin_k.linkid._3_1.core.PaymentReportResponse;
import net.lin_k.linkid._3_1.core.PaymentStatusRequest;
import net.lin_k.linkid._3_1.core.PaymentStatusResponse;
import net.lin_k.linkid._3_1.core.PaymentTransaction;
import net.lin_k.linkid._3_1.core.ReportApplicationFilter;
import net.lin_k.linkid._3_1.core.ReportDateFilter;
import net.lin_k.linkid._3_1.core.ReportPageFilter;
import net.lin_k.linkid._3_1.core.ReportWalletFilter;
import net.lin_k.linkid._3_1.core.WalletAddCreditRequest;
import net.lin_k.linkid._3_1.core.WalletAddCreditResponse;
import net.lin_k.linkid._3_1.core.WalletCommitRequest;
import net.lin_k.linkid._3_1.core.WalletCommitResponse;
import net.lin_k.linkid._3_1.core.WalletEnrollRequest;
import net.lin_k.linkid._3_1.core.WalletEnrollResponse;
import net.lin_k.linkid._3_1.core.WalletGetInfoRequest;
import net.lin_k.linkid._3_1.core.WalletGetInfoResponse;
import net.lin_k.linkid._3_1.core.WalletInfoReport;
import net.lin_k.linkid._3_1.core.WalletInfoReportRequest;
import net.lin_k.linkid._3_1.core.WalletInfoReportResponse;
import net.lin_k.linkid._3_1.core.WalletReleaseRequest;
import net.lin_k.linkid._3_1.core.WalletReleaseResponse;
import net.lin_k.linkid._3_1.core.WalletRemoveCreditRequest;
import net.lin_k.linkid._3_1.core.WalletRemoveCreditResponse;
import net.lin_k.linkid._3_1.core.WalletRemoveRequest;
import net.lin_k.linkid._3_1.core.WalletRemoveResponse;
import net.lin_k.linkid._3_1.core.WalletReportRequest;
import net.lin_k.linkid._3_1.core.WalletReportResponse;
import net.lin_k.linkid._3_1.core.WalletReportTransaction;
import net.lin_k.linkid._3_1.core.WalletTransaction;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentTransaction;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import net.link.safeonline.sdk.api.reporting.LinkIDParkingReport;
import net.link.safeonline.sdk.api.reporting.LinkIDPaymentReport;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportException;
import net.link.safeonline.sdk.api.reporting.LinkIDReportPageFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletInfoReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletInfoReportException;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTransaction;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.ws.callback.LinkIDCallbackPullException;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthCancelException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthenticationState;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplication;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDConfigWalletApplicationsException;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizationException;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemesException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRBulkPushException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRChangeException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRClientSession;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRInfo;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRInfoException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRLockType;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPullException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushResponse;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDMandatePaymentException;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentCaptureException;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentDetails;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentRefundException;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatusException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletCommitException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReleaseException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReportInfo;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDAuthnRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.linkid.LinkIDWSServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.common.DomUtils;
import net.link.util.saml.SamlUtils;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Element;


/**
 * Created by wvdhaute
 * Date: 29/01/14
 * Time: 15:47
 */
public class LinkIDServiceClientImpl extends AbstractWSClient<LinkIDServicePort> implements LinkIDServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                   final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDWSServiceFactory.newInstance().getLinkIDServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.path" ) ) );

        // initialize SAML2
        LinkIDAuthnRequestFactory.bootstrapSaml2();
    }

    @Override
    public LinkIDAuthSession authStart(final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthException {

        AuthStartRequest request = new AuthStartRequest();

        request.setAny( SamlUtils.marshall( LinkIDSaml2Utils.generate( authenticationContext ) ) );

        request.setLanguage( null != authenticationContext.getLanguage()? authenticationContext.getLanguage().getLanguage(): null );
        request.setUserAgent( userAgent );

        // operate
        AuthStartResponse response = getPort().authStart( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDAuthException( LinkIDServiceUtils.convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSuccess()) {

            return new LinkIDAuthSession( response.getSuccess().getSessionId(), LinkIDServiceUtils.convert( response.getSuccess().getQrCodeInfo() ) );
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public LinkIDAuthPollResponse authPoll(final String sessionId, final String language)
            throws LinkIDAuthPollException {

        AuthPollRequest request = new AuthPollRequest();

        request.setSessionId( sessionId );
        request.setLanguage( language );

        // operate
        AuthPollResponse response = getPort().authPoll( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDAuthPollException( LinkIDServiceUtils.convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSuccess()) {

            // authenticate state
            LinkIDAuthenticationState linkIDAuthenticationState = LinkIDConversionUtils.convert( response.getSuccess().getAuthenticationState() );

            LinkIDPaymentState paymentState = null;
            if (null != response.getSuccess().getPaymentState()) {
                paymentState = LinkIDServiceUtils.convert( response.getSuccess().getPaymentState() );
            }

            String paymentMenuURL = response.getSuccess().getPaymentMenuURL();

            // parse authentication request
            LinkIDAuthnResponse linkIDAuthnResponse = null;
            if (null != response.getSuccess().getAuthenticationResponse()) {
                Element authnResponseElement = (Element) response.getSuccess().getAuthenticationResponse().getAny();
                if (null != authnResponseElement) {
                    Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller( authnResponseElement );
                    if (null == unmarshaller) {
                        String responseString = DomUtils.domToString( authnResponseElement, true );
                        throw new InternalInconsistencyException(
                                String.format( "Failed to unmarshall SAML v2.0 authentication response?!: Element=\"%s\"", responseString ) );
                    }
                    try {
                        XMLObject xmlObject = unmarshaller.unmarshall( authnResponseElement );
                        linkIDAuthnResponse = LinkIDSaml2Utils.parse( (Response) xmlObject );
                    }
                    catch (UnmarshallingException e) {
                        throw new InternalInconsistencyException( "Failed to unmarshall SAML v2.0 authentication response?!", e );
                    }
                }
            }

            return new LinkIDAuthPollResponse( linkIDAuthenticationState, paymentState, paymentMenuURL, linkIDAuthnResponse );
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public void authCancel(final String sessionId)
            throws LinkIDAuthCancelException {

        AuthCancelRequest request = new AuthCancelRequest();
        request.setSessionId( sessionId );

        // operate
        AuthCancelResponse response = getPort().authCancel( request );

        if (null != response.getError()) {
            throw new LinkIDAuthCancelException( LinkIDServiceUtils.convert( response.getError().getError() ), response.getError().getInfo() );
        }
    }

    @Override
    public LinkIDAuthnResponse callbackPull(final String sessionId)
            throws LinkIDCallbackPullException {

        CallbackPullRequest request = new CallbackPullRequest();
        request.setSessionId( sessionId );

        // operate
        CallbackPullResponse response = getPort().callbackPull( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDCallbackPullException( LinkIDServiceUtils.convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSuccess()) {

            // parse authentication request
            Element authnResponseElement = (Element) response.getSuccess().getAny();
            if (null == authnResponseElement) {
                throw new InternalInconsistencyException( "No SAML v2.0 authentication response found ?!" );
            }

            Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller( authnResponseElement );
            LinkIDAuthnResponse linkIDAuthnResponse;
            if (null == unmarshaller) {
                String responseString = DomUtils.domToString( authnResponseElement, true );
                throw new InternalInconsistencyException(
                        String.format( "Failed to unmarshall SAML v2.0 authentication response?!: Element=\"%s\"", responseString ) );
            }
            try {
                XMLObject responseXMLObject = unmarshaller.unmarshall( authnResponseElement );
                linkIDAuthnResponse = LinkIDSaml2Utils.parse( (Response) responseXMLObject );
            }
            catch (UnmarshallingException e) {
                throw new InternalInconsistencyException( "Failed to unmarshall SAML v2.0 authentication response?!", e );
            }

            return linkIDAuthnResponse;
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );

    }

    @Override
    public List<LinkIDApplication> configWalletApplications(final String walletOrganizationId, final Locale locale)
            throws LinkIDConfigWalletApplicationsException {

        ConfigWalletApplicationsRequest request = new ConfigWalletApplicationsRequest();
        request.setWalletOrganizationId( walletOrganizationId );
        if (null != locale) {
            request.setLanguage( locale.getLanguage() );
        } else {
            request.setLanguage( Locale.ENGLISH.getLanguage() );
        }

        // operate
        ConfigWalletApplicationsResponse response = getPort().configWalletApplications( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDConfigWalletApplicationsException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            List<LinkIDApplication> applications = Lists.newLinkedList();
            for (net.lin_k.linkid._3_1.core.LinkIDApplication application : response.getSuccess().getApplications()) {
                applications.add( new LinkIDApplication( application.getName(), application.getFriendlyName() ) );
            }
            return applications;
        }

        throw new InternalInconsistencyException( "No succes nor error element in the response ?!" );
    }

    @Override
    public LinkIDThemes getThemes(final String applicationName)
            throws LinkIDThemesException {

        ConfigThemesRequest request = new ConfigThemesRequest();
        request.setApplicationName( applicationName );

        // operate
        ConfigThemesResponse response = getPort().configThemes( request );

        if (null != response.getError()) {
            throw new LinkIDThemesException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        // all good...
        List<LinkIDTheme> linkIDThemes = Lists.newLinkedList();
        for (ConfigThemes themes : response.getSuccess().getThemes()) {

            linkIDThemes.add( new LinkIDTheme( themes.getName(), themes.isDefaultTheme(), LinkIDServiceUtils.convert( themes.isOwner() ),
                    LinkIDServiceUtils.convert( themes.getLogo() ), LinkIDServiceUtils.convert( themes.getAuthLogo() ),
                    LinkIDServiceUtils.convert( themes.getBackground() ), LinkIDServiceUtils.convert( themes.getTabletBackground() ),
                    LinkIDServiceUtils.convert( themes.getAlternativeBackground() ), themes.getBackgroundColor(), themes.getTextColor() ) );
        }

        return new LinkIDThemes( linkIDThemes );
    }

    @Override
    public List<LinkIDLocalization> getLocalization(final List<String> keys)
            throws LinkIDLocalizationException {

        ConfigLocalizationRequest request = new ConfigLocalizationRequest();
        request.getKey().addAll( keys );

        // operate
        ConfigLocalizationResponse response = getPort().configLocalization( request );

        if (null != response.getError()) {
            throw new LinkIDLocalizationException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        // all good
        List<LinkIDLocalization> localizations = Lists.newLinkedList();
        for (ConfigLocalization localization : response.getSuccess().getLocalization()) {
            Map<String, String> values = Maps.newHashMap();
            for (ConfigLocalizationValue localizationValue : localization.getValues()) {
                values.put( localizationValue.getLanguageCode(), localizationValue.getLocalized() );
            }
            localizations.add( new LinkIDLocalization( localization.getKey(), LinkIDServiceUtils.convert( localization.getType() ), values ) );
        }
        return localizations;
    }

    @Override
    public LinkIDPaymentStatus getPaymentStatus(final String orderReference)
            throws LinkIDPaymentStatusException {

        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setOrderReference( orderReference );

        try {
            // operate
            PaymentStatusResponse response = getPort().paymentStatus( request );

            // convert response
            if (null != response.getError()) {
                throw new LinkIDPaymentStatusException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            }

            if (null != response.getSuccess()) {

                // parse
                List<LinkIDPaymentTransaction> transactions = Lists.newLinkedList();
                for (PaymentTransaction paymentTransaction : response.getSuccess().getPaymentDetails().getPaymentTransactions()) {
                    transactions.add( new LinkIDPaymentTransaction( LinkIDServiceUtils.convert( paymentTransaction.getPaymentMethodType() ),
                            paymentTransaction.getPaymentMethod(), LinkIDServiceUtils.convert( paymentTransaction.getPaymentState() ),
                            LinkIDSDKUtils.convert( paymentTransaction.getCreationDate() ), LinkIDSDKUtils.convert( paymentTransaction.getAuthorizationDate() ),
                            LinkIDSDKUtils.convert( paymentTransaction.getCapturedDate() ), paymentTransaction.getDocdataReference(),
                            paymentTransaction.getAmount(), LinkIDServiceUtils.convert( paymentTransaction.getCurrency() ),
                            paymentTransaction.getRefundAmount() ) );
                }

                List<LinkIDWalletTransaction> walletTransactions = Lists.newLinkedList();
                for (WalletTransaction walletTransaction : response.getSuccess().getPaymentDetails().getWalletTransactions()) {
                    walletTransactions.add(
                            new LinkIDWalletTransaction( walletTransaction.getWalletId(), LinkIDSDKUtils.convert( walletTransaction.getCreationDate() ),
                                    walletTransaction.getTransactionId(), walletTransaction.getAmount(),
                                    LinkIDServiceUtils.convert( walletTransaction.getCurrency() ), walletTransaction.getWalletCoin(),
                                    walletTransaction.getRefundAmount(), response.getSuccess().getDescription() ) );
                }

                return new LinkIDPaymentStatus( response.getSuccess().getOrderReference(), response.getSuccess().getUserId(),
                        LinkIDServiceUtils.convert( response.getSuccess().getPaymentStatus() ), response.getSuccess().isAuthorized(),
                        response.getSuccess().isCaptured(), response.getSuccess().getAmountPayed(), response.getSuccess().getAmount(),
                        response.getSuccess().getRefundAmount(), LinkIDServiceUtils.convert( response.getSuccess().getCurrency() ),
                        response.getSuccess().getWalletCoin(), response.getSuccess().getDescription(), response.getSuccess().getProfile(),
                        LinkIDSDKUtils.convert( response.getSuccess().getCreated() ), response.getSuccess().getMandateReference(),
                        new LinkIDPaymentDetails( transactions, walletTransactions ) );
            }

            throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    @Override
    public void paymentCapture(final String orderReference)
            throws LinkIDPaymentCaptureException {

        PaymentCaptureRequest request = new PaymentCaptureRequest();

        request.setOrderReference( orderReference );

        // operate
        PaymentCaptureResponse response = getPort().paymentCapture( request );

        if (null != response.getError()) {
            throw new LinkIDPaymentCaptureException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        // all good...
    }

    @Override
    public void paymentRefund(final String orderReference)
            throws LinkIDPaymentRefundException {

        PaymentRefundRequest request = new PaymentRefundRequest();

        request.setOrderReference( orderReference );

        // operate
        PaymentRefundResponse response = getPort().paymentRefund( request );

        if (null != response.getError()) {
            throw new LinkIDPaymentRefundException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        // all good...
    }

    @Override
    public String mandatePayment(final String mandateReference, final LinkIDPaymentContext linkIDPaymentContext, @Nullable final String notificationLocation,
                                 final Locale locale)
            throws LinkIDMandatePaymentException {

        MandatePaymentRequest request = new MandatePaymentRequest();

        request.setPaymentContext( LinkIDServiceUtils.convert( linkIDPaymentContext ) );
        request.setMandateReference( mandateReference );
        request.setNotificationLocation( notificationLocation );
        if (null != locale) {
            request.setLanguage( locale.getLanguage() );
        } else {
            request.setLanguage( Locale.ENGLISH.getLanguage() );
        }

        // operate
        MandatePaymentResponse response = getPort().mandatePayment( request );

        if (null != response.getError()) {
            throw new LinkIDMandatePaymentException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getOrderReference();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDLTQRSession ltqrPush(final LinkIDLTQRContent content, final String userAgent, final LinkIDLTQRLockType lockType)
            throws LinkIDLTQRPushException {

        LTQRPushRequest request = new LTQRPushRequest();

        request.setContent( LinkIDServiceUtils.convert( content ) );
        request.setUserAgent( userAgent );
        request.setLockType( LinkIDServiceUtils.convert( lockType ) );

        // operate
        LTQRPushResponse response = getPort().ltqrPush( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDLTQRPushException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ), response.getError().getErrorMessage() );
        }

        if (null != response.getSuccess()) {

            return new LinkIDLTQRSession( response.getSuccess().getLtqrReference(), LinkIDServiceUtils.convert( response.getSuccess().getQrCodeInfo() ),
                    response.getSuccess().getPaymentOrderReference() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

    }

    @Override
    public List<LinkIDLTQRPushResponse> ltqrBulkPush(final List<LinkIDLTQRPushContent> contents)
            throws LinkIDLTQRBulkPushException {

        LTQRBulkPushRequest request = new LTQRBulkPushRequest();

        for (LinkIDLTQRPushContent content : contents) {
            LTQRPushContent ltqrPushContent = new LTQRPushContent();
            ltqrPushContent.setContent( LinkIDServiceUtils.convert( content.getContent() ) );
            ltqrPushContent.setUserAgent( content.getUserAgent() );
            ltqrPushContent.setLockType( LinkIDServiceUtils.convert( content.getLockType() ) );
            request.getRequests().add( ltqrPushContent );
        }

        // operate
        LTQRBulkPushResponse response = getPort().ltqrBulkPush( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDLTQRBulkPushException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ), response.getError().getErrorMessage() );
        }

        if (null != response.getSuccess()) {

            List<LinkIDLTQRPushResponse> results = Lists.newLinkedList();
            for (LTQRPushResponse2 ltqrPushResponse : response.getSuccess().getResponses()) {
                if (null != ltqrPushResponse.getSuccess()) {
                    results.add( new LinkIDLTQRPushResponse( new LinkIDLTQRSession( ltqrPushResponse.getSuccess().getLtqrReference(),
                            LinkIDServiceUtils.convert( ltqrPushResponse.getSuccess().getQrCodeInfo() ),
                            ltqrPushResponse.getSuccess().getPaymentOrderReference() ) ) );
                } else {
                    results.add( new LinkIDLTQRPushResponse( LinkIDServiceUtils.convert( ltqrPushResponse.getError().getErrorCode() ),
                            ltqrPushResponse.getError().getErrorMessage() ) );
                }
            }
            return results;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDLTQRSession ltqrChange(final String ltqrReference, final LinkIDLTQRContent content, final String userAgent, final boolean unlock,
                                        final boolean unblock)
            throws LinkIDLTQRChangeException {

        LTQRChangeRequest request = new LTQRChangeRequest();

        request.setLtqrReference( ltqrReference );
        request.setContent( LinkIDServiceUtils.convert( content ) );
        request.setUserAgent( userAgent );
        request.setUnlock( unlock );
        request.setUnblock( unblock );

        // operate
        LTQRChangeResponse response = getPort().ltqrChange( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDLTQRChangeException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ), response.getError().getErrorMessage() );
        }

        if (null != response.getSuccess()) {
            return new LinkIDLTQRSession( response.getSuccess().getLtqrReference(), LinkIDServiceUtils.convert( response.getSuccess().getQrCodeInfo() ),
                    response.getSuccess().getPaymentOrderReference() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public List<LinkIDLTQRClientSession> ltqrPull(@Nullable final List<String> ltqrReferences, @Nullable final List<String> paymentOrderReferences,
                                                  @Nullable final List<String> clientSessionIds)
            throws LinkIDLTQRPullException {

        LTQRPullRequest request = new LTQRPullRequest();

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
        LTQRPullResponse response = getPort().ltqrPull( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDLTQRPullException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            List<LinkIDLTQRClientSession> clientSessions = Lists.newLinkedList();

            for (LTQRClientSession clientSession : response.getSuccess().getSessions()) {

                clientSessions.add( new LinkIDLTQRClientSession( clientSession.getLtqrReference(), LinkIDServiceUtils.convert( clientSession.getQrCodeInfo() ),
                        clientSession.getClientSessionId(), clientSession.getUserId(), clientSession.getCreated().toGregorianCalendar().getTime(),
                        LinkIDServiceUtils.convert( clientSession.getPaymentStatus() ), clientSession.getPaymentOrderReference() ) );
            }

            return clientSessions;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void ltqrRemove(@Nullable final List<String> ltqrReferences, @Nullable final List<String> paymentOrderReferences,
                           @Nullable final List<String> clientSessionIds)
            throws LinkIDLTQRRemoveException {

        LTQRRemoveRequest request = new LTQRRemoveRequest();

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
        LTQRRemoveResponse response = getPort().ltqrRemove( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDLTQRRemoveException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

    }

    @Override
    public List<LinkIDLTQRInfo> ltqrInfo(final List<String> ltqrReferences, final String userAgent)
            throws LinkIDLTQRInfoException {

        LTQRInfoRequest request = new LTQRInfoRequest();

        if (null == ltqrReferences || ltqrReferences.isEmpty()) {
            throw new InternalInconsistencyException( "No LTQR references to fetch information for!" );
        }

        request.getLtqrReferences().addAll( ltqrReferences );
        request.setUserAgent( userAgent );

        // operate
        LTQRInfoResponse response = getPort().ltqrInfo( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDLTQRInfoException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            List<LinkIDLTQRInfo> infos = Lists.newLinkedList();
            for (LTQRInfo ltqrInfo : response.getSuccess().getResults()) {

                infos.add( new LinkIDLTQRInfo( ltqrInfo.getLtqrReference(), ltqrInfo.getSessionId(), ltqrInfo.getCreated().toGregorianCalendar().getTime(),
                        LinkIDServiceUtils.convert( ltqrInfo.getQrCodeInfo() ), LinkIDServiceUtils.convert( ltqrInfo.getContent() ),
                        LinkIDServiceUtils.convert( ltqrInfo.getLockType() ), ltqrInfo.isLocked(), ltqrInfo.isWaitForUnblock(), ltqrInfo.isBlocked() ) );
            }
            return infos;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDPaymentReport getPaymentReport(@Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getPaymentReport( dateFilter, pageFilter, null, null );
    }

    @Override
    public LinkIDPaymentReport getPaymentReportForOrderReferences(final List<String> orderReferences, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getPaymentReport( null, pageFilter, orderReferences, null );
    }

    @Override
    public LinkIDPaymentReport getPaymentReportForMandates(final List<String> mandateReferences, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getPaymentReport( null, pageFilter, null, mandateReferences );
    }

    @Override
    public LinkIDParkingReport getParkingReport(@Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( dateFilter, pageFilter, null, null, null, null );
    }

    @Override
    public LinkIDParkingReport getParkingReport(@Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter,
                                                @Nullable final List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( dateFilter, pageFilter, null, null, null, parkings );
    }

    @Override
    public LinkIDParkingReport getParkingReportForBarCodes(final List<String> barCodes, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( null, pageFilter, barCodes, null, null, null );
    }

    @Override
    public LinkIDParkingReport getParkingReportForTicketNumbers(final List<String> ticketNumbers, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( null, pageFilter, null, ticketNumbers, null, null );
    }

    @Override
    public LinkIDParkingReport getParkingReportForDTAKeys(final List<String> dtaKeys, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( null, pageFilter, null, null, dtaKeys, null );
    }

    @Override
    public LinkIDWalletReport getWalletReport(@Nullable final Locale locale, final String walletOrganizationId,
                                              @Nullable final LinkIDReportApplicationFilter applicationFilter,
                                              @Nullable final LinkIDReportWalletFilter walletFilter, @Nullable final LinkIDReportDateFilter dateFilter,
                                              @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        WalletReportRequest request = new WalletReportRequest();

        if (null != locale) {
            request.setLanguage( locale.getLanguage() );
        }

        request.setWalletOrganizationId( walletOrganizationId );

        if (null != dateFilter) {
            ReportDateFilter wsDateFilter = new ReportDateFilter();
            wsDateFilter.setStartDate( LinkIDSDKUtils.convert( dateFilter.getStartDate() ) );
            if (null != dateFilter.getEndDate()) {
                wsDateFilter.setEndDate( LinkIDSDKUtils.convert( dateFilter.getEndDate() ) );
            }
            request.setDateFilter( wsDateFilter );
        }
        if (null != pageFilter) {
            ReportPageFilter wsPageFilter = new ReportPageFilter();
            wsPageFilter.setFirstResult( pageFilter.getFirstResult() );
            wsPageFilter.setMaxResults( pageFilter.getMaxResults() );
            request.setPageFilter( wsPageFilter );
        }
        if (null != applicationFilter) {
            ReportApplicationFilter wsApplicationFilter = new ReportApplicationFilter();
            wsApplicationFilter.setApplicationName( applicationFilter.getApplicationName() );
            request.setApplicationFilter( wsApplicationFilter );
        }
        if (null != walletFilter) {
            ReportWalletFilter wsWalletFilter = new ReportWalletFilter();
            wsWalletFilter.setWalletId( walletFilter.getWalletId() );
            request.setWalletFilter( wsWalletFilter );
        }

        try {
            WalletReportResponse response = getPort().walletReport( request );

            if (null != response.getError()) {
                throw new LinkIDReportException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDWalletReportTransaction> transactions = Lists.newLinkedList();

            for (WalletReportTransaction walletReportTransaction : response.getTransactions()) {

                transactions.add( new LinkIDWalletReportTransaction( walletReportTransaction.getWalletId(),
                        LinkIDSDKUtils.convert( walletReportTransaction.getCreationDate() ), walletReportTransaction.getTransactionId(),
                        walletReportTransaction.getAmount(), LinkIDServiceUtils.convert( walletReportTransaction.getCurrency() ),
                        walletReportTransaction.getWalletCoin(), walletReportTransaction.getRefundAmount(), walletReportTransaction.getPaymentDescription(),
                        walletReportTransaction.getUserId(), walletReportTransaction.getApplicationName(), walletReportTransaction.getApplicationFriendly(),
                        LinkIDServiceUtils.convert( walletReportTransaction.getType() ),
                        LinkIDServiceUtils.convert( walletReportTransaction.getReportInfo() ) ) );
            }

            return new LinkIDWalletReport( response.getTotal(), transactions );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    @Override
    public List<LinkIDWalletInfoReport> getWalletInfoReport(@Nullable final Locale locale, final List<String> walletIds)
            throws LinkIDWSClientTransportException, LinkIDWalletInfoReportException {

        WalletInfoReportRequest request = new WalletInfoReportRequest();

        if (null != locale) {
            request.setLanguage( locale.getLanguage() );
        }
        request.getWalletId().addAll( walletIds );

        try {

            WalletInfoReportResponse response = getPort().walletInfoReport( request );

            if (null != response.getError()) {
                throw new LinkIDWalletInfoReportException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDWalletInfoReport> result = Lists.newLinkedList();
            for (WalletInfoReport walletInfo : response.getWalletInfo()) {
                result.add( new LinkIDWalletInfoReport( walletInfo.getWalletId(), LinkIDSDKUtils.convert( walletInfo.getCreated() ),
                        LinkIDSDKUtils.convert( walletInfo.getRemoved() ), walletInfo.getUserId(), walletInfo.getOrganizationId(), walletInfo.getOrganization(),
                        walletInfo.getBalance() ) );
            }

            return result;

        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    @Override
    public String walletEnroll(final String userId, final String walletOrganizationId, final double amount, @Nullable final LinkIDCurrency currency,
                               @Nullable final String walletCoin, @Nullable final LinkIDWalletReportInfo reportInfo)
            throws LinkIDWalletEnrollException {

        //request
        WalletEnrollRequest request = new WalletEnrollRequest();

        // input
        request.setUserId( userId );
        request.setWalletOrganizationId( walletOrganizationId );
        request.setAmount( amount );
        request.setCurrency( LinkIDServiceUtils.convert( currency ) );
        request.setWalletCoin( walletCoin );
        request.setReportInfo( LinkIDServiceUtils.convert( reportInfo ) );

        // operate
        WalletEnrollResponse response = getPort().walletEnroll( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletEnrollException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getWalletId();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDWalletInfo walletGetInfo(final String userId, final String walletOrganizationId)
            throws LinkIDWalletGetInfoException {

        // request
        WalletGetInfoRequest request = new WalletGetInfoRequest();

        // input
        request.setUserId( userId );
        request.setWalletOrganizationId( walletOrganizationId );

        // operate
        WalletGetInfoResponse response = getPort().walletGetInfo( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletGetInfoException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            if (null == response.getSuccess().getWalletId()) {
                return null;
            }

            return new LinkIDWalletInfo( response.getSuccess().getWalletId() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void walletAddCredit(final String userId, final String walletId, final double amount, @Nullable final LinkIDCurrency currency,
                                @Nullable final String walletCoin, @Nullable final LinkIDWalletReportInfo reportInfo)
            throws LinkIDWalletAddCreditException {

        //request
        WalletAddCreditRequest request = new WalletAddCreditRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setAmount( amount );
        request.setCurrency( LinkIDServiceUtils.convert( currency ) );
        request.setWalletCoin( walletCoin );
        request.setReportInfo( LinkIDServiceUtils.convert( reportInfo ) );

        // operate
        WalletAddCreditResponse response = getPort().walletAddCredit( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletAddCreditException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void walletRemoveCredit(final String userId, final String walletId, final double amount, @Nullable final LinkIDCurrency currency,
                                   @Nullable final String walletCoin, @Nullable final LinkIDWalletReportInfo reportInfo)
            throws LinkIDWalletRemoveCreditException {

        //request
        WalletRemoveCreditRequest request = new WalletRemoveCreditRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setAmount( amount );
        request.setCurrency( LinkIDServiceUtils.convert( currency ) );
        request.setWalletCoin( walletCoin );
        request.setReportInfo( LinkIDServiceUtils.convert( reportInfo ) );

        // operate
        WalletRemoveCreditResponse response = getPort().walletRemoveCredit( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletRemoveCreditException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void walletRemove(final String userId, final String walletId)
            throws LinkIDWalletRemoveException {

        //request
        WalletRemoveRequest request = new WalletRemoveRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );

        // operate
        WalletRemoveResponse response = getPort().walletRemove( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDWalletRemoveException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void walletCommit(final String userId, final String walletId, final String walletTransactionId)
            throws LinkIDWalletCommitException {

        // request
        WalletCommitRequest request = new WalletCommitRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setWalletTransactionId( walletTransactionId );

        // operate
        WalletCommitResponse response = getPort().walletCommit( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDWalletCommitException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            // all good <o/
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void walletRelease(final String userId, final String walletId, final String walletTransactionId)
            throws LinkIDWalletReleaseException {

        // request
        WalletReleaseRequest request = new WalletReleaseRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setWalletTransactionId( walletTransactionId );

        // operate
        WalletReleaseResponse response = getPort().walletRelease( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDWalletReleaseException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            // all good <o/
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private LinkIDPaymentReport getPaymentReport(@Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter,
                                                 @Nullable final List<String> orderReferences, @Nullable final List<String> mandateReferences)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        PaymentReportRequest request = new PaymentReportRequest();

        if (null != dateFilter) {
            ReportDateFilter wsDateFilter = new ReportDateFilter();
            wsDateFilter.setStartDate( LinkIDSDKUtils.convert( dateFilter.getStartDate() ) );
            if (null != dateFilter.getEndDate()) {
                wsDateFilter.setEndDate( LinkIDSDKUtils.convert( dateFilter.getEndDate() ) );
            }
            request.setDateFilter( wsDateFilter );
        }
        if (null != pageFilter) {
            ReportPageFilter wsPageFilter = new ReportPageFilter();
            wsPageFilter.setFirstResult( pageFilter.getFirstResult() );
            wsPageFilter.setMaxResults( pageFilter.getMaxResults() );
            request.setPageFilter( wsPageFilter );
        }
        if (null != orderReferences) {
            request.getOrderReferences().addAll( orderReferences );
        }
        if (null != mandateReferences) {
            request.getMandateReferences().addAll( mandateReferences );
        }

        try {
            PaymentReportResponse response = getPort().paymentReport( request );

            if (null != response.getError()) {
                throw new LinkIDReportException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDPaymentOrder> orders = Lists.newLinkedList();
            for (PaymentOrder paymentOrder : response.getOrders()) {

                // payment transactions
                List<LinkIDPaymentTransaction> transactions = Lists.newLinkedList();
                for (PaymentTransaction paymentTransaction : paymentOrder.getTransactions()) {
                    transactions.add( new LinkIDPaymentTransaction( LinkIDServiceUtils.convert( paymentTransaction.getPaymentMethodType() ),
                            paymentTransaction.getPaymentMethod(), LinkIDServiceUtils.convert( paymentTransaction.getPaymentState() ),
                            LinkIDServiceUtils.convert( paymentTransaction.getCreationDate() ),
                            LinkIDServiceUtils.convert( paymentTransaction.getAuthorizationDate() ),
                            LinkIDServiceUtils.convert( paymentTransaction.getCapturedDate() ), paymentTransaction.getDocdataReference(),
                            paymentTransaction.getAmount(), LinkIDServiceUtils.convert( paymentTransaction.getCurrency() ),
                            paymentTransaction.getRefundAmount() ) );
                }

                // wallet transactions
                List<LinkIDWalletTransaction> walletTransactions = Lists.newLinkedList();
                for (WalletTransaction walletTransaction : paymentOrder.getWalletTransactions()) {
                    walletTransactions.add(
                            new LinkIDWalletTransaction( walletTransaction.getWalletId(), LinkIDServiceUtils.convert( walletTransaction.getCreationDate() ),
                                    walletTransaction.getTransactionId(), walletTransaction.getAmount(),
                                    LinkIDServiceUtils.convert( walletTransaction.getCurrency() ), walletTransaction.getWalletCoin(),
                                    walletTransaction.getRefundAmount(), paymentOrder.getDescription() ) );
                }

                // order
                orders.add( new LinkIDPaymentOrder( LinkIDServiceUtils.convert( paymentOrder.getDate() ), paymentOrder.getAmount(),
                        LinkIDServiceUtils.convert( paymentOrder.getCurrency() ), paymentOrder.getWalletCoin(), paymentOrder.getDescription(),
                        LinkIDServiceUtils.convert( paymentOrder.getPaymentState() ), paymentOrder.getAmountPayed(), paymentOrder.getAmountRefunded(),
                        paymentOrder.isAuthorized(), paymentOrder.isCaptured(), paymentOrder.getOrderReference(), paymentOrder.getUserId(),
                        paymentOrder.getEmail(), paymentOrder.getGivenName(), paymentOrder.getFamilyName(), transactions, walletTransactions ) );
            }

            return new LinkIDPaymentReport( response.getTotal(), orders );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    private LinkIDParkingReport getParkingReport(@Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter,
                                                 @Nullable final List<String> barCodes, @Nullable final List<String> ticketNumbers,
                                                 @Nullable final List<String> dtaKeys, @Nullable final List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        ParkingReportRequest request = new ParkingReportRequest();

        if (null != dateFilter) {
            ReportDateFilter wsDateFilter = new ReportDateFilter();
            wsDateFilter.setStartDate( LinkIDSDKUtils.convert( dateFilter.getStartDate() ) );
            if (null != dateFilter.getEndDate()) {
                wsDateFilter.setEndDate( LinkIDSDKUtils.convert( dateFilter.getEndDate() ) );
            }
            request.setDateFilter( wsDateFilter );
        }
        if (null != pageFilter) {
            ReportPageFilter wsPageFilter = new ReportPageFilter();
            wsPageFilter.setFirstResult( pageFilter.getFirstResult() );
            wsPageFilter.setMaxResults( pageFilter.getMaxResults() );
            request.setPageFilter( wsPageFilter );
        }
        if (null != barCodes) {
            request.getBarCodes().addAll( barCodes );
        }
        if (null != ticketNumbers) {
            request.getTicketNumbers().addAll( ticketNumbers );
        }
        if (null != dtaKeys) {
            request.getDtaKeys().addAll( dtaKeys );
        }
        if (null != parkings) {
            request.getParkings().addAll( parkings );
        }

        try {
            ParkingReportResponse response = getPort().parkingReport( request );

            if (null != response.getError()) {
                throw new LinkIDReportException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDParkingSession> sessions = Lists.newLinkedList();

            for (ParkingSession session : response.getSessions()) {
                sessions.add( new LinkIDParkingSession( session.getDate().toGregorianCalendar().getTime(), session.getBarCode(), session.getParking(),
                        session.getUserId(), session.getTurnover(), session.isValidated(), session.getPaymentOrderReference(),
                        LinkIDServiceUtils.convert( session.getPaymentState() ) ) );
            }

            return new LinkIDParkingReport( response.getTotal(), sessions );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

}
