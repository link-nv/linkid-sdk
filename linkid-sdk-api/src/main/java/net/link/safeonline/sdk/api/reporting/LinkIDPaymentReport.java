package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentOrder;


/**
 * Created by wvdhaute
 * Date: 19/11/15
 * Time: 10:44
 */
public class LinkIDPaymentReport implements Serializable {

    private final long                     total;
    private final List<LinkIDPaymentOrder> paymentOrders;

    public LinkIDPaymentReport(final long total, final List<LinkIDPaymentOrder> paymentOrders) {

        this.total = total;
        this.paymentOrders = paymentOrders;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDPaymentReport{" +
               "total=" + total +
               ", paymentOrders=" + paymentOrders +
               '}';
    }

    // Accessors

    public long getTotal() {

        return total;
    }

    public List<LinkIDPaymentOrder> getPaymentOrders() {

        return paymentOrders;
    }
}
