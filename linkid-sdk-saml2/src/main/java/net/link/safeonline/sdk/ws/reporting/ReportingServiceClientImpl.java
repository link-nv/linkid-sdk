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
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.common.ParkingSession;
import net.lin_k.safe_online.common.PaymentTransaction;
import net.lin_k.safe_online.reporting.ParkingReportRequest;
import net.lin_k.safe_online.reporting.ParkingReportResponse;
import net.lin_k.safe_online.reporting.PaymentReportRequest;
import net.lin_k.safe_online.reporting.PaymentReportResponse;
import net.lin_k.safe_online.reporting.ReportingServicePort;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.parking.ParkingSessionDO;
import net.link.safeonline.sdk.api.payment.PaymentTransactionDO;
import net.link.safeonline.sdk.api.ws.reporting.ReportingServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.reporting.ReportingServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.jetbrains.annotations.Nullable;


public class ReportingServiceClientImpl extends AbstractWSClient<ReportingServicePort> implements ReportingServiceClient {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security configuration
     */
    public ReportingServiceClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        this( location, sslCertificate );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the ltqr web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public ReportingServiceClientImpl(final String location, final X509Certificate sslCertificate,
                                      final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificate );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the reporting web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    private ReportingServiceClientImpl(String location, X509Certificate sslCertificate) {

        super( ReportingServiceFactory.newInstance().getReportingServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.reporting.path" ) ) );
    }

    @Override
    public List<PaymentTransactionDO> getPaymentReport(@Nullable final Date startDate, @Nullable final Date endDate,
                                                       @Nullable final List<String> orderReferences, @Nullable final List<String> mandateReferences)
            throws WSClientTransportException {

        PaymentReportRequest request = new PaymentReportRequest();

        if (null != startDate) {
            request.setStartDate( SDKUtils.convert( startDate ) );
        }
        if (null != endDate) {
            request.setEndDate( SDKUtils.convert( endDate ) );
        }
        if (null != orderReferences) {
            request.getOrderReferences().addAll( orderReferences );
        }
        if (null != mandateReferences) {
            request.getMandateReferences().addAll( mandateReferences );
        }

        try {
            PaymentReportResponse response = getPort().paymentReport( request );

            List<PaymentTransactionDO> transactions = Lists.newLinkedList();
            for (PaymentTransaction paymentTransaction : response.getTransactions()) {
                transactions.add( new PaymentTransactionDO( paymentTransaction.getDate().toGregorianCalendar().getTime(), paymentTransaction.getAmount(),
                        SDKUtils.convert( paymentTransaction.getCurrency() ), paymentTransaction.getPaymentMethod(), paymentTransaction.getDescription(),
                        SDKUtils.convert( paymentTransaction.getPaymentState() ), paymentTransaction.isPaid(), paymentTransaction.getOrderReference(),
                        paymentTransaction.getDocdataReference(), paymentTransaction.getUserId(), paymentTransaction.getEmail(),
                        paymentTransaction.getGivenName(), paymentTransaction.getFamilyName() ) );
            }
            return transactions;
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    @Override
    public List<ParkingSessionDO> getParkingReport(@Nullable final Date startDate, @Nullable final Date endDate, @Nullable final List<String> barCodes,
                                                   @Nullable final List<String> parkings)
            throws WSClientTransportException {

        ParkingReportRequest request = new ParkingReportRequest();

        if (null != startDate) {
            request.setStartDate( SDKUtils.convert( startDate ) );
        }
        if (null != endDate) {
            request.setEndDate( SDKUtils.convert( endDate ) );
        }
        if (null != barCodes) {
            request.getBarCodes().addAll( barCodes );
        }
        if (null != parkings) {
            request.getParkings().addAll( parkings );
        }

        try {
            ParkingReportResponse response = getPort().parkingReport( request );

            List<ParkingSessionDO> sessions = Lists.newLinkedList();

            for (ParkingSession session : response.getSessions()) {
                sessions.add( new ParkingSessionDO( session.getDate().toGregorianCalendar().getTime(), session.getBarCode(), session.getParking(),
                        session.getUserId(), session.getTurnover(), session.isValidated(), session.getPaymentOrderReference(),
                        SDKUtils.convert( session.getPaymentState() ) ) );
            }

            return sessions;
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }
}
