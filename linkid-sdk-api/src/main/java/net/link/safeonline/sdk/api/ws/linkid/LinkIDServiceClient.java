/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid;

import java.util.List;
import java.util.Locale;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.common.LinkIDApplicationFilter;
import net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode;
import net.link.safeonline.sdk.api.common.LinkIDUserFilter;
import net.link.safeonline.sdk.api.credentials.LinkIDCredential;
import net.link.safeonline.sdk.api.credentials.LinkIDCredentialRequest;
import net.link.safeonline.sdk.api.credentials.LinkIDCredentialType;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;
import net.link.safeonline.sdk.api.notification.LinkIDNotificationLocation;
import net.link.safeonline.sdk.api.notification.LinkIDNotificationTopicConfiguration;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDMandateRemoveResult;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentInfo;
import net.link.safeonline.sdk.api.paymentconfiguration.LinkIDPaymentConfiguration;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissionType;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissions;
import net.link.safeonline.sdk.api.reporting.LinkIDParkingReport;
import net.link.safeonline.sdk.api.reporting.LinkIDPaymentReport;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportException;
import net.link.safeonline.sdk.api.reporting.LinkIDReportPageFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletInfoReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReport;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTypeFilter;
import net.link.safeonline.sdk.api.themes.LinkIDThemeConfig;
import net.link.safeonline.sdk.api.themes.LinkIDThemeStatus;
import net.link.safeonline.sdk.api.users.LinkIDUsers;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucher;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherEventTypeFilter;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherHistory;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganization;
import net.link.safeonline.sdk.api.voucher.LinkIDVoucherOrganizationDetails;
import net.link.safeonline.sdk.api.voucher.LinkIDVouchers;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganization;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationDetails;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationResult;
import net.link.safeonline.sdk.api.ws.callback.LinkIDCallbackPullException;
import net.link.safeonline.sdk.api.ws.linkid.applications.LinkIDApplicationAddException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthCancelException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.comments.LinkIDCommentGetException;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplication;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDApplicationDetails;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDConfigWalletApplicationsException;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.linkid.credentials.LinkIDCredentialRemoveException;
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
import net.link.safeonline.sdk.api.ws.linkid.notifications.LinkIDNotificationAddException;
import net.link.safeonline.sdk.api.ws.linkid.notifications.LinkIDNotificationRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.notifications.LinkIDNotificationUpdateException;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDMandatePaymentException;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentCaptureException;
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
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDUserListException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherInfoException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherListRedeemedException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationActivateException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationAddUpdateException;
import net.link.safeonline.sdk.api.ws.linkid.voucher.LinkIDVoucherOrganizationHistoryException;
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
import org.jetbrains.annotations.Nullable;


/**
 * linkID WS client.
 * <p/>
 */
@SuppressWarnings("unused")
public interface LinkIDServiceClient {

    /**
     * Start a linkID authentication.
     *
     * @param authenticationContext the linkID authentication context
     * @param userAgent             optional user agent string, for adding e.g. callback params to the QR code URL, android chrome URL needs to be
     *                              http://linkidmauthurl/MAUTH/2/zUC8oA/eA==, ...
     *
     * @return LinkIDAuthnSession the session details, e.g. ID,  QR code, ...
     *
     * @throws LinkIDAuthException something went wrong, check the error code and info message
     */
    LinkIDAuthSession authStart(LinkIDAuthenticationContext authenticationContext, String userAgent)
            throws LinkIDAuthException;

    /**
     * Poll the linkID authentication
     *
     * @param sessionId the sessionId of the authentication
     * @param language  optional language (default is en)
     *
     * @return poll response containing the state of the authentication.
     *
     * @throws LinkIDAuthPollException something went wrong, check the error code and info message
     */
    LinkIDAuthPollResponse authPoll(String sessionId, String language)
            throws LinkIDAuthPollException;

