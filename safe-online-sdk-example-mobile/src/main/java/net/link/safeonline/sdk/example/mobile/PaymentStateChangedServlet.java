package net.link.safeonline.sdk.example.mobile;

import com.lyndir.lhunath.opal.system.logging.Logger;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import net.link.safeonline.sdk.api.payment.PaymentConstants;
import net.link.safeonline.sdk.api.payment.PaymentState;
import net.link.safeonline.sdk.api.ws.payment.PaymentServiceClient;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;


public class PaymentStateChangedServlet extends HttpServlet {

    private static final Logger logger = Logger.get( PaymentStateChangedServlet.class );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String transactionId = request.getParameter( PaymentConstants.PAYMENT_CHANGED_ID_PARAM );
        if (null == transactionId) {
            logger.inf( "Payment status update but no txn ID..." );
            return;
        }

        logger.inf( "Payment status update for transaction %s", transactionId );

        // lookup the transaction

        // fetch the status report using the linkID payment web service
        PaymentServiceClient paymentServiceClient = LinkIDServiceFactory.getPaymentService();
        PaymentState paymentState = paymentServiceClient.getStatus( transactionId );

        logger.dbg( "  * state = %s", paymentState );

        // update the payment transaction
    }
}
