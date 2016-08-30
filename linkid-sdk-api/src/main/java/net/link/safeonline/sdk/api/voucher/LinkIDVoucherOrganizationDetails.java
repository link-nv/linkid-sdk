/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissionType;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 29/04/16
 * Time: 14:56
 */
public class LinkIDVoucherOrganizationDetails implements Serializable {

    private final LinkIDVoucherOrganization             organization;
    private final boolean                               owner;
    //
    private final List<LinkIDApplicationPermissionType> permissions;
    //
    @Nullable
    private final LinkIDVoucherOrganizationStats        stats;
    //
    // permissions
    private final List<String>                          rewardPermissionApplications;
    private final List<String>                          listPermissionApplications;
    private final List<String>                          redeemPermissionApplications;

    public LinkIDVoucherOrganizationDetails(final LinkIDVoucherOrganization organization, final boolean owner,
                                            final List<LinkIDApplicationPermissionType> permissions, @Nullable final LinkIDVoucherOrganizationStats stats,
                                            final List<String> rewardPermissionApplications, final List<String> listPermissionApplications,
                                            final List<String> redeemPermissionApplications) {

        this.organization = organization;
        this.owner = owner;
        this.permissions = permissions;
        this.stats = stats;
        this.rewardPermissionApplications = rewardPermissionApplications;
        this.listPermissionApplications = listPermissionApplications;
        this.redeemPermissionApplications = redeemPermissionApplications;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherOrganizationDetails{" + "organization=" + organization + ", owner=" + owner + ", stats=" + stats
               + ", rewardPermissionApplications=" + rewardPermissionApplications + ", listPermissionApplications=" + listPermissionApplications
               + ", redeemPermissionApplications=" + redeemPermissionApplications + '}';
    }

    // Accessors

    public LinkIDVoucherOrganization getOrganization() {

        return organization;
    }

    public boolean isOwner() {

        return owner;
    }

    public List<LinkIDApplicationPermissionType> getPermissions() {

        return permissions;
    }

    @Nullable
    public LinkIDVoucherOrganizationStats getStats() {

        return stats;
    }

    public List<String> getRewardPermissionApplications() {

        return rewardPermissionApplications;
    }

    public List<String> getListPermissionApplications() {

        return listPermissionApplications;
    }

    public List<String> getRedeemPermissionApplications() {

        return redeemPermissionApplications;
    }
}