    /**
     * Cancel an existing linkID authentication session
     *
     * @param sessionId the sessionId of the authentication
     *
     * @throws LinkIDAuthCancelException something went wrong, check the error code and info message
     */
    void authCancel(String sessionId)
            throws LinkIDAuthCancelException;

    /**
     * Fetch a linkID callback authentication response
     *
     * @param sessionId the sessionId of the authentication response
     *
     * @return the authentication response
     *
     * @throws LinkIDCallbackPullException something went wrong, check the error code and info message
     */
    LinkIDAuthnResponse callbackPull(String sessionId)
            throws LinkIDCallbackPullException;

    /**
     * Fetch the list of linkID applications allowed to use specified wallet organization
     *
     * @param walletOrganizationId the wallet organization ID
     * @param locale               locale to return the application friendly name in
     *
     * @return the list of applications
     *
     * @throws LinkIDConfigWalletApplicationsException something went wrong, check the error code and info message
     */
    List<LinkIDApplication> configWalletApplications(String walletOrganizationId, Locale locale)
            throws LinkIDConfigWalletApplicationsException;

    /**
     * Fetch the specified keys's localization in linkID.
     * <p/>
     * e.g. for getting the wallet organization ID's localization, wallet coin ID
     *
     * @param keys the keys to fetch localization for
     *
     * @return the localizations
     */
    List<LinkIDLocalization> getLocalization(List<String> keys);

    /**
     * Fetch application details for the specified list of technical application names. If not found, it's not in the result list.
     *
     * @param applicationNames the application technical names
     * @param locale           the locale
     *
     * @return the application details
     */
    List<LinkIDApplicationDetails> configApplications(List<String> applicationNames, Locale locale);

    /**
     * Get payment info, e.g. what wallet organizations you can use, what payment methods you can use
     *
     * @return the payment info
     */
    LinkIDPaymentInfo paymentInfo(Locale locale);

    /**
     * Fetch the payment status of specified order
     *
     * @param orderReference the order reference of the payment order
     *
     * @return the payment status details
     *
     * @throws LinkIDPaymentStatusException failure
     */
    LinkIDPaymentStatus getPaymentStatus(String orderReference)
            throws LinkIDPaymentStatusException;

    /**
     * Capture a payment
     *
     * @throws LinkIDPaymentCaptureException something went wrong, check the error code in the exception
     */
    void paymentCapture(String orderReference)
            throws LinkIDPaymentCaptureException;

    /**
     * Refund a payment
     *
     * @throws LinkIDPaymentRefundException something went wrong, check the error code in the exception
     */
    void paymentRefund(String orderReference)
            throws LinkIDPaymentRefundException;

    /**
     * Make a payment for specified mandate
     *
     * @return the order reference for this payment
     *
     * @throws LinkIDMandatePaymentException something went wrong, check the error code in the exception
     */
    String mandatePayment(String mandateReference, LinkIDPaymentContext paymentContext, @Nullable String notificationLocation, Locale locale)
            throws LinkIDMandatePaymentException;

    /**
     * Remove specified mandates
     *
     * @param mandateReferences the mandate references to remove
     * @param noEmail           don't send users an e-mail
     *
     * @return result containing 3 lists, the successfully removed, not found ones and already archived ones
     */
    LinkIDMandateRemoveResult mandateRemove(List<String> mandateReferences, boolean noEmail);

    /**
     * Push a long term QR session to linkID.
     *
     * @param content   Configuration of this LTQR
     * @param userAgent optional user agent case you want to get the QR code URL in the correct format
     * @param lockType  lock type of the LTQR, check the enum for more info
     *
     * @return Success object containing the QR in PNG format, the content of the QR code and a type 4 UUID session ID of the created long term session. This
     * session ID will be used in the notifications to the Service Provider.
     *
     * @throws LinkIDLTQRPushException failure
     */
    LinkIDLTQRSession ltqrPush(LinkIDLTQRContent content, String userAgent, LinkIDLTQRLockType lockType)
            throws LinkIDLTQRPushException;

