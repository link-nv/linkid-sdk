/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.ltqr;

import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.ltqr.LinkIDChangeException;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRClientSession;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRInfo;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRInfoException;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRPollingConfiguration;
import net.link.safeonline.sdk.api.ltqr.LinkIDLTQRSession;
import net.link.safeonline.sdk.api.ltqr.LinkIDPullException;
import net.link.safeonline.sdk.api.ltqr.LinkIDPushException;
import net.link.safeonline.sdk.api.ltqr.LinkIDRemoveException;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import org.jetbrains.annotations.Nullable;


/**
 * linkID Long Term QR WS client.
 * <p/>
 * Via this interface, service providers can push new long term QR session and fetch completed long term transactions.
 */
public interface LinkIDLTQRServiceClient {

    /**
     * Push a long term QR session to linkID.
     *
     * @param authenticationMessage Optional authentication message to be shown in the pin view in the mobile app. If there is a payment, this will be ignored.
     * @param finishedMessage       Optional finished message on the final view in the mobile app.
     * @param paymentContext        Optional payment context
     * @param oneTimeUse            Long term QR session can only be used once
     * @param expiryDate            Optional expiry date of the long term session.
     * @param expiryDuration        Optional expiry duration of the long term session. Expressed in number of seconds starting from the creation.
     *                              Do not mix this attribute with expiryDate. If so, expiryDate will be preferred.
     * @param callback              Optional callback config
     * @param identityProfiles      Optional identity profiles
     * @param sessionExpiryOverride optional session expiry (seconds)
     * @param theme                 optional theme, if not specified default application theme will be chosen
     * @param mobileLandingSuccess  optional landing page for an authn/payment started on iOS browser
     * @param mobileLandingError    optional landing page for an authn/payment started on iOS browser
     * @param mobileLandingCancel   optional landing page for an authn/payment started on iOS browser
     * @param pollingConfiguration  Optional polling configuration
     * @param waitForUnlock         Marks the LTQR to wait for an explicit unlock call. This only makes sense for single-use LTQR codes. Unlock the LTQR with
     *                              the change operation with unlock=true
     * @param ltqrStatusLocation    Optional LTQR status location
     *
     * @return Success object containing the QR in PNG format, the content of the QR code and a type 4 UUID session ID of the created long term session. This
     * session ID will be used in the notifications to the Service Provider.
     *
     * @throws LinkIDPushException failure
     */
    LinkIDLTQRSession push(@Nullable String authenticationMessage, @Nullable String finishedMessage, @Nullable LinkIDPaymentContext paymentContext,
                           boolean oneTimeUse, @Nullable Date expiryDate, @Nullable Long expiryDuration, @Nullable LinkIDCallback callback,
                           @Nullable List<String> identityProfiles, @Nullable Long sessionExpiryOverride, @Nullable String theme,
                           @Nullable String mobileLandingSuccess, @Nullable String mobileLandingError, @Nullable String mobileLandingCancel,
                           @Nullable LinkIDLTQRPollingConfiguration pollingConfiguration, boolean waitForUnlock, @Nullable String ltqrStatusLocation)
            throws LinkIDPushException;

    /**
     * Change an existing long term QR code
     *
     * @param ltqrReference         LTQR reference, mandatory
     * @param authenticationMessage Optional authentication message to be shown in the pin view in the mobile app. If there is a payment, this will be ignored.
     * @param finishedMessage       Optional finished message on the final view in the mobile app.
     * @param paymentContext        Optional payment context
     * @param expiryDate            Optional expiry date of the long term session.
     * @param expiryDuration        Optional expiry duration of the long term session. Expressed in number of seconds starting from the creation.
     *                              Do not mix this attribute with expiryDate. If so, expiryDate will be preferred.
     * @param callback              Optional callback config
     * @param identityProfiles      Optional identity profiles
     * @param sessionExpiryOverride optional session expiry (seconds)
     * @param theme                 optional theme, if not specified default application theme will be chosen
     * @param mobileLandingSuccess  optional landing page for an authn/payment started on iOS browser
     * @param mobileLandingError    optional landing page for an authn/payment started on iOS browser
     * @param mobileLandingCancel   optional landing page for an authn/payment started on iOS browser
     * @param resetUsed             Optional flag for single use LTQR codes to let them be used again one time. If multi use this flag does nothing.
     * @param pollingConfiguration  Optional polling configuration
     * @param waitForUnlock         Marks the LTQR to wait for an explicit unlock call. This only makes sense for single-use LTQR codes. Unlock the LTQR with
     *                              the change operation with unlock=true
     * @param unlock                Unlocks the LTQR. When the first linkID user has finished for this LTQR, it will go back to locked if waitForUnlock=true.
     * @param ltqrStatusLocation    Optional LTQR status location
     *
     * @return Success object containing the QR in PNG format, the content of the QR code and a type 4 UUID session ID of the created long term session. This
     */
    LinkIDLTQRSession change(String ltqrReference, @Nullable String authenticationMessage, @Nullable String finishedMessage,
                             @Nullable LinkIDPaymentContext paymentContext, @Nullable Date expiryDate, @Nullable Long expiryDuration,
                             @Nullable LinkIDCallback callback, @Nullable List<String> identityProfiles, @Nullable Long sessionExpiryOverride,
                             @Nullable String theme, @Nullable String mobileLandingSuccess, @Nullable String mobileLandingError,
                             @Nullable String mobileLandingCancel, boolean resetUsed, @Nullable LinkIDLTQRPollingConfiguration pollingConfiguration,
                             boolean waitForUnlock, boolean unlock, @Nullable String ltqrStatusLocation)
            throws LinkIDChangeException;

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
     * @throws LinkIDPullException failure
     */
    List<LinkIDLTQRClientSession> pull(@Nullable List<String> ltqrReferences, @Nullable List<String> paymentOrderReferences,
                                       @Nullable List<String> clientSessionIds)
            throws LinkIDPullException;

    /**
     * Remove a set of client sessions.
     *
     * @param ltqrReferences         Optional list of LTQR References to remove. If none are specified, all LTQR sessions and client session are removed.
     * @param paymentOrderReferences Optional list of Payment order References to remove. If none are specified, all are removed for the LTQR References
     *                               specified above.
     * @param clientSessionIds       optional list of client session IDs to remove
     *
     * @throws LinkIDRemoveException failure
     */
    void remove(@Nullable List<String> ltqrReferences, @Nullable List<String> paymentOrderReferences, @Nullable List<String> clientSessionIds)
            throws LinkIDRemoveException;

    /**
     * Fetch info for the specified LTQR references
     *
     * @param ltqrReferences the list of LTQR references to fetch info for
     *
     * @return the LTQR info objects
     *
     * @throws LinkIDLTQRInfoException failure
     */
    List<LinkIDLTQRInfo> info(List<String> ltqrReferences)
            throws LinkIDLTQRInfoException;
}
