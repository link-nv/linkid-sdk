/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.ltqr;

import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.api.ltqr.*;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import org.jetbrains.annotations.Nullable;


/**
 * linkID Long Term QR WS client.
 * <p/>
 * Via this interface, service providers can push new long term QR session and fetch completed long term transactions.
 */
public interface LTQRServiceClient {

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
     *
     * @return Success object containing the QR in PNG format, the content of the QR code and a type 4 UUID session ID of the created long term session. This
     * session ID will be used in the notifications to the Service Provider.
     *
     * @throws PushException failure
     */
    LTQRSession push(@Nullable String authenticationMessage, @Nullable String finishedMessage, @Nullable PaymentContextDO paymentContext, boolean oneTimeUse,
                     @Nullable Date expiryDate, @Nullable Long expiryDuration)
            throws PushException;

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
     */
    ChangeResponseDO change(String ltqrReference, @Nullable String authenticationMessage, @Nullable String finishedMessage,
                            @Nullable PaymentContextDO paymentContext, @Nullable Date expiryDate, @Nullable Long expiryDuration)
            throws ChangeException;

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
     * @throws PullException failure
     */
    List<LTQRClientSession> pull(@Nullable List<String> ltqrReferences, @Nullable List<String> paymentOrderReferences, @Nullable List<String> clientSessionIds)
            throws PullException;

    /**
     * Remove a set of client sessions.
     *
     * @param ltqrReferences         Optional list of LTQR References to remove. If none are specified, all LTQR sessions and client session are removed.
     * @param paymentOrderReferences Optional list of Payment order References to remove. If none are specified, all are removed for the LTQR References
     *                               specified above.
     * @param clientSessionIds       optional list of client session IDs to remove
     *
     * @throws RemoveException failure
     */
    void remove(@Nullable List<String> ltqrReferences, @Nullable List<String> paymentOrderReferences, @Nullable List<String> clientSessionIds)
            throws RemoveException;
}
