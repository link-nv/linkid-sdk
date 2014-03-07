package net.link.safeonline.sdk.example.mobile.x509;

import com.lyndir.lhunath.opal.system.logging.Logger;
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
            logger.inf( "Payment status update but no orderReference..." );
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