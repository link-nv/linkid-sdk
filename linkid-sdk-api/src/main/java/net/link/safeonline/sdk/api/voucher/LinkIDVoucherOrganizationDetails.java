/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 29/04/16
 * Time: 14:56
 */
public class LinkIDVoucherOrganizationDetails implements Serializable {

    private final LinkIDVoucherOrganization organization;
    //
    // permissions
    private final List<String>              rewardPermissionApplications;
    private final List<String>              listPermissionApplications;
    private final List<String>              redeemPermissionApplications;

    public LinkIDVoucherOrganizationDetails(final LinkIDVoucherOrganization organization, final List<String> rewardPermissionApplications,
                                            final List<String> listPermissionApplications, final List<String> redeemPermissionApplications) {

        this.organization = organization;
        this.rewardPermissionApplications = rewardPermissionApplications;
        this.listPermissionApplications = listPermissionApplications;
        this.redeemPermissionApplications = redeemPermissionApplications;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherOrganization{" +
               "organization='" + organization + '\'' +
               ", rewardPermissionApplications=" + rewardPermissionApplications +
               ", listPermissionApplications=" + listPermissionApplications +
               ", redeemPermissionApplications=" + redeemPermissionApplications +
               '}';
    }

    // Accessors

    public LinkIDVoucherOrganization getOrganization() {

        return organization;
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
