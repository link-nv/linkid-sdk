/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatus;
import net.link.safeonline.sdk.api.ws.linkid.payment.LinkIDPaymentStatusException;
import net.link.safeonline.sdk.util.LinkIDNotificationMessage;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.logging.Logger;


public class LinkIDNotificationServlet extends HttpServlet {

    private static final Logger logger = Logger.get( LinkIDNotificationServlet.class );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LinkIDNotificationMessage notificationMessage = new LinkIDNotificationMessage( request );
        logger.dbg( "Notification message: %s", notificationMessage );

        LinkIDServiceClient linkIDServiceClient = LinkIDServiceFactory.getLinkIDService();

        switch (notificationMessage.getTopic()) {

            case REMOVE_USER:
                break;
            case UNSUBSCRIBE_USER:
                break;
            case ATTRIBUTE_UPDATE:
                break;
            case ATTRIBUTE_REMOVAL:
                break;
            case IDENTITY_UPDATE:
                break;
            case EXPIRED_AUTHENTICATION:
                break;
            case EXPIRED_PAYMENT:
                break;
            case MANDATE_ARCHIVED:
                break;
            case LTQR_SESSION_NEW:
                break;
            case LTQR_SESSION_UPDATE:
                break;
            case CONFIGURATION_UPDATE:
                break;
            case PAYMENT_ORDER_UPDATE:
                LinkIDPaymentStatus paymentState = null;
                try {
                    paymentState = linkIDServiceClient.getPaymentStatus( notificationMessage.getPaymentOrderReference() );
                }
                catch (LinkIDPaymentStatusException e) {
                    logger.err( e, e.getMessage() );
                }
                logger.dbg( "  * state = %s", paymentState );
                break;
        }
    }
}
