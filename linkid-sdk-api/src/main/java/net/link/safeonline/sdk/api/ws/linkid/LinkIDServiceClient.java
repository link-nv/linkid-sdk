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
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.reporting.LinkIDParkingReport;
import net.link.safeonline.sdk.api.reporting.LinkIDPaymentReport;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportException;
import net.link.safeonline.sdk.api.reporting.LinkIDReportPageFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReport;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.ws.callback.LinkIDCallbackPullException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthCancelException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizationException;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDThemesException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRChangeException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRClientSession;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRContent;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRInfo;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRInfoException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRLockType;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPullException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRPushException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRRemoveException;
import net.link.safeonline.sdk.api.ws.linkid.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDMandatePaymentException;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentCaptureException;
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
     * Fetch the application's themes
     *
     * @throws LinkIDThemesException something went wrong, check the error code in the exception
     */
    LinkIDThemes getThemes(String applicationName)
            throws LinkIDThemesException;

    /**
     * Fetch the specified keys's localization in linkID.
     * <p/>
     * e.g. for getting the wallet organization ID's localization, wallet coin ID
     *
     * @param keys the keys to fetch localization for
     *
     * @return the localizations
     *
     * @throws LinkIDLocalizationException something went wrong, check the error code in the exception
     */
    List<LinkIDLocalization> getLocalization(List<String> keys)
            throws LinkIDLocalizationException;

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
    String mandatePayment(String mandateReference, LinkIDPaymentContext paymentContext, Locale locale)
            throws LinkIDMandatePaymentException;

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
     * @param dateFilter optional date filter
     * @param pageFilter optional page filter
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDPaymentReport getPaymentReport(@Nullable LinkIDReportDateFilter dateFilter, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param orderReferences order references
     * @param pageFilter      optional page filter
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDPaymentReport getPaymentReportForOrderReferences(List<String> orderReferences, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param mandateReferences mandate references
     * @param pageFilter        optional page filter
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDPaymentReport getPaymentReportForMandates(List<String> mandateReferences, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param dateFilter optional date filter
     * @param pageFilter optional page filter
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDParkingReport getParkingReport(@Nullable LinkIDReportDateFilter dateFilter, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param dateFilter optional date filter
     * @param pageFilter optional page filter
     * @param parkings   optional list of parkings
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDParkingReport getParkingReport(@Nullable LinkIDReportDateFilter dateFilter, @Nullable LinkIDReportPageFilter pageFilter,
                                         @Nullable List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param barCodes   bar codes
     * @param pageFilter optional page filter
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDParkingReport getParkingReportForBarCodes(List<String> barCodes, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param ticketNumbers ticket numbers
     * @param pageFilter    optional page filter
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDParkingReport getParkingReportForTicketNumbers(List<String> ticketNumbers, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param dtaKeys    dtaKeys
     * @param pageFilter optional page filter
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDParkingReport getParkingReportForDTAKeys(List<String> dtaKeys, @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param applicationFilter application filter
     * @param walletFilter      wallet filter
     * @param dateFilter        date filter
     * @param pageFilter        optional page filter
     *
     * @return the wallet transactions matching your search. If none found and empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    LinkIDWalletReport getWalletReport(String walletOrganizationId, @Nullable LinkIDReportApplicationFilter applicationFilter,
                                       @Nullable LinkIDReportWalletFilter walletFilter, @Nullable LinkIDReportDateFilter dateFilter,
                                       @Nullable LinkIDReportPageFilter pageFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * Enroll users for a wallet. Optionally specify initial credit to add to wallet if applicable
     *
     * @return walletId the enrolled wallet ID
     *
     * @throws LinkIDWalletEnrollException something went wrong, check the error code in the exception
     */
    String walletEnroll(String userId, String walletOrganizationId, double amount, @Nullable LinkIDCurrency currency, @Nullable String walletCoin)
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
    void walletAddCredit(String userId, String walletId, double amount, @Nullable LinkIDCurrency currency, @Nullable String walletCoi)
            throws LinkIDWalletAddCreditException;

    /**
     * Remove credit for a user for a wallet.
     * If the amount is > than the credit on their wallet or amount==-1, their wallet credit will be set to 0
     *
     * @throws LinkIDWalletRemoveCreditException something went wrong, check the error code in the exception
     */
    void walletRemoveCredit(String userId, String walletId, double amount, @Nullable LinkIDCurrency currency, @Nullable String walletCoi)
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

}
