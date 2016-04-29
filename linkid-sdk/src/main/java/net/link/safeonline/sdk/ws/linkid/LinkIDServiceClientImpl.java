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
import net.lin_k.linkid._3_1.core.ThemeAddRequest;
import net.lin_k.linkid._3_1.core.ThemeAddResponse;
import net.lin_k.linkid._3_1.core.ThemeConfig;
import net.lin_k.linkid._3_1.core.ThemeError;
import net.lin_k.linkid._3_1.core.ThemeRemoveRequest;
import net.lin_k.linkid._3_1.core.ThemeRemoveResponse;
import net.lin_k.linkid._3_1.core.ThemeStatusRequest;
import net.lin_k.linkid._3_1.core.ThemeStatusResponse;
import net.lin_k.linkid._3_1.core.Themes;
import net.lin_k.linkid._3_1.core.ThemesRequest;
import net.lin_k.linkid._3_1.core.ThemesResponse;
import net.lin_k.linkid._3_1.core.Voucher;
import net.lin_k.linkid._3_1.core.VoucherListRedeemedRequest;
import net.lin_k.linkid._3_1.core.VoucherListRedeemedResponse;
import net.lin_k.linkid._3_1.core.VoucherListRequest;
import net.lin_k.linkid._3_1.core.VoucherListResponse;
import net.lin_k.linkid._3_1.core.VoucherOrganizationAddUpdateRequest;
import net.lin_k.linkid._3_1.core.VoucherOrganizationAddUpdateResponse;
import net.lin_k.linkid._3_1.core.VoucherRedeemRequest;
import net.lin_k.linkid._3_1.core.VoucherRedeemResponse;
import net.lin_k.linkid._3_1.core.VoucherRewardRequest;
import net.lin_k.linkid._3_1.core.VoucherRewardResponse;
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
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTypeFilter;
import net.link.safeonline.sdk.api.themes.LinkIDThemeConfig;
import net.link.safeonline.sdk.api.themes.LinkIDThemeError;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatus;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatusCode;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucher;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganization;
import net.link.safeonline.sdk.api.voucher.LinkIDVouchers;
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
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeAddException;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeStatusException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListRedeemedException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationAddUpdateException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRedeemException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRewardException;
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
import net.link.safeonline.sdk.ws.LinkIDAbstractWSClient;
import net.link.safeonline.ws.linkid.LinkIDWSServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.common.DomUtils;
import net.link.util.saml.SamlUtils;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.apache.commons.collections.CollectionUtils;
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
public class LinkIDServiceClientImpl extends LinkIDAbstractWSClient<LinkIDServicePort> implements LinkIDServiceClient {

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

        super( location, LinkIDWSServiceFactory.newInstance().getLinkIDServicePort(), sslCertificates );

