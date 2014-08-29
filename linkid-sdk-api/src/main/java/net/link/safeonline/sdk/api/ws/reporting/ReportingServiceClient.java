/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.reporting;

import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.parking.ParkingSessionDO;
import net.link.safeonline.sdk.api.payment.PaymentTransactionDO;
import org.jetbrains.annotations.Nullable;


/**
 * linkID Payment WS Client.
 * <p/>
 * Via this interface, applications can fetch payment status reports.
 */
public interface ReportingServiceClient {

    /**
     * @param startDate         optional startDate
     * @param endDate           optional endDate, not specified means till now
     * @param orderReferences   optional order references
     * @param mandateReferences optional mandate references
     *
     * @return The payment transactions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<PaymentTransactionDO> getPaymentReport(@Nullable Date startDate, @Nullable Date endDate, @Nullable List<String> orderReferences,
                                                @Nullable List<String> mandateReferences)
            throws WSClientTransportException;

    /**
     * @param startDate optional startDate
     * @param endDate   optional endDate, not specified means till now
     * @param barCodes  optional bar codes
     * @param parkings  optional parkings
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<ParkingSessionDO> getParkingReport(@Nullable Date startDate, @Nullable Date endDate, @Nullable List<String> barCodes, @Nullable List<String> parkings)
            throws WSClientTransportException;
}
