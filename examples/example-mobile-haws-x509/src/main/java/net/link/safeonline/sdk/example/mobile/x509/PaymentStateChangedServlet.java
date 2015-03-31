/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile.x509;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentConstants;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.api.ws.payment.LinkIDPaymentServiceClient;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.logging.Logger;


public class PaymentStateChangedServlet extends HttpServlet {

    private static final Logger logger = Logger.get( PaymentStateChangedServlet.class );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String orderReference = request.getParameter( LinkIDPaymentConstants.PAYMENT_CHANGED_ORDER_REF_PARAM );
        if (null == orderReference) {
            logger.inf( "Payment status update but no orderReference..." );
            return;
        }

        logger.inf( "Payment status update for orderReference %s", orderReference );

        // lookup the transaction

        // fetch the status report using the linkID payment web service
        LinkIDPaymentServiceClient linkIDPaymentServiceClient = LinkIDServiceFactory.getPaymentService();
        LinkIDPaymentStatus linkIDPaymentStatus;
        try {
            linkIDPaymentStatus = linkIDPaymentServiceClient.getStatus( orderReference );
        }
        catch (LinkIDWSClientTransportException e) {
            logger.err( e, "Failed to get payment state..." );
            return;
        }

        logger.dbg( "  * state = %s", linkIDPaymentStatus );

        // update the payment transaction
    }
}