    /**
     * Bulk push long term QR sessions to linkID
     *
     * @param contents the LTQR request contents
     *
     * @return list of response for the LTQR requests
     *
     * @throws LinkIDLTQRBulkPushException failure, check error code
     */
    List<LinkIDLTQRPushResponse> ltqrBulkPush(List<LinkIDLTQRPushContent> contents)
            throws LinkIDLTQRBulkPushException;

    /**
     * Change an existing long term QR code
     *
     * @param ltqrReference LTQR reference, mandatory
     * @param content       Configuration of this LTQR
     * @param userAgent     Optional user agent case you want to get the QR code URL in the correct format
     * @param unlock        Unlocks the LTQR code that has been locked depending on the lockType
     * @param unblock       Unblocks the LTQR code if waitForUnblock was set true. This will allow the users that were waiting to continue the QR session.
     *
     * @return Success object containing the QR in PNG format, the content of the QR code and a type 4 UUID session ID of the created long term session. This
     */
    LinkIDLTQRSession ltqrChange(String ltqrReference, LinkIDLTQRContent content, String userAgent, boolean unlock, boolean unblock)
            throws LinkIDLTQRChangeException;

    /**
     * Fetch a set of client sessions.
     *
     * @param ltqrReferences         Optional list of LTQR References to fetch. If none are specified, all LTQR sessions and client session are returned.
     * @param paymentOrderReferences Optional list of Payment order References to fetch. If none are specified, all are fetched for the LTQR References
     *                               specified above.
     * @param clientSessionIds       optional list of client session IDs
     *
     * @return list of client sessions
     *
     * @throws LinkIDLTQRPullException failure
     */
    List<LinkIDLTQRClientSession> ltqrPull(@Nullable List<String> ltqrReferences, @Nullable List<String> paymentOrderReferences,
                                           @Nullable List<String> clientSessionIds)
            throws LinkIDLTQRPullException;

    /**
     * Remove a set of client sessions.
     *
     * @param ltqrReferences         Optional list of LTQR References to remove. If none are specified, all LTQR sessions and client session are removed.
     * @param paymentOrderReferences Optional list of Payment order References to remove. If none are specified, all are removed for the LTQR References
     *                               specified above.
     * @param clientSessionIds       optional list of client session IDs to remove
     *
     * @throws LinkIDLTQRRemoveException failure
     */
    void ltqrRemove(@Nullable List<String> ltqrReferences, @Nullable List<String> paymentOrderReferences, @Nullable List<String> clientSessionIds)
            throws LinkIDLTQRRemoveException;

    /**
     * Fetch info for the specified LTQR references
     *
     * @param ltqrReferences the list of LTQR references to fetch info for
     * @param userAgent      optional user agent case you want to get the QR code URL in the correct format
     *
     * @return the LTQR info objects
     *
     * @throws LinkIDLTQRInfoException failure
     */
    List<LinkIDLTQRInfo> ltqrInfo(List<String> ltqrReferences, String userAgent)
            throws LinkIDLTQRInfoException;