        // initialize SAML2
        LinkIDAuthnRequestFactory.bootstrapSaml2();
    }

    @Override
    protected String getLocationProperty() {

        return "linkid.ws.path";
    }

    @Override
    public LinkIDAuthSession authStart(final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthException {

        // request
        AuthStartRequest request = new AuthStartRequest();

        // input
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

        // request
        AuthPollRequest request = new AuthPollRequest();

        // input
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

            return new LinkIDAuthPollResponse( linkIDAuthenticationState, paymentState, linkIDAuthnResponse );
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public void authCancel(final String sessionId)
            throws LinkIDAuthCancelException {

        // request
        AuthCancelRequest request = new AuthCancelRequest();

        // input
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

        // request
        CallbackPullRequest request = new CallbackPullRequest();

        // input
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

        // request
        ConfigWalletApplicationsRequest request = new ConfigWalletApplicationsRequest();

        // input
        request.setWalletOrganizationId( walletOrganizationId );
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );

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
    public List<LinkIDLocalization> getLocalization(final List<String> keys)
            throws LinkIDLocalizationException {

        // request
        ConfigLocalizationRequest request = new ConfigLocalizationRequest();

        // input
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

        // request
        PaymentStatusRequest request = new PaymentStatusRequest();

        // input
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
                    transactions.add(
                            new LinkIDPaymentTransaction( paymentTransaction.getId(), LinkIDServiceUtils.convert( paymentTransaction.getPaymentMethodType() ),
                                    paymentTransaction.getPaymentMethod(), LinkIDServiceUtils.convert( paymentTransaction.getPaymentState() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getCreationDate() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getAuthorizationDate() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getCapturedDate() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getRefundedDate() ), paymentTransaction.getDocdataReference(),
                                    paymentTransaction.getAmount(), LinkIDServiceUtils.convert( paymentTransaction.getCurrency() ),
                                    paymentTransaction.getRefundAmount() ) );
                }

                List<LinkIDWalletTransaction> walletTransactions = Lists.newLinkedList();
                for (WalletTransaction walletTransaction : response.getSuccess().getPaymentDetails().getWalletTransactions()) {
                    walletTransactions.add( new LinkIDWalletTransaction( walletTransaction.getWalletId(), walletTransaction.getWalletOrganizationId(),
                            walletTransaction.getWalletOrganizationFriendly(), LinkIDServiceUtils.convert( walletTransaction.getCreationDate() ),
                            LinkIDServiceUtils.convert( walletTransaction.getRefundedDate() ),
                            LinkIDServiceUtils.convert( walletTransaction.getCommittedDate() ), walletTransaction.getTransactionId(),
                            walletTransaction.getAmount(), LinkIDServiceUtils.convert( walletTransaction.getCurrency() ), walletTransaction.getWalletCoin(),
                            walletTransaction.getRefundAmount(), response.getSuccess().getDescription() ) );
                }

                return new LinkIDPaymentStatus( response.getSuccess().getOrderReference(), response.getSuccess().getUserId(),
                        LinkIDServiceUtils.convert( response.getSuccess().getPaymentStatus() ), response.getSuccess().isAuthorized(),
                        response.getSuccess().isCaptured(), response.getSuccess().getAmountPayed(), response.getSuccess().getAmount(),
                        response.getSuccess().getRefundAmount(), LinkIDServiceUtils.convert( response.getSuccess().getCurrency() ),
                        response.getSuccess().getWalletCoin(), response.getSuccess().getDescription(), response.getSuccess().getProfile(),
                        LinkIDServiceUtils.convert( response.getSuccess().getCreated() ), response.getSuccess().getMandateReference(),
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

        // request
        PaymentCaptureRequest request = new PaymentCaptureRequest();

        // input
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

        // request
        PaymentRefundRequest request = new PaymentRefundRequest();

        // input
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

        // request
        MandatePaymentRequest request = new MandatePaymentRequest();

        // input
        request.setPaymentContext( LinkIDServiceUtils.convert( linkIDPaymentContext ) );
        request.setMandateReference( mandateReference );
        request.setNotificationLocation( notificationLocation );
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );

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

        // request
        LTQRPushRequest request = new LTQRPushRequest();

        // input
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

        // request
        LTQRBulkPushRequest request = new LTQRBulkPushRequest();

        // input
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

        // request
        LTQRChangeRequest request = new LTQRChangeRequest();

        // input
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

        // request
        LTQRPullRequest request = new LTQRPullRequest();

        // input
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

        // request
        LTQRRemoveRequest request = new LTQRRemoveRequest();

        // input
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

        // request
        LTQRInfoRequest request = new LTQRInfoRequest();

        // input
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
    public LinkIDPaymentReport paymentReport(@Nullable final LinkIDReportDateFilter dateFilter, @Nullable final List<String> orderReferences,
                                             @Nullable final List<String> mandateReferences, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        // request
        PaymentReportRequest request = new PaymentReportRequest();

        // input
        request.setDateFilter( LinkIDServiceUtils.convert( dateFilter ) );
        request.setPageFilter( LinkIDServiceUtils.convert( pageFilter ) );
        if (!CollectionUtils.isEmpty( orderReferences )) {
            request.getOrderReferences().addAll( orderReferences );
        }
        if (!CollectionUtils.isEmpty( mandateReferences )) {
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
                    transactions.add(
                            new LinkIDPaymentTransaction( paymentTransaction.getId(), LinkIDServiceUtils.convert( paymentTransaction.getPaymentMethodType() ),
                                    paymentTransaction.getPaymentMethod(), LinkIDServiceUtils.convert( paymentTransaction.getPaymentState() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getCreationDate() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getAuthorizationDate() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getCapturedDate() ),
                                    LinkIDServiceUtils.convert( paymentTransaction.getRefundedDate() ), paymentTransaction.getDocdataReference(),
                                    paymentTransaction.getAmount(), LinkIDServiceUtils.convert( paymentTransaction.getCurrency() ),
                                    paymentTransaction.getRefundAmount() ) );
                }

                // wallet transactions
                List<LinkIDWalletTransaction> walletTransactions = Lists.newLinkedList();
                for (WalletTransaction walletTransaction : paymentOrder.getWalletTransactions()) {
                    walletTransactions.add( new LinkIDWalletTransaction( walletTransaction.getWalletId(), walletTransaction.getWalletOrganizationId(),
                            walletTransaction.getWalletOrganizationFriendly(), LinkIDServiceUtils.convert( walletTransaction.getCreationDate() ),
                            LinkIDServiceUtils.convert( walletTransaction.getRefundedDate() ),
                            LinkIDServiceUtils.convert( walletTransaction.getCommittedDate() ), walletTransaction.getTransactionId(),
                            walletTransaction.getAmount(), LinkIDServiceUtils.convert( walletTransaction.getCurrency() ), walletTransaction.getWalletCoin(),
                            walletTransaction.getRefundAmount(), paymentOrder.getDescription() ) );
                }

                // order
                orders.add( new LinkIDPaymentOrder( paymentOrder.getProfile(), LinkIDServiceUtils.convert( paymentOrder.getDate() ), paymentOrder.getAmount(),
                        LinkIDServiceUtils.convert( paymentOrder.getCurrency() ), paymentOrder.getWalletCoin(), paymentOrder.getDescription(),
                        LinkIDServiceUtils.convert( paymentOrder.getPaymentState() ), paymentOrder.getAmountPayed(), paymentOrder.getAmountRefunded(),
                        paymentOrder.isAuthorized(), LinkIDServiceUtils.convert( paymentOrder.getAuthorizedDate() ), paymentOrder.isCaptured(),
                        LinkIDServiceUtils.convert( paymentOrder.getCapturedDate() ), paymentOrder.isRefunded(),
                        LinkIDServiceUtils.convert( paymentOrder.getRefundedDate() ), paymentOrder.getOrderReference(), paymentOrder.getUserId(),
                        paymentOrder.getEmail(), paymentOrder.getGivenName(), paymentOrder.getFamilyName(), transactions, walletTransactions ) );
            }

            return new LinkIDPaymentReport( response.getTotal(), orders );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    @Override
    public LinkIDParkingReport parkingReport(@Nullable final LinkIDReportDateFilter dateFilter, @Nullable final List<String> parkings,
                                             @Nullable final List<String> barCodes, @Nullable final List<String> ticketNumbers,
                                             @Nullable final List<String> dtaKeys, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        // request
        ParkingReportRequest request = new ParkingReportRequest();

        // input
        request.setDateFilter( LinkIDServiceUtils.convert( dateFilter ) );
        request.setPageFilter( LinkIDServiceUtils.convert( pageFilter ) );
        if (!CollectionUtils.isEmpty( barCodes )) {
            request.getBarCodes().addAll( barCodes );
        }
        if (!CollectionUtils.isEmpty( ticketNumbers )) {
            request.getTicketNumbers().addAll( ticketNumbers );
        }
        if (!CollectionUtils.isEmpty( dtaKeys )) {
            request.getDtaKeys().addAll( dtaKeys );
        }
        if (!CollectionUtils.isEmpty( parkings )) {
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

    @Override
    public LinkIDWalletReport walletReport(@Nullable final Locale locale, final String walletOrganizationId,
                                           @Nullable final LinkIDReportApplicationFilter applicationFilter,
                                           @Nullable final LinkIDReportWalletFilter walletFilter, @Nullable LinkIDWalletReportTypeFilter walletReportTypeFilter,
                                           @Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        // request
        WalletReportRequest request = new WalletReportRequest();

        // input
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );
        request.setWalletOrganizationId( walletOrganizationId );
        request.setDateFilter( LinkIDServiceUtils.convert( dateFilter ) );
        request.setPageFilter( LinkIDServiceUtils.convert( pageFilter ) );
        request.setApplicationFilter( LinkIDServiceUtils.convert( applicationFilter ) );
        request.setWalletFilter( LinkIDServiceUtils.convert( walletFilter ) );
        request.setWalletReportTypeFilter( LinkIDServiceUtils.convert( walletReportTypeFilter ) );

        // operate
        try {
            WalletReportResponse response = getPort().walletReport( request );

            if (null != response.getError()) {
                throw new LinkIDReportException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDWalletReportTransaction> transactions = Lists.newLinkedList();

            for (WalletReportTransaction walletReportTransaction : response.getTransactions()) {

                transactions.add( new LinkIDWalletReportTransaction( walletReportTransaction.getId(), walletReportTransaction.getWalletId(),
                        walletReportTransaction.getWalletOrganizationId(), walletReportTransaction.getWalletOrganizationFriendly(),
                        LinkIDServiceUtils.convert( walletReportTransaction.getCreationDate() ),
                        LinkIDServiceUtils.convert( walletReportTransaction.getRefundedDate() ),
                        LinkIDServiceUtils.convert( walletReportTransaction.getCommittedDate() ), walletReportTransaction.getTransactionId(),
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
    public List<LinkIDWalletInfoReport> walletInfoReport(@Nullable final Locale locale, final List<String> walletIds)
            throws LinkIDWSClientTransportException, LinkIDWalletInfoReportException {

        // request
        WalletInfoReportRequest request = new WalletInfoReportRequest();

        // input
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );
        request.getWalletId().addAll( walletIds );

        // operate
        try {

            WalletInfoReportResponse response = getPort().walletInfoReport( request );

            if (null != response.getError()) {
                throw new LinkIDWalletInfoReportException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDWalletInfoReport> result = Lists.newLinkedList();
            for (WalletInfoReport walletInfo : response.getWalletInfo()) {
                result.add( new LinkIDWalletInfoReport( walletInfo.getWalletId(), LinkIDServiceUtils.convert( walletInfo.getCreated() ),
                        LinkIDServiceUtils.convert( walletInfo.getRemoved() ), walletInfo.getUserId(), walletInfo.getOrganizationId(),
                        walletInfo.getOrganization(), walletInfo.getBalance() ) );
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

    @Override
    public void voucherReward(final String userId, final String voucherOrganizationId, final long points)
            throws LinkIDVoucherRewardException {

        // request
        VoucherRewardRequest request = new VoucherRewardRequest();

        // input
        request.setUserId( userId );
        request.setVoucherOrganizationId( voucherOrganizationId );
        request.setPoints( points );

        // operate
        VoucherRewardResponse response = getPort().voucherReward( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDVoucherRewardException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            // all good <o/
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDVouchers voucherList(final String userId, final String voucherOrganizationId, final Locale locale)
            throws LinkIDVoucherListException {

        // request
        VoucherListRequest request = new VoucherListRequest();

        // input
        request.setUserId( userId );
        request.setVoucherOrganizationId( voucherOrganizationId );
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );

        // operate
        VoucherListResponse response = getPort().voucherList( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDVoucherListException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            List<LinkIDVoucher> vouchers = Lists.newLinkedList();
            for (Voucher voucher : response.getSuccess().getVouchers()) {
                vouchers.add( LinkIDServiceUtils.convert( voucher ) );
            }
            return new LinkIDVouchers( vouchers, response.getSuccess().getTotal() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDVouchers voucherListRedeemed(final String userId, final String voucherOrganizationId, final Locale locale,
                                              @Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDVoucherListRedeemedException {

        // request
        VoucherListRedeemedRequest request = new VoucherListRedeemedRequest();

        // input
        request.setUserId( userId );
        request.setVoucherOrganizationId( voucherOrganizationId );
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );
        request.setDateFilter( LinkIDServiceUtils.convert( dateFilter ) );
        request.setPageFilter( LinkIDServiceUtils.convert( pageFilter ) );

        // operate
        VoucherListRedeemedResponse response = getPort().voucherListRedeemed( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDVoucherListRedeemedException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            List<LinkIDVoucher> vouchers = Lists.newLinkedList();
            for (Voucher voucher : response.getSuccess().getVouchers()) {
                vouchers.add( LinkIDServiceUtils.convert( voucher ) );
            }
            return new LinkIDVouchers( vouchers, response.getSuccess().getTotal() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void voucherRedeem(final String voucherId)
            throws LinkIDVoucherRedeemException {

        // request
        VoucherRedeemRequest request = new VoucherRedeemRequest();

        // input
        request.setVoucherId( voucherId );

        // operate
        VoucherRedeemResponse response = getPort().voucherRedeem( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDVoucherRedeemException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            // all good <o/
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public String voucherOrganizationAddUpdate(final LinkIDVoucherOrganization voucherOrganization)
            throws LinkIDVoucherOrganizationAddUpdateException {

        // request
        VoucherOrganizationAddUpdateRequest request = new VoucherOrganizationAddUpdateRequest();

        // input
        request.setVoucherOrganizationId( voucherOrganization.getId() );
        request.setLogoUrl( voucherOrganization.getLogoUrl() );
        request.setVoucherLimit( voucherOrganization.getVoucherLimit() );

        // operate
        VoucherOrganizationAddUpdateResponse response = getPort().voucherOrganizationAddUpdate( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                throw new LinkIDVoucherOrganizationAddUpdateException( response.getError().getErrorMessage(),
                        LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {
            return response.getSuccess().getName();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public String themeAdd(final LinkIDThemeConfig themeConfig)
            throws LinkIDThemeAddException {

        ThemeAddRequest request = new ThemeAddRequest();

        ThemeConfig wsThemeConfig = new ThemeConfig();
        wsThemeConfig.setName( themeConfig.getName() );
        wsThemeConfig.setFriendlyName( themeConfig.getFriendlyName() );
        wsThemeConfig.setDefaultTheme( themeConfig.isDefaultTheme() );
        wsThemeConfig.setLogo( LinkIDServiceUtils.convert( themeConfig.getLogos() ) );
        wsThemeConfig.setAuthLogo( LinkIDServiceUtils.convert( themeConfig.getAuthLogos() ) );
        wsThemeConfig.setBackground( LinkIDServiceUtils.convert( themeConfig.getBackgrounds() ) );
        wsThemeConfig.setTabletBackground( LinkIDServiceUtils.convert( themeConfig.getTabletBackgrounds() ) );
        wsThemeConfig.setAlternativeBackground( LinkIDServiceUtils.convert( themeConfig.getAlternativeBackgrounds() ) );
        wsThemeConfig.setBackgroundColor( themeConfig.getBackgroundColor() );
        wsThemeConfig.setTextColor( themeConfig.getTextColor() );
        request.setConfig( wsThemeConfig );

        // operate
        ThemeAddResponse response = getPort().themeAdd( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                throw new LinkIDThemeAddException( response.getError().getErrorMessage(), LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            } else if (null != response.getError().getError()) {

                ThemeError themeError = response.getError().getError();

                throw new LinkIDThemeAddException( new LinkIDThemeError( LinkIDServiceUtils.convert( themeError.getBackgroundColorError() ),
                        LinkIDServiceUtils.convert( themeError.getTextColorError() ) ) );

            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {
            return response.getSuccess().getName();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

    }

    @Override
    public void themeRemove(final String themeName, final boolean removeReleased)
            throws LinkIDThemeRemoveException {

        ThemeRemoveRequest request = new ThemeRemoveRequest();
        request.setName( themeName );
        request.setRemoveReleased( removeReleased );

        // operate
        ThemeRemoveResponse response = getPort().themeRemove( request );

        // convert response
        if (null != response.getError()) {

            throw new LinkIDThemeRemoveException( response.getError().getErrorMessage(), LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

    }

    @Override
    public LinkIDThemeStatus themeStatus(final String themeName)
            throws LinkIDThemeStatusException {

        ThemeStatusRequest request = new ThemeStatusRequest();
        request.setName( themeName );

        // operate
        ThemeStatusResponse response = getPort().themeStatus( request );

        // convert response
        if (null != response.getError()) {

            throw new LinkIDThemeStatusException( response.getError().getErrorMessage(), LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return new LinkIDThemeStatus( LinkIDServiceUtils.convert( response.getSuccess().getStatusCode() ), response.getSuccess().getInfoMessage(),
                    LinkIDServiceUtils.convert( response.getSuccess().getErrorReport() ) );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

    }

    @Override
    public LinkIDThemes themes(@Nullable final String themeName, @Nullable final LinkIDThemeStatusCode linkIDThemeStatusCode)
            throws LinkIDThemesException {

        ThemesRequest request = new ThemesRequest();
        request.setName( themeName );
        request.setStatusCode( LinkIDConversionUtils.convert( linkIDThemeStatusCode ) );

        // operate
        ThemesResponse response = getPort().themes( request );

        if (null != response.getError()) {
            throw new LinkIDThemesException( LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        // all good...
        List<LinkIDTheme> linkIDThemes = Lists.newLinkedList();
        for (Themes themes : response.getSuccess().getThemes()) {

            linkIDThemes.add( LinkIDServiceUtils.convert( themes ) );
        }

        return new LinkIDThemes( linkIDThemes );
    }

}
