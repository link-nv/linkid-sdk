package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by wvdhaute
 * Date: 03/02/16
 * Time: 16:24
 */
public class LinkIDVoucher implements Serializable {

    private final String id;
    private final String name;
    private final String description;
    private final String logoUrl;
    //
    private final long   counter;
    private final long   limit;
    private final Date   activated;
    private final Date   redeemed;
    //
    private final String voucherOrganizationId;

    public LinkIDVoucher(final String id, final String name, final String description, final String logoUrl, final long counter, final long limit,
                         final Date activated, final Date redeemed, final String voucherOrganizationId) {

        this.id = id;

        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;

        this.counter = counter;
        this.limit = limit;
        this.activated = activated;
        this.redeemed = redeemed;

        this.voucherOrganizationId = voucherOrganizationId;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucher{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", logoUrl='" + logoUrl + '\'' +
               ", counter=" + counter +
               ", limit=" + limit +
               ", activated=" + activated +
               ", redeemed=" + redeemed +
               ", voucherOrganizationId='" + voucherOrganizationId + '\'' +
               '}';
    }

    // Accessors

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public String getDescription() {

        return description;
    }

    public String getLogoUrl() {

        return logoUrl;
    }

    public long getCounter() {

        return counter;
    }

    public long getLimit() {

        return limit;
    }

    public Date getActivated() {

        return activated;
    }

    public Date getRedeemed() {

        return redeemed;
    }

    public String getVoucherOrganizationId() {

        return voucherOrganizationId;
    }
}
