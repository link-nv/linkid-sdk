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
    private final Date   activated;
    private final Date   redeemed;

    public LinkIDVoucher(final String id, final String name, final String description, final String logoUrl, final Date activated, final Date redeemed) {

        this.id = id;

        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.activated = activated;
        this.redeemed = redeemed;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucher{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", logoUrl='" + logoUrl + '\'' +
               ", activated=" + activated +
               ", redeemed=" + redeemed +
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

    public Date getActivated() {

        return activated;
    }

    public Date getRedeemed() {

        return redeemed;
    }
}
