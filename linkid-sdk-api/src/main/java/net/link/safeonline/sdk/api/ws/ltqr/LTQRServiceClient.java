package net.link.safeonline.sdk.api.ws.ltqr;

import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.api.ltqr.*;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import org.jetbrains.annotations.Nullable;


/**
 * linkID Long Term QR WS client.
 * <p/>
 * Via this interface, service provider's can push new long term QR session and fetch completed long term transactions.
 */
public interface LTQRServiceClient {

    /**
     * Push a long term QR session to linkID.
     *
     * @param ltqrServiceProvider the service provider credentials
     * @param paymentContext      Optional payment context
     * @param oneTimeUse          Long term QR session can only be used once
     * @param expiryDate          Optional expiry date of the long term session.
     * @param expiryDuration      Optional expiry duration of the long term session. Expressed in number of seconds starting from the creation.
     *                            Do not mix this attribute with expiryDate. If so, expiryDate will be preferred.
     *
     * @return Success object containing the QR in PNG format, the content of the QR code and a tyoe 4 UUID session ID of the created long term session. This
     * session ID will be used in the notifications to the Service Provider.
     *
     * @throws PushException failure
     */
    LTQRSession push(LTQRServiceProvider ltqrServiceProvider, @Nullable PaymentContextDO paymentContext, boolean oneTimeUse, @Nullable Date expiryDate,
                     @Nullable Long expiryDuration)
            throws PushException;

    /**
     * Fetch a set of client sessions.
     *
     * @param ltqrServiceProvider the service provider credentials
     * @param sessionIds          optional list of long term session Ids, if empty or null all client sessions for all ltqr sessions will be returned
     * @param clientSessionIds    optional list of client session IDs
     *
     * @return list of client sessions
     *
     * @throws PullException failure
     */
    List<LTQRClientSession> pull(LTQRServiceProvider ltqrServiceProvider, @Nullable List<String> sessionIds, @Nullable List<String> clientSessionIds)
            throws PullException;
}
