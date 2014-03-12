/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile.username;

import net.link.util.logging.Logger;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.payment.PaymentConstants;
import net.link.safeonline.sdk.api.payment.PaymentState;
import net.link.safeonline.sdk.api.ws.payment.PaymentServiceClient;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;


public class PaymentStateChangedServlet extends HttpServlet {

    private static final Logger logger = Logger.get( PaymentStateChangedServlet.class );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String orderReference = request.getParameter( PaymentConstants.PAYMENT_CHANGED_ORDER_REF_PARAM );
        if (null == orderReference) {
            logger.inf( "Payment status update but no txn ID..." );
            return;
        }

        logger.inf( "Payment status update for orderReference %s", orderReference );

        // lookup the transaction

        // fetch the status report using the linkID payment web service
        PaymentServiceClient paymentServiceClient = LinkIDServiceFactory.getPaymentService();
        PaymentState paymentState;
        try {
            paymentState = paymentServiceClient.getStatus( orderReference );
        }
        catch (WSClientTransportException e) {
            logger.err( e, "Failed to get payment state..." );
            return;
        }

        logger.dbg( "  * state = %s", paymentState );

        // update the payment transaction
    }
}
