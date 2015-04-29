/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.reporting;

import java.util.Date;
import java.util.List;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportException;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTransaction;
import org.jetbrains.annotations.Nullable;


/**
 * linkID Payment WS Client.
 * <p/>
 * Via this interface, applications can fetch payment status reports and parking session reports.
 */
@SuppressWarnings("UnusedDeclaration")
public interface LinkIDReportingServiceClient {

    /**
     * @param startDate startDate
     * @param endDate   optional endDate, not specified means till now
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDPaymentOrder> getPaymentReport(Date startDate, @Nullable Date endDate)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param orderReferences order references
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDPaymentOrder> getPaymentReportForOrderReferences(List<String> orderReferences)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param mandateReferences mandate references
     *
     * @return The payment orders matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDPaymentOrder> getPaymentReportForMandates(List<String> mandateReferences)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param startDate startDate
     * @param endDate   optional endDate, not specified means till now
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDParkingSession> getParkingReport(Date startDate, @Nullable Date endDate)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param startDate startDate
     * @param endDate   optional endDate, not specified means till now
     * @param parkings  optional list of parkings
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDParkingSession> getParkingReport(Date startDate, @Nullable Date endDate, @Nullable List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param barCodes bar codes
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDParkingSession> getParkingReportForBarCodes(List<String> barCodes)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param ticketNumbers ticket numbers
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDParkingSession> getParkingReportForTicketNumbers(List<String> ticketNumbers)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param dtaKeys dtaKeys
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDParkingSession> getParkingReportForDTAKeys(List<String> dtaKeys)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param parkings parkings
     *
     * @return The parking sessions matching your search. If none found an empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDParkingSession> getParkingReportForParkings(List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param dateFilter date filter
     *
     * @return the wallet transactions matching your search. If none found and empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDWalletReportTransaction> getWalletReport(String walletOrganizationId, LinkIDReportDateFilter dateFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param applicationFilter application filter
     *
     * @return the wallet transactions matching your search. If none found and empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDWalletReportTransaction> getWalletReport(String walletOrganizationId, LinkIDReportApplicationFilter applicationFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;

    /**
     * @param walletFilter wallet filter
     *
     * @return the wallet transactions matching your search. If none found and empty list is returned
     *
     * @throws LinkIDWSClientTransportException could not contact the linkID web service
     */
    List<LinkIDWalletReportTransaction> getWalletReport(String walletOrganizationId, LinkIDReportWalletFilter walletFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException;
}
