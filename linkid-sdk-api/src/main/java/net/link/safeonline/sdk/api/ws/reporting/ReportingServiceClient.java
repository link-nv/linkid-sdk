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
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import org.jetbrains.annotations.Nullable;


/**
 * linkID Payment WS Client.
 * <p/>
 * Via this interface, applications can fetch payment status reports and parking session reports.
 */
@SuppressWarnings("UnusedDeclaration")
public interface ReportingServiceClient {

    /**
     * @param startDate startDate
     * @param endDate   optional endDate, not specified means till now
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<LinkIDPaymentOrder> getPaymentReport(Date startDate, @Nullable Date endDate)
            throws WSClientTransportException;

    /**
     * @param orderReferences order references
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<LinkIDPaymentOrder> getPaymentReportForOrderReferences(List<String> orderReferences)
            throws WSClientTransportException;

    /**
     * @param mandateReferences mandate references
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<LinkIDPaymentOrder> getPaymentReportForMandates(List<String> mandateReferences)
            throws WSClientTransportException;

    /**
     * @param startDate startDate
     * @param endDate   optional endDate, not specified means till now
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<ParkingSessionDO> getParkingReport(Date startDate, @Nullable Date endDate)
            throws WSClientTransportException;

    /**
     * @param startDate startDate
     * @param endDate   optional endDate, not specified means till now
     * @param parkings  optional list of parkings
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<ParkingSessionDO> getParkingReport(Date startDate, @Nullable Date endDate, @Nullable List<String> parkings)
            throws WSClientTransportException;

    /**
     * @param barCodes bar codes
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<ParkingSessionDO> getParkingReportForBarCodes(List<String> barCodes)
            throws WSClientTransportException;

    /**
     * @param ticketNumbers ticket numbers
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<ParkingSessionDO> getParkingReportForTicketNumbers(List<String> ticketNumbers)
            throws WSClientTransportException;

    /**
     * @param dtaKeys dtaKeys
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<ParkingSessionDO> getParkingReportForDTAKeys(List<String> dtaKeys)
            throws WSClientTransportException;

    /**
     * @param parkings parkings
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws WSClientTransportException could not contact the linkID web service
     */
    List<ParkingSessionDO> getParkingReportForParkings(List<String> parkings)
            throws WSClientTransportException;
}
