/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;


/**
 * Created by wvdhaute
 * Date: 29/04/16
 * Time: 14:56
 */
public class LinkIDVoucherOrganization implements Serializable {

    private final String                        id;
    private final String                        logoUrl;
    private final long                          voucherLimit;
    private final boolean                       active;
    private final List<LinkIDLocalizationValue> nameLocalizations;
    private final List<LinkIDLocalizationValue> descriptionLocalizations;

    public LinkIDVoucherOrganization(final String id, final String logoUrl, final long voucherLimit, final boolean active,
                                     final List<LinkIDLocalizationValue> nameLocalizations, final List<LinkIDLocalizationValue> descriptionLocalizations) {

        this.id = id;
        this.logoUrl = logoUrl;
        this.voucherLimit = voucherLimit;
        this.active = active;
        this.nameLocalizations = nameLocalizations;
        this.descriptionLocalizations = descriptionLocalizations;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherOrganization{" +
               "id='" + id + '\'' +
               ", logoUrl='" + logoUrl + '\'' +
               ", voucherLimit=" + voucherLimit +
               ", active=" + active +
               ", nameLocalizations=" + nameLocalizations +
               ", descriptionLocalizations=" + descriptionLocalizations +
               '}';
    }

    // Accessors

    public String getId() {

        return id;
    }

    public String getLogoUrl() {

        return logoUrl;
    }

    public long getVoucherLimit() {

        return voucherLimit;
    }

    public boolean isActive() {

        return active;
    }

    public List<LinkIDLocalizationValue> getNameLocalizations() {

        return nameLocalizations;
    }

    public List<LinkIDLocalizationValue> getDescriptionLocalizations() {

        return descriptionLocalizations;
    }
}
