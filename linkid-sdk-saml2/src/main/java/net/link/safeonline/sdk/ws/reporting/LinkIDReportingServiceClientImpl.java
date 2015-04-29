/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.reporting;

import com.google.common.collect.Lists;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.common.PaymentTransactionV20;
import net.lin_k.safe_online.common.WalletTransactionV20;
import net.lin_k.safe_online.reporting._2.ApplicationFilter;
import net.lin_k.safe_online.reporting._2.DateFilter;
import net.lin_k.safe_online.reporting._2.ParkingReportRequest;
import net.lin_k.safe_online.reporting._2.ParkingReportResponse;
import net.lin_k.safe_online.reporting._2.ParkingSession;
import net.lin_k.safe_online.reporting._2.PaymentOrder;
import net.lin_k.safe_online.reporting._2.PaymentReportRequest;
import net.lin_k.safe_online.reporting._2.PaymentReportResponse;
import net.lin_k.safe_online.reporting._2.ReportingServicePort;
import net.lin_k.safe_online.reporting._2.WalletFilter;
import net.lin_k.safe_online.reporting._2.WalletReportRequest;
import net.lin_k.safe_online.reporting._2.WalletReportResponse;
import net.lin_k.safe_online.reporting._2.WalletReportTransaction;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.parking.LinkIDParkingSession;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentTransaction;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import net.link.safeonline.sdk.api.reporting.LinkIDReportApplicationFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportDateFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDReportErrorCode;
import net.link.safeonline.sdk.api.reporting.LinkIDReportException;
import net.link.safeonline.sdk.api.reporting.LinkIDReportWalletFilter;
import net.link.safeonline.sdk.api.reporting.LinkIDWalletReportTransaction;
import net.link.safeonline.sdk.api.ws.reporting.LinkIDReportingServiceClient;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.reporting.LinkIDReportingServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDReportingServiceClientImpl extends AbstractWSClient<ReportingServicePort> implements LinkIDReportingServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDReportingServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDReportingServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                            final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the reporting web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    private LinkIDReportingServiceClientImpl(String location, X509Certificate[] sslCertificates) {

        super( LinkIDReportingServiceFactory.newInstance().getReportingServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.reporting.path" ) ) );
    }

    @Override
    public List<LinkIDPaymentOrder> getPaymentReport(final Date startDate, @Nullable final Date endDate)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getPaymentReport( startDate, endDate, null, null );
    }

    @Override
    public List<LinkIDPaymentOrder> getPaymentReportForOrderReferences(final List<String> orderReferences)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getPaymentReport( null, null, orderReferences, null );
    }

    @Override
    public List<LinkIDPaymentOrder> getPaymentReportForMandates(final List<String> mandateReferences)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getPaymentReport( null, null, null, mandateReferences );
    }

    @Override
    public List<LinkIDParkingSession> getParkingReport(final Date startDate, @Nullable final Date endDate)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( startDate, endDate, null, null, null, null );
    }

    @Override
    public List<LinkIDParkingSession> getParkingReport(final Date startDate, @Nullable final Date endDate, @Nullable final List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( startDate, endDate, null, null, null, parkings );
    }

    @Override
    public List<LinkIDParkingSession> getParkingReportForBarCodes(final List<String> barCodes)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( null, null, barCodes, null, null, null );
    }

    @Override
    public List<LinkIDParkingSession> getParkingReportForTicketNumbers(final List<String> ticketNumbers)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( null, null, null, ticketNumbers, null, null );
    }

    @Override
    public List<LinkIDParkingSession> getParkingReportForDTAKeys(final List<String> dtaKeys)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( null, null, null, null, dtaKeys, null );
    }

    @Override
    public List<LinkIDParkingSession> getParkingReportForParkings(final List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getParkingReport( null, null, null, null, null, parkings );
    }

    @Override
    public List<LinkIDWalletReportTransaction> getWalletReport(final String walletOrganizationId, final LinkIDReportDateFilter dateFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getWalletReport( walletOrganizationId, dateFilter, null, null );
    }

    @Override
    public List<LinkIDWalletReportTransaction> getWalletReport(final String walletOrganizationId, final LinkIDReportApplicationFilter applicationFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getWalletReport( walletOrganizationId, null, applicationFilter, null );
    }

    @Override
    public List<LinkIDWalletReportTransaction> getWalletReport(final String walletOrganizationId, final LinkIDReportWalletFilter walletFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        return getWalletReport( walletOrganizationId, null, null, walletFilter );
    }

    // Helper methods

    private List<LinkIDWalletReportTransaction> getWalletReport(final String walletOrganizationId, @Nullable final LinkIDReportDateFilter dateFilter,
                                                                @Nullable final LinkIDReportApplicationFilter applicationFilter,
                                                                @Nullable final LinkIDReportWalletFilter walletFilter)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        WalletReportRequest request = new WalletReportRequest();

        request.setWalletOrganizationId( walletOrganizationId );

        if (null != dateFilter) {
            DateFilter wsDateFilter = new DateFilter();
            wsDateFilter.setStartDate( LinkIDSDKUtils.convert( dateFilter.getStartDate() ) );
            if (null != dateFilter.getEndDate()) {
                wsDateFilter.setEndDate( LinkIDSDKUtils.convert( dateFilter.getEndDate() ) );
            }
            request.setDateFilter( wsDateFilter );
        }
        if (null != applicationFilter) {
            ApplicationFilter wsApplicationFilter = new ApplicationFilter();
            wsApplicationFilter.setApplicationName( applicationFilter.getApplicationName() );
            request.setApplicationFilter( wsApplicationFilter );
        }
        if (null != walletFilter) {
            WalletFilter wsWalletFilter = new WalletFilter();
            wsWalletFilter.setWalletId( walletFilter.getWalletId() );
            wsWalletFilter.setUserId( walletFilter.getUserId() );
            request.setWalletFilter( wsWalletFilter );
        }

        try {
            WalletReportResponse response = getPort().walletReport( request );

            if (null != response.getError()) {
                throw new LinkIDReportException( convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDWalletReportTransaction> transactions = Lists.newLinkedList();

            for (WalletReportTransaction walletReportTransaction : response.getTransactions()) {

                transactions.add( new LinkIDWalletReportTransaction( walletReportTransaction.getWalletId(),
                        LinkIDSDKUtils.convert( walletReportTransaction.getCreationDate() ), walletReportTransaction.getTransactionId(),
                        walletReportTransaction.getAmount(), LinkIDSDKUtils.convert( walletReportTransaction.getCurrency() ),
                        walletReportTransaction.getUserId(), walletReportTransaction.getApplicationName() ) );
            }

            return transactions;
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    private List<LinkIDPaymentOrder> getPaymentReport(@Nullable final Date startDate, @Nullable final Date endDate,
                                                      @Nullable final List<String> orderReferences, @Nullable final List<String> mandateReferences)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        PaymentReportRequest request = new PaymentReportRequest();

        if (null != startDate) {
            request.setStartDate( LinkIDSDKUtils.convert( startDate ) );
        }
        if (null != endDate) {
            request.setEndDate( LinkIDSDKUtils.convert( endDate ) );
        }
        if (null != orderReferences) {
            request.getOrderReferences().addAll( orderReferences );
        }
        if (null != mandateReferences) {
            request.getMandateReferences().addAll( mandateReferences );
        }

        try {
            PaymentReportResponse response = getPort().paymentReport( request );

            if (null != response.getError()) {
                throw new LinkIDReportException( convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDPaymentOrder> orders = Lists.newLinkedList();
            for (PaymentOrder paymentOrder : response.getOrders()) {

                // payment transactions
                List<LinkIDPaymentTransaction> transactions = Lists.newLinkedList();
                for (PaymentTransactionV20 paymentTransaction : paymentOrder.getTransactions()) {
                    transactions.add( new LinkIDPaymentTransaction( LinkIDSDKUtils.convert( paymentTransaction.getPaymentMethodType() ),
                            paymentTransaction.getPaymentMethod(), LinkIDSDKUtils.convert( paymentTransaction.getPaymentState() ),
                            convert( paymentTransaction.getCreationDate() ), convert( paymentTransaction.getAuthorizationDate() ),
                            convert( paymentTransaction.getCapturedDate() ), paymentTransaction.getDocdataReference(), paymentTransaction.getAmount(),
                            LinkIDSDKUtils.convert( paymentTransaction.getCurrency() ) ) );
                }

                // wallet transactions
                List<LinkIDWalletTransaction> walletTransactions = Lists.newLinkedList();
                for (WalletTransactionV20 walletTransaction : paymentOrder.getWalletTransactions()) {
                    walletTransactions.add( new LinkIDWalletTransaction( walletTransaction.getWalletId(), convert( walletTransaction.getCreationDate() ),
                            walletTransaction.getTransactionId(), walletTransaction.getAmount(), LinkIDSDKUtils.convert( walletTransaction.getCurrency() ) ) );
                }

                // order
                orders.add( new LinkIDPaymentOrder( convert( paymentOrder.getDate() ), paymentOrder.getAmount(),
                        LinkIDSDKUtils.convert( paymentOrder.getCurrency() ), paymentOrder.getDescription(),
                        LinkIDSDKUtils.convert( paymentOrder.getPaymentState() ), paymentOrder.getAmountPayed(), paymentOrder.isAuthorized(),
                        paymentOrder.isCaptured(), paymentOrder.getOrderReference(), paymentOrder.getUserId(), paymentOrder.getEmail(),
                        paymentOrder.getGivenName(), paymentOrder.getFamilyName(), transactions, walletTransactions ) );
            }

            return orders;
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    private List<LinkIDParkingSession> getParkingReport(@Nullable final Date startDate, @Nullable final Date endDate, @Nullable final List<String> barCodes,
                                                        @Nullable final List<String> ticketNumbers, @Nullable final List<String> dtaKeys,
                                                        @Nullable final List<String> parkings)
            throws LinkIDWSClientTransportException, LinkIDReportException {

        ParkingReportRequest request = new ParkingReportRequest();

        if (null != startDate) {
            request.setStartDate( LinkIDSDKUtils.convert( startDate ) );
        }
        if (null != endDate) {
            request.setEndDate( LinkIDSDKUtils.convert( endDate ) );
        }
        if (null != barCodes) {
            request.getBarCodes().addAll( barCodes );
        }
        if (null != ticketNumbers) {
            request.getTicketNumbers().addAll( ticketNumbers );
        }
        if (null != dtaKeys) {
            request.getDtaKeys().addAll( dtaKeys );
        }
        if (null != parkings) {
            request.getParkings().addAll( parkings );
        }

        try {
            ParkingReportResponse response = getPort().parkingReport( request );

            if (null != response.getError()) {
                throw new LinkIDReportException( convert( response.getError().getErrorCode() ) );
            }

            List<LinkIDParkingSession> sessions = Lists.newLinkedList();

            for (ParkingSession session : response.getSessions()) {
                sessions.add( new LinkIDParkingSession( session.getDate().toGregorianCalendar().getTime(), session.getBarCode(), session.getParking(),
                        session.getUserId(), session.getTurnover(), session.isValidated(), session.getPaymentOrderReference(),
                        LinkIDSDKUtils.convert( session.getPaymentState() ) ) );
            }

            return sessions;
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    private Date convert(XMLGregorianCalendar xmlDate) {

        return null != xmlDate? xmlDate.toGregorianCalendar().getTime(): null;
    }

    private LinkIDReportErrorCode convert(final net.lin_k.safe_online.reporting._2.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_TOO_MANY_RESULTS:
                return LinkIDReportErrorCode.ERROR_TOO_MANY_RESULTS;
            case ERROR_UNEXPECTED:
                return LinkIDReportErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
