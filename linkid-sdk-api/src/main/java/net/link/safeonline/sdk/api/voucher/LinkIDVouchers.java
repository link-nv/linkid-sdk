package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 04/02/16
 * Time: 09:32
 */
public class LinkIDVouchers implements Serializable {

    private final List<LinkIDVoucher> vouchers;
    private final long                total;

    public LinkIDVouchers(final List<LinkIDVoucher> vouchers, final long total) {

        this.vouchers = vouchers;
        this.total = total;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVouchers{" +
               "vouchers=" + vouchers +
               ", total=" + total +
               '}';
    }

    // Accessors

    public List<LinkIDVoucher> getVouchers() {

        return vouchers;
    }

    public long getTotal() {

        return total;
    }
}