    /**
     * @param dateFilter        optional date filter
     * @param orderReferences   optional order references
     * @param mandateReferences optional mandate references
     * @param pageFilter        optional page filter
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     */
    LinkIDPaymentReport paymentReport(@Nullable LinkIDReportDateFilter dateFilter, @Nullable List<String> orderReferences,
                                      @Nullable List<String> mandateReferences, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDReportException;

    /**
     * @param dateFilter    optional date filter
     * @param parkings      optional list of parkings
     * @param barCodes      optional list of bar codes
     * @param ticketNumbers optional list of ticket numbers
     * @param dtaKeys       optional list of DTA keys
     * @param pageFilter    optional page filter
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     */
    LinkIDParkingReport parkingReport(@Nullable LinkIDReportDateFilter dateFilter, @Nullable List<String> parkings, @Nullable List<String> barCodes,
                                      @Nullable List<String> ticketNumbers, @Nullable List<String> dtaKeys, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDReportException;

    /**
     * @param locale                 optional locale, if not specified will default to en
     * @param applicationFilter      application filter
     * @param walletFilter           wallet filter
     * @param walletReportTypeFilter optional wallet report type filter
     * @param dateFilter             date filter
     * @param pageFilter             optional page filter
     *
     * @return the wallet transactions matching your search. If none found and empty list is returned
     */
    LinkIDWalletReport walletReport(@Nullable Locale locale, String walletOrganizationId, @Nullable LinkIDUserFilter userFilter,
                                    @Nullable LinkIDReportApplicationFilter applicationFilter, @Nullable LinkIDReportWalletFilter walletFilter,
                                    @Nullable LinkIDWalletReportTypeFilter walletReportTypeFilter, @Nullable LinkIDReportDateFilter dateFilter,
                                    @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDReportException;

    /**
     * @param locale    optional locale, if not specified will default to en
     * @param walletIds the list of walletIds to get info about
     *
     * @return list of wallet report info objects for the specified walletIds. If a walletId was not found it will be skipped
     */
    List<LinkIDWalletInfoReport> walletInfoReport(@Nullable Locale locale, List<String> walletIds);

    /**
     * Enroll users for a wallet. Optionally specify initial credit to add to wallet if applicable
     *
     * @param reportInfo optional wallet report info
     *
     * @return walletId the enrolled wallet ID
     *
     * @throws LinkIDWalletEnrollException something went wrong, check the error code in the exception
     */
    String walletEnroll(String userId, String walletOrganizationId, double amount, @Nullable LinkIDCurrency currency, @Nullable String walletCoin,
                        @Nullable LinkIDWalletReportInfo reportInfo)
            throws LinkIDWalletEnrollException;

    /**
     * Get info about a wallet for specified user and wallet organization
     *
     * @param userId               the userId
     * @param walletOrganizationId the wallet organization ID
     *
     * @return wallet info or null if no such wallet for that user
     *
     * @throws LinkIDWalletGetInfoException the wallet does not exist, user does not exist, ... check the error code
     */
    @Nullable
    LinkIDWalletInfo walletGetInfo(String userId, String walletOrganizationId)
            throws LinkIDWalletGetInfoException;

    /**
     * Add credit for a user for a wallet
     *
     * @throws LinkIDWalletAddCreditException something went wrong, check the error code in the exception
     */
    void walletAddCredit(String userId, String walletId, double amount, @Nullable LinkIDCurrency currency, @Nullable String walletCoin,
                         @Nullable LinkIDWalletReportInfo reportInfo)
            throws LinkIDWalletAddCreditException;

    /**
     * Remove credit for a user for a wallet.
     * If the amount is > than the credit on their wallet or amount==-1, their wallet credit will be set to 0
     *
     * @throws LinkIDWalletRemoveCreditException something went wrong, check the error code in the exception
     */
    void walletRemoveCredit(String userId, String walletId, double amount, @Nullable LinkIDCurrency currency, @Nullable String walletCoin,
                            @Nullable LinkIDWalletReportInfo reportInfo)
            throws LinkIDWalletRemoveCreditException;

    /**
     * Remove the specified wallet from that user
     *
     * @throws LinkIDWalletRemoveException something went wrong, check the error code in the exception
     */
    void walletRemove(String userId, String walletId)
            throws LinkIDWalletRemoveException;

    /**
     * Commit a wallet transaction. The amount payed by the specified wallet transaction ID will be free'd.
     * If not committed, linkID will after a period of time release it.
     *
     * @throws LinkIDWalletCommitException something went wrong, check the error code in the exception
     */
    void walletCommit(String userId, String walletId, String walletTransactionId)
            throws LinkIDWalletCommitException;

    /**
     * Release a wallet transaction immediately instead of waiting for the wallet's expiration.
     *
     * @throws LinkIDWalletReleaseException something went wrong, check the error code in the exception
     */
    void walletRelease(String userId, String walletId, String walletTransactionId)
            throws LinkIDWalletReleaseException;

    /**
     * Request a new wallet organization
     *
     * @param walletOrganization the wallet organization configuration
     *
     * @return the technical name(s) of the wallet organization (coin) to be added
     *
     * @throws LinkIDWalletOrganizationAddException something went wrong, check the error code in the exception
     */
    LinkIDWalletOrganizationResult walletOrganizationAdd(final LinkIDWalletOrganization walletOrganization)
            throws LinkIDWalletOrganizationAddException;

    /**
     * Update aa new wallet organization request
     *
     * @param walletOrganization the wallet organization configuration
     *
     * @return the technical name(s) of the wallet organization (coin) to be added
     *
     * @throws LinkIDWalletOrganizationUpdateException something went wrong, check the error code in the exception
     */
    LinkIDWalletOrganizationResult walletOrganizationUpdate(final LinkIDWalletOrganization walletOrganization)
            throws LinkIDWalletOrganizationUpdateException;

    /**
     * Returns the list of wallet organizations the caller application owns
     *
     * @param walletOrganizationIds optional list of wallet organization IDs
     * @param requestStatusCode     optional status code for filtering
     * @param includeStats          include stats?
     * @param locale                optional language (default is en)
     *
     * @return the list of owned wallet organizations
     */
    List<LinkIDWalletOrganizationDetails> walletOrganizationList(@Nullable List<String> walletOrganizationIds,
                                                                 @Nullable LinkIDRequestStatusCode requestStatusCode, boolean includeStats,
                                                                 @Nullable Locale locale);

    /**
     * Request to remove a linkID wallet organization
     *
     * @param walletOrganizationId the wallet organization ID
     * @param removeReleased       is the wallet organization a pending wallet organization or an already released one
     *
     * @throws LinkIDWalletOrganizationRemoveException something went wrong, check the error code and info message
     */
    void walletOrganizationRemove(String walletOrganizationId, boolean removeReleased)
            throws LinkIDWalletOrganizationRemoveException;

    /**
     * Add points for specified user and specified voucher organization.
     *
     * @param userId                the scoped user ID
     * @param voucherOrganizationId the voucher organization ID
     * @param points                # of points to add
     *
     * @throws LinkIDVoucherRewardException something went wrong, check the error code in the exception
     */
    void voucherReward(String userId, String voucherOrganizationId, long points)
            throws LinkIDVoucherRewardException;

    /**
     * List the vouchers specified user has for specified voucher organization
     *
     * @param userId                the scoped user ID
     * @param voucherOrganizationId optional voucher organization ID
     * @param includeInactive       include inactive vouchers
     * @param locale                locale for returning localization voucher organization name, description
     *
     * @return the list of active vouchers
     *
     * @throws LinkIDVoucherListException something went wrong, check the error code in the exception
     */
    LinkIDVouchers voucherList(String userId, @Nullable String voucherOrganizationId, boolean includeInactive, Locale locale)
            throws LinkIDVoucherListException;

    /**
     * Get info about a voucher for a given voucherId
     *
     * @param voucherId the voucher ID for which info is required
     * @param locale    locale for returning localization voucher organization name, description
     *
     * @return the voucher info
     *
     * @throws LinkIDVoucherInfoException something went wrong, check the error code in the exception
     */
    LinkIDVoucher voucherInfo(String voucherId, Locale locale)
            throws LinkIDVoucherInfoException;

    /**
     * List the redeemed vouchers specified user has for specified voucher organization
     *
     * @param userId                the scoped user ID
     * @param voucherOrganizationId the voucher organization ID
     * @param locale                locale for returning localization voucher organization name, description
     * @param dateFilter            optional date filter
     * @param pageFilter            optional page filter
     *
     * @return the list of redeemed vouchers
     *
     * @throws LinkIDVoucherListRedeemedException something went wrong, check the error code in the exception
     */
    LinkIDVouchers voucherListRedeemed(String userId, String voucherOrganizationId, Locale locale, @Nullable LinkIDReportDateFilter dateFilter,
                                       @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDVoucherListRedeemedException;

    /**
     * Redeem the specified voucher
     *
     * @param voucherId ID of the voucher
     *
     * @throws LinkIDVoucherRedeemException something went wrong, check the error code in the exception
     */
    void voucherRedeem(String voucherId)
            throws LinkIDVoucherRedeemException;

    /**
     * Add/update a voucher organization
     *
     * @param voucherOrganization the voucher organization
     *
     * @return the technical name of the added/update voucher organization
     *
     * @throws LinkIDVoucherOrganizationAddUpdateException something went wrong, check the error code in the exception
     */
    String voucherOrganizationAddUpdate(LinkIDVoucherOrganization voucherOrganization)
            throws LinkIDVoucherOrganizationAddUpdateException;

    /**
     * Returns the list of voucher organizations the caller application owns
     *
     * @param voucherOrganizationIds optional list of voucher organization IDs
     * @param includeStats           include stats?
     *
     * @return the list of owned voucher organizations
     */
    List<LinkIDVoucherOrganizationDetails> voucherOrganizationList(@Nullable List<String> voucherOrganizationIds, boolean includeStats);

    /**
     * Remove specified voucher organization
     * <p>
     * NOTE: this can only be done if no vouchers exist for it. Once a voucher has been created ( not necessarily redeemed ) there is no way to remove this
     * voucher organization.
     *
     * @param voucherOrganizationId the voucher organization ID
     *
     * @throws LinkIDVoucherOrganizationRemoveException something went wrong, check the error code in the exception
     */
    void voucherOrganizationRemove(String voucherOrganizationId)
            throws LinkIDVoucherOrganizationRemoveException;

    /**
     * Activate ( or deactivate ) specified voucher organization
     *
     * @param voucherOrganizationId the voucher organization ID
     * @param active                activate or deactivate
     *
     * @throws LinkIDVoucherOrganizationActivateException something went wrong, check the error code in the exception
     */
    void voucherOrganizationActivate(String voucherOrganizationId, boolean active)
            throws LinkIDVoucherOrganizationActivateException;

    /**
     * Fetch voucher history for specified organization, filtered if wanted
     *
     * @param voucherOrganizationIds the voucher organization IDs
     * @param eventTypeFilter        optional voucher history event type filter
     * @param userFilter             optional user ID filter
     * @param applicationFilter      optional  application filter
     * @param dateFilter             optional date filter
     * @param pageFilter             optional page filter
     * @param language               optional language (default is en)
     *
     * @throws LinkIDVoucherOrganizationHistoryException something went wrong, check the error code in the exception
     */
    LinkIDVoucherHistory voucherOrganizationHistory(@Nullable List<String> voucherOrganizationIds, @Nullable LinkIDVoucherEventTypeFilter eventTypeFilter,
                                                    @Nullable LinkIDUserFilter userFilter, @Nullable LinkIDApplicationFilter applicationFilter,
                                                    @Nullable LinkIDReportDateFilter dateFilter, @Nullable LinkIDReportPageFilter pageFilter,
                                                    @Nullable String language)
            throws LinkIDVoucherOrganizationHistoryException;

    /**
     * Request a new linkID Theme
     *
     * @param themeConfig the theme configuration
     *
     * @return the official technical name of the theme
     *
     * @throws LinkIDThemeAddException something went wrong, check the error code and info message
     */
    String themeAdd(LinkIDThemeConfig themeConfig)
            throws LinkIDThemeAddException;

    /**
     * Request to remove a linkID theme
     *
     * @param themeName      the name of the theme
     * @param removeReleased is the theme a pending theme or an already released one
     *
     * @throws LinkIDThemeRemoveException something went wrong, check the error code and info message
     */
    void themeRemove(String themeName, boolean removeReleased)
            throws LinkIDThemeRemoveException;

    /**
     * Fetch the status of the theme request
     *
     * @param themeName the name of the theme
     *
     * @return the status
     *
     * @throws LinkIDThemeStatusException something went wrong, check the error code and info message
     */
    LinkIDThemeStatus themeStatus(String themeName)
            throws LinkIDThemeStatusException;

    /**
     * Fetch the application's themes, if needed filtered by themeName, status code
     *
     * @param themeName         optional theme name for filtering
     * @param requestStatusCode optional status code for filtering
     */
    LinkIDThemes themeList(@Nullable String themeName, @Nullable LinkIDRequestStatusCode requestStatusCode);

    /**
     * Add a new payment configuration
     *
     * @param paymentConfiguration the payment configuration
     *
     * @return the official technical name of the payment configuration
     *
     * @throws LinkIDPaymentConfigurationAddException something went wrong, check the error code in the exception
     */
    String paymentConfigurationAdd(LinkIDPaymentConfiguration paymentConfiguration)
            throws LinkIDPaymentConfigurationAddException;

    /**
     * Update an existing payment configuration
     *
     * @param paymentConfiguration the payment configuration
     *
     * @return the official technical name of the payment configuration
     *
     * @throws LinkIDPaymentConfigurationUpdateException something went wrong, check the error code in the exception
     */
    String paymentConfigurationUpdate(LinkIDPaymentConfiguration paymentConfiguration)
            throws LinkIDPaymentConfigurationUpdateException;

    /**
     * Remove an existing payment configuration
     *
     * @param name the name of the configuration to remove
     *
     * @throws LinkIDPaymentConfigurationRemoveException something went wrong, check the error code in the exception
     */
    void paymentConfigurationRemove(String name)
            throws LinkIDPaymentConfigurationRemoveException;

    /**
     * List all payment configurations
     */
    List<LinkIDPaymentConfiguration> paymentConfigurationList();

    /**
     * Add a permission for specified entity to specified application. Have to be owner of the entity behind the ID to do this
     *
     * @param id              the ID of the entity to give permission to ( voucher organization ID, wallet organization ID )
     * @param applicationName the application's technical name
     * @param permissionType  what permission to give
     *
     * @throws LinkIDApplicationPermissionAddException something went wrong, check the error code in the exception
     */
    void applicationPermissionAdd(String id, String applicationName, LinkIDApplicationPermissionType permissionType)
            throws LinkIDApplicationPermissionAddException;

    /**
     * Remove a permission for specified entity. Have to be owner of the entity behind the ID to do this
     * <p>
     * If no application name is specified the permission for your application will be removed.
     * If an application name is specified, you'll have to be owner of the entity to remove the permission
     *
     * @param id              the ID of the entity to give permission to ( voucher organization ID, wallet organization ID )
     * @param applicationName optional application name if the owner wants to remove the permission
     * @param permissionType  what permission to remove
     *
     * @throws LinkIDApplicationPermissionRemoveException something went wrong, check the error code in the exception
     */
    void applicationPermissionRemove(String id, @Nullable String applicationName, LinkIDApplicationPermissionType permissionType)
            throws LinkIDApplicationPermissionRemoveException;

    /**
     * Returns the list of permissions the caller application has for specified entity
     *
     * @param id the ID of the entity to give permission to ( voucher organization ID, wallet organization ID )
     *
     * @return the list of permissions
     *
     * @throws LinkIDApplicationPermissionListException something went wrong, check the error code in the exception
     */
    LinkIDApplicationPermissions applicationPermissionList(String id)
            throws LinkIDApplicationPermissionListException;

    /**
     * Get the comment with specified ID
     *
     * @param id the id of the comment
     *
     * @return the comment
     *
     * @throws LinkIDCommentGetException something went wrong, check the error code in the exception
     */
    String commentGet(String id)
            throws LinkIDCommentGetException;

    /**
     * Query the list of users that have a subscription to your application, optionally filtered by...
     *
     * @param voucherOrganizationId list only users who have a voucher for specified voucher organization ID
     * @param walletOrganizationId  list only users who have a voucher for specified voucher organization ID
     * @param createdFilter         Optional date filter on when user created a subscription
     * @param authenticatedFilter   Optional date filter on when user authenticated for your application for the last time
     * @param pageFilter            optional page filter if too many results
     *
     * @return the users
     *
     * @throws LinkIDUserListException something went wrong, check the error code in the exception
     */
    LinkIDUsers userList(@Nullable String voucherOrganizationId, @Nullable String walletOrganizationId, @Nullable final LinkIDReportDateFilter createdFilter,
                         @Nullable final LinkIDReportDateFilter authenticatedFilter, @Nullable final LinkIDReportPageFilter pageFilter)
            throws LinkIDUserListException;

    /**
     * Request credentials of specified type
     *
     * @param label human friendly label for the credential
     * @param type  the credential type
     *
     * @return the credential request, e.g. download URL, ... don't forget to confirm the credential after the download
     */
    LinkIDCredentialRequest credentialGet(String label, LinkIDCredentialType type);

    /**
     * Remove the specified credential
     *
     * @param name the username or certificate subject
     *
     * @throws LinkIDCredentialRemoveException something went wrong, check the error code in the exception
     */
    void credentialRemove(String name)
            throws LinkIDCredentialRemoveException;

    /**
     * Get the credentials available for this application
     *
     * @return the credentials
     */
    List<LinkIDCredential> credentialList();

    /**
     * Add a new notification location
     *
     * @param label    human readable label for this location
     * @param location the location
     * @param topics   the topics to be subscribed to
     *
     * @return the technical URN to be used in update, delete calls
     *
     * @throws LinkIDNotificationAddException something went wrong, check the error code in the exception
     */
    String notificationAdd(String label, String location, List<LinkIDNotificationTopicConfiguration> topics)
            throws LinkIDNotificationAddException;

    /**
     * Add a new notification location
     *
     * @param urn      the technical urn
     * @param label    human readable label for this location
     * @param location the location
     * @param topics   the topics to be subscribed to
     *
     * @return the technical URN to be used in update, delete calls
     *
     * @throws LinkIDNotificationUpdateException something went wrong, check the error code in the exception
     */
    String notificationUpdate(String urn, String label, String location, List<LinkIDNotificationTopicConfiguration> topics)
            throws LinkIDNotificationUpdateException;

    /**
     * Get notification location info
     *
     * @param urns optional URN list, if empty returns all
     *
     * @return the notification locations
     */
    List<LinkIDNotificationLocation> notificationList(List<String> urns);

    /**
     * Remove the notification locations with specified URNs
     *
     * @param urns the URNs
     *
     * @throws LinkIDNotificationRemoveException something went wrong, check the error code in the exception
     */
    void notificationRemove(List<String> urns)
            throws LinkIDNotificationRemoveException;

    /**
     * Request a new application
     *
     * @param name                     the application name
     * @param applicationUrl           the application url
     * @param logoUrl                  the application logo url
     * @param nameLocalizations        the name i18n
     * @param descriptionLocalizations the description i18n
     *
     * @return the URN of the application
     *
     * @throws LinkIDApplicationAddException something went wrong, check the error code in the exception
     */
    String applicationAdd(String name, String applicationUrl, String logoUrl, List<LinkIDLocalizationValue> nameLocalizations,
                          List<LinkIDLocalizationValue> descriptionLocalizations)
            throws LinkIDApplicationAddException;
}
