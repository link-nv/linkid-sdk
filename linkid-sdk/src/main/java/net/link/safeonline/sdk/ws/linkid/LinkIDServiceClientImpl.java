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
import net.lin_k.linkid._3_1.core.*;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.common.LinkIDApplicationFilter;
import net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode;
import net.link.safeonline.sdk.api.common.LinkIDUserAttributeFilter;
import net.link.safeonline.sdk.api.common.LinkIDUserFilter;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentTransaction;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import net.link.safeonline.sdk.api.paymentconfiguration.LinkIDPaymentConfiguration;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissionType;
import net.link.safeonline.sdk.api.reporting.LinkIDParkingReport;
import net.link.safeonline.sdk.api.reporting.LinkIDPaymentReport;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportException;
import net.link.safeonline.sdk.api.reporting.LinkIDReportPageFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletInfoReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTransaction;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTypeFilter;
import net.link.safeonline.sdk.api.themes.LinkIDThemeConfig;
import net.link.safeonline.sdk.api.themes.LinkIDThemeError;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatus;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucher;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherEventTypeFilter;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistory;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistoryEvent;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganization;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationDetails;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationUsers;
import net.link.safeonline.sdk.api.voucher.LinkIDVouchers;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganization;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationDetails;
import net.link.safeonline.sdk.api.ws.callback.LinkIDCallbackPullException;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthCancelException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthenticationState;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplication;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplicationDetails;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDConfigWalletApplicationsException;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemes;
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
import net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration.LinkIDPaymentConfigurationAddException;
import net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration.LinkIDPaymentConfigurationRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration.LinkIDPaymentConfigurationUpdateException;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionAddException;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionListException;
import net.link.safeonline.sdk.api.ws.linkid.permissions.LinkIDApplicationPermissionRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeAddException;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.themes.LinkIDThemeStatusException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListRedeemedException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationActivateException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationAddUpdateException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationHistoryException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationListUsersException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRedeemException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherRewardException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletAddCreditException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletCommitException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletEnrollException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletGetInfoException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationAddException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletOrganizationUpdateException;
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
            LinkIDAuthenticationState linkIDAuthenticationState = LinkIDServiceUtils.convert( response.getSuccess().getAuthenticationState() );

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
    public List<LinkIDLocalization> getLocalization(final List<String> keys) {

        // request
        ConfigLocalizationRequest request = new ConfigLocalizationRequest();

        // input
        request.getKey().addAll( keys );

        // operate
        ConfigLocalizationResponse response = getPort().configLocalization( request );

        if (null != response.getError()) {
            LinkIDServiceUtils.convert( response.getError().getErrorCode() );
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
    public List<LinkIDApplicationDetails> configApplications(final List<String> applicationNames, final Locale locale) {

        // request
        ConfigApplicationsRequest request = new ConfigApplicationsRequest();

        // input
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );
        request.getNames().addAll( applicationNames );

        // operate
        ConfigApplicationsResponse response = getPort().configApplications( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                LinkIDServiceUtils.convert( response.getError().getErrorCode() );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {

            List<LinkIDApplicationDetails> applications = Lists.newLinkedList();

            for (ApplicationDetails applicationDetails : response.getSuccess().getApplications()) {
                applications.add(
                        new LinkIDApplicationDetails( applicationDetails.getName(), applicationDetails.getFriendlyName(), applicationDetails.getDescription(),
                                applicationDetails.getApplicationURL(), applicationDetails.getLogo() ) );
            }

            return applications;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

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
    public List<LinkIDWalletInfoReport> walletInfoReport(@Nullable final Locale locale, final List<String> walletIds) {

        // request
        WalletInfoReportRequest request = new WalletInfoReportRequest();

        // input
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );
        request.getWalletId().addAll( walletIds );

        // operate
        try {

            WalletInfoReportResponse response = getPort().walletInfoReport( request );

            if (null != response.getError()) {
                LinkIDServiceUtils.convert( response.getError().getErrorCode() );
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
    public String walletOrganizationAdd(final LinkIDWalletOrganization walletOrganization)
            throws LinkIDWalletOrganizationAddException {

        // request
        WalletOrganizationAddRequest request = new WalletOrganizationAddRequest();

        // input
        request.setOrganization( LinkIDServiceUtils.convert( walletOrganization ) );

        // operate
        WalletOrganizationAddResponse response = getPort().walletOrganizationAdd( request );

        // response
        if (null != response.getError()) {

            LinkIDServiceUtils.handle( response.getError() );

        } else if (null != response.getSuccess()) {

            // all good <o/
            return response.getSuccess().getName();

        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public String walletOrganizationUpdate(final LinkIDWalletOrganization walletOrganization)
            throws LinkIDWalletOrganizationUpdateException {

        // request
        WalletOrganizationUpdateRequest request = new WalletOrganizationUpdateRequest();

        // input
        request.setOrganization( LinkIDServiceUtils.convert( walletOrganization ) );

        // operate
        WalletOrganizationUpdateResponse response = getPort().walletOrganizationUpdate( request );

        // response
        if (null != response.getError()) {

            LinkIDServiceUtils.handle( response.getError() );

        } else if (null != response.getSuccess()) {

            // all good <o/
            return response.getSuccess().getName();

        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public List<LinkIDWalletOrganizationDetails> walletOrganizationList(@Nullable final List<String> walletOrganizationIds,
                                                                        @Nullable final LinkIDRequestStatusCode requestStatusCode, final boolean includeStats,
                                                                        @Nullable final Locale locale) {

        // request
        WalletOrganizationListRequest request = new WalletOrganizationListRequest();

        // input
        request.setStatusCode( LinkIDServiceUtils.convert( requestStatusCode ) );
        if (null != walletOrganizationIds) {
            request.getOrganizationIds().addAll( walletOrganizationIds );
        }
        request.setIncludeStats( includeStats );
        request.setLanguage( LinkIDServiceUtils.convert( locale ) );

        // operate
        WalletOrganizationListResponse response = getPort().walletOrganizationList( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                LinkIDServiceUtils.convert( response.getError().getErrorCode() );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {

            List<LinkIDWalletOrganizationDetails> organizations = Lists.newLinkedList();

            for (WalletOrganizationDetails details : response.getSuccess().getOrganizationDetails()) {
                organizations.add( LinkIDServiceUtils.convert( details ) );
            }

            return organizations;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void walletOrganizationRemove(final String walletOrganizationId, final boolean removeReleased)
            throws LinkIDWalletOrganizationRemoveException {

        // request
        WalletOrganizationRemoveRequest request = new WalletOrganizationRemoveRequest();

        // input
        request.setWalletOrganizationId( walletOrganizationId );
        request.setRemoveReleased( removeReleased );

        // operate
        WalletOrganizationRemoveResponse response = getPort().walletOrganizationRemove( request );

        // convert response
        if (null != response.getError()) {
            LinkIDServiceUtils.handle( response.getError() );

        } else if (null != response.getSuccess()) {
            // do nothing
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
    public LinkIDVouchers voucherList(final String userId, @Nullable final String voucherOrganizationId, boolean includeInactive, final Locale locale)
            throws LinkIDVoucherListException {

        // request
        VoucherListRequest request = new VoucherListRequest();

        // input
        request.setUserId( userId );
        request.setVoucherOrganizationId( voucherOrganizationId );
        request.setIncludeInactive( includeInactive );
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
        VoucherOrganization organization = new VoucherOrganization();
        request.setOrganization( organization );
        organization.setVoucherOrganizationId( voucherOrganization.getId() );
        organization.setLogoUrl( voucherOrganization.getLogoUrl() );
        organization.setVoucherLimit( voucherOrganization.getVoucherLimit() );
        organization.setActive( voucherOrganization.isActive() );

        if (!CollectionUtils.isEmpty( voucherOrganization.getNameLocalizations() )) {
            for (LinkIDLocalizationValue nameValue : voucherOrganization.getNameLocalizations()) {
                Localization localization = new Localization();
                localization.setLanguageCode( nameValue.getLanguageCode() );
                localization.setValue( nameValue.getValue() );
                organization.getNameLocalization().add( localization );
            }
        }

        if (!CollectionUtils.isEmpty( voucherOrganization.getDescriptionLocalizations() )) {
            for (LinkIDLocalizationValue nameValue : voucherOrganization.getDescriptionLocalizations()) {
                Localization localization = new Localization();
                localization.setLanguageCode( nameValue.getLanguageCode() );
                localization.setValue( nameValue.getValue() );
                organization.getDescriptionLocalization().add( localization );
            }
        }

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
    public List<LinkIDVoucherOrganizationDetails> voucherOrganizationList(@Nullable final List<String> voucherOrganizationIds, final boolean includeStats) {

        // request
        VoucherOrganizationListRequest request = new VoucherOrganizationListRequest();
        if (null != voucherOrganizationIds) {
            request.getOrganizationIds().addAll( voucherOrganizationIds );
        }
        request.setIncludeStats( includeStats );

        // operate
        VoucherOrganizationListResponse response = getPort().voucherOrganizationList( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                LinkIDServiceUtils.convert( response.getError().getErrorCode() );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {

            List<LinkIDVoucherOrganizationDetails> organizations = Lists.newLinkedList();

            for (VoucherOrganizationDetails voucherOrganizationDetails : response.getSuccess().getOrganizationDetails()) {
                organizations.add( LinkIDServiceUtils.convert( voucherOrganizationDetails ) );
            }

            return organizations;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDVoucherOrganizationUsers voucherOrganizationListUsers(final String voucherOrganizationId,
                                                                       @Nullable final LinkIDUserAttributeFilter userAttributeFilter,
                                                                       @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDVoucherOrganizationListUsersException {

        // request
        VoucherOrganizationListUsersRequest request = new VoucherOrganizationListUsersRequest();
        request.setOrganizationId( voucherOrganizationId );
        request.setUserAttributeFilter( LinkIDServiceUtils.convert( userAttributeFilter ) );
        request.setPageFilter( LinkIDServiceUtils.convert( pageFilter ) );

        // operate
        VoucherOrganizationListUsersResponse response = getPort().voucherOrganizationListUsers( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                throw new LinkIDVoucherOrganizationListUsersException( response.getError().getErrorMessage(),
                        LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {
            return new LinkIDVoucherOrganizationUsers( response.getSuccess().getUserIds(), response.getSuccess().getTotal() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void voucherOrganizationRemove(final String voucherOrganizationId)
            throws LinkIDVoucherOrganizationRemoveException {

        // request
        VoucherOrganizationRemoveRequest request = new VoucherOrganizationRemoveRequest();

        // input
        request.setVoucherOrganizationId( voucherOrganizationId );

        // operate
        VoucherOrganizationRemoveResponse response = getPort().voucherOrganizationRemove( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                throw new LinkIDVoucherOrganizationRemoveException( response.getError().getErrorMessage(),
                        LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void voucherOrganizationActivate(final String voucherOrganizationId, final boolean active)
            throws LinkIDVoucherOrganizationActivateException {

        // request
        VoucherOrganizationActivateRequest request = new VoucherOrganizationActivateRequest();

        // input
        request.setVoucherOrganizationId( voucherOrganizationId );
        request.setActive( active );

        // operate
        VoucherOrganizationActivateResponse response = getPort().voucherOrganizationActivate( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                throw new LinkIDVoucherOrganizationActivateException( response.getError().getErrorMessage(),
                        LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDVoucherHistory voucherOrganizationHistory(final String voucherOrganizationId, @Nullable final LinkIDVoucherEventTypeFilter eventTypeFilter,
                                                           @Nullable final LinkIDUserFilter userFilter,
                                                           @Nullable final LinkIDApplicationFilter applicationFilter,
                                                           @Nullable final LinkIDReportDateFilter dateFilter, @Nullable final LinkIDReportPageFilter pageFilter,
                                                           @Nullable final String language)
            throws LinkIDVoucherOrganizationHistoryException {

        // request
        VoucherOrganizationHistoryRequest request = new VoucherOrganizationHistoryRequest();

        // input
        request.setVoucherOrganizationId( voucherOrganizationId );
        request.setEventTypeFilter( LinkIDServiceUtils.convert( eventTypeFilter ) );
        request.setUserFilter( LinkIDServiceUtils.convert( userFilter ) );
        request.setApplicationFilter( LinkIDServiceUtils.convert( applicationFilter ) );
        request.setDateFilter( LinkIDServiceUtils.convert( dateFilter ) );
        request.setPageFilter( LinkIDServiceUtils.convert( pageFilter ) );
        request.setLanguage( language );

        // operate
        VoucherOrganizationHistoryResponse response = getPort().voucherOrganizationHistory( request );

        // convert response
        if (null != response.getError()) {

            if (null != response.getError().getErrorCode()) {
                throw new LinkIDVoucherOrganizationHistoryException( response.getError().getErrorMessage(),
                        LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
            } else {
                throw new InternalInconsistencyException( "No error nor error code element in the response error ?!" );
            }
        }

        if (null != response.getSuccess()) {

            List<LinkIDVoucherHistoryEvent> events = Lists.newLinkedList();
            for (VoucherHistoryEvent wsEvent : response.getSuccess().getEvents()) {
                events.add( LinkIDServiceUtils.convert( wsEvent ) );
            }

            return new LinkIDVoucherHistory( events, response.getSuccess().getTotal() );
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
        wsThemeConfig.setAuthLogo( LinkIDServiceUtils.convert( themeConfig.getLogos() ) );
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
                LinkIDServiceUtils.convert( response.getError().getErrorCode() );
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
    public LinkIDThemes themeList(@Nullable final String themeName, @Nullable final LinkIDRequestStatusCode requestStatusCode) {

        ThemesRequest request = new ThemesRequest();
        request.setName( themeName );
        request.setStatusCode( LinkIDServiceUtils.convertOld( requestStatusCode ) );

        // operate
        ThemesResponse response = getPort().themes( request );

        if (null != response.getError()) {

            LinkIDServiceUtils.convert( response.getError().getErrorCode() );

        }

        // all good...
        List<LinkIDTheme> linkIDThemes = Lists.newLinkedList();
        for (Themes themes : response.getSuccess().getThemes()) {

            linkIDThemes.add( LinkIDServiceUtils.convert( themes ) );
        }

        return new LinkIDThemes( linkIDThemes );

    }

    @Override
    public String paymentConfigurationAdd(final LinkIDPaymentConfiguration paymentConfiguration)
            throws LinkIDPaymentConfigurationAddException {

        // Setup
        PaymentConfigurationAddRequest request = new PaymentConfigurationAddRequest();
        request.setConfiguration( LinkIDServiceUtils.convert( paymentConfiguration ) );

        // Operate
        PaymentConfigurationAddResponse response = getPort().paymentConfigurationAdd( request );

        // Response
        if (null != response.getError()) {

            throw new LinkIDPaymentConfigurationAddException( response.getError().getErrorMessage(),
                    LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getName();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public String paymentConfigurationUpdate(final LinkIDPaymentConfiguration paymentConfiguration)
            throws LinkIDPaymentConfigurationUpdateException {

        // Setup
        PaymentConfigurationUpdateRequest request = new PaymentConfigurationUpdateRequest();
        request.setConfiguration( LinkIDServiceUtils.convert( paymentConfiguration ) );

        // Operate
        PaymentConfigurationUpdateResponse response = getPort().paymentConfigurationUpdate( request );

        // Response
        if (null != response.getError()) {

            throw new LinkIDPaymentConfigurationUpdateException( response.getError().getErrorMessage(),
                    LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getName();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void paymentConfigurationRemove(final String name)
            throws LinkIDPaymentConfigurationRemoveException {

        // Setup
        PaymentConfigurationRemoveRequest request = new PaymentConfigurationRemoveRequest();
        request.setName( name );

        // Operate
        PaymentConfigurationRemoveResponse response = getPort().paymentConfigurationRemove( request );

        // Response
        if (null != response.getError()) {

            throw new LinkIDPaymentConfigurationRemoveException( response.getError().getErrorMessage(),
                    LinkIDServiceUtils.convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public List<LinkIDPaymentConfiguration> paymentConfigurationList() {

        // Setup
        PaymentConfigurationListRequest request = new PaymentConfigurationListRequest();

        // Operate
        PaymentConfigurationListResponse response = getPort().paymentConfigurationList( request );

        // Response
        if (null != response.getError()) {

            LinkIDServiceUtils.convert( response.getError().getErrorCode() );

        } else if (null != response.getSuccess()) {

            List<LinkIDPaymentConfiguration> configurations = Lists.newLinkedList();
            for (PaymentConfiguration paymentConfiguration : response.getSuccess().getConfigurations()) {
                configurations.add( LinkIDServiceUtils.convert( paymentConfiguration ) );
            }
            return configurations;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void applicationPermissionAdd(final String id, final String applicationName, final LinkIDApplicationPermissionType permissionType)
            throws LinkIDApplicationPermissionAddException {

        // request
        ApplicationPermissionAddRequest request = new ApplicationPermissionAddRequest();

        // input
        request.setId( id );
        request.setApplicationName( applicationName );
        request.setPermissionType( LinkIDServiceUtils.convert( permissionType ) );

        // operate
        ApplicationPermissionAddResponse response = getPort().applicationPermissionAdd( request );

        // convert response
        if (null != response.getError()) {
            LinkIDServiceUtils.handle( response.getError() );
        }

        if (null != response.getSuccess()) {
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

    }

    @Override
    public void applicationPermissionRemove(final String id, @Nullable final String applicationName, final LinkIDApplicationPermissionType permissionType)
            throws LinkIDApplicationPermissionRemoveException {

        // request
        ApplicationPermissionRemoveRequest request = new ApplicationPermissionRemoveRequest();

        // input
        request.setId( id );
        request.setApplicationName( applicationName );
        request.setPermissionType( LinkIDServiceUtils.convert( permissionType ) );

        // operate
        ApplicationPermissionRemoveResponse response = getPort().applicationPermissionRemove( request );

        // convert response
        if (null != response.getError()) {
            LinkIDServiceUtils.handle( response.getError() );
        }

        if (null != response.getSuccess()) {
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public List<LinkIDApplicationPermissionType> applicationPermissionList(final String id)
            throws LinkIDApplicationPermissionListException {

        // request
        ApplicationPermissionListRequest request = new ApplicationPermissionListRequest();

        // input
        request.setId( id );

        // operate
        ApplicationPermissionListResponse response = getPort().applicationPermissionList( request );

        // convert response
        if (null != response.getError()) {
            LinkIDServiceUtils.handle( response.getError() );
        }

        if (null != response.getSuccess()) {

            List<LinkIDApplicationPermissionType> permissions = Lists.newLinkedList();

            for (ApplicationPermissionType permissionType : response.getSuccess().getPermissions()) {
                permissions.add( LinkIDServiceUtils.convert( permissionType ) );
            }

            return permissions;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );

    }

}
