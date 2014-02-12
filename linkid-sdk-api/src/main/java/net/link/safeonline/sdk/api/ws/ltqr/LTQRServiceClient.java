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
     * @param paymentContext Optional payment context
     * @param oneTimeUse     Long term QR session can only be used once
     * @param expiryDate     Optional expiry date of the long term session.
     * @param expiryDuration Optional expiry duration of the long term session. Expressed in number of seconds starting from the creation.
     *                       Do not mix this attribute with expiryDate. If so, expiryDate will be preferred.
     *
     * @return Success object containing the QR in PNG format, the content of the QR code and a type 4 UUID session ID of the created long term session. This
     * session ID will be used in the notifications to the Service Provider.
     *
     * @throws PushException failure
     */
    LTQRSession push(@Nullable PaymentContextDO paymentContext, boolean oneTimeUse, @Nullable Date expiryDate, @Nullable Long expiryDuration)
            throws PushException;

    /**
     * Fetch a set of client sessions.
     *
     * @param orderReferences  Optional list of orderReferences to fetch. If none are specified, all LTQR sessions and client session are returned.
     * @param clientSessionIds optional list of client session IDs
     *
     * @return list of client sessions
     *
     * @throws PullException failure
     */
    List<LTQRClientSession> pull(@Nullable List<String> orderReferences, @Nullable List<String> clientSessionIds)
            throws PullException;

    /**
     * Remove a set of client sessions.
     *
     * @param orderReferences  List of orderReferences to remove. If none are specified all related client sessions will be removed.
     * @param clientSessionIds optional list of client session IDs to remove
     *
     * @throws RemoveException failure
     */
    void remove(@Nullable List<String> orderReferences, @Nullable List<String> clientSessionIds)
            throws RemoveException;
}
