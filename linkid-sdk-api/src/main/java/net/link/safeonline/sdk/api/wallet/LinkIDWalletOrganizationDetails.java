/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.wallet;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.permissions.LinkIDApplicationPermissionType;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 29/04/16
 * Time: 14:56
 */
@SuppressWarnings({ "InstanceVariableNamingConvention", "unused", "MethodParameterNamingConvention" })
public class LinkIDWalletOrganizationDetails implements Serializable {

    private final LinkIDWalletOrganization organization;

    private final boolean owner;

    //
    private final List<LinkIDApplicationPermissionType> permissions;
    //
    @Nullable
    private final LinkIDWalletOrganizationStats         stats;
    //
    // permissions
    private final List<String>                          permissionAddCreditApplications;
    private final List<String>                          permissionRemoveCreditApplications;
    private final List<String>                          permissionRemoveApplications;
    private final List<String>                          permissionEnrollApplications;
    private final List<String>                          permissionUseApplications;

    public LinkIDWalletOrganizationDetails(final LinkIDWalletOrganization organization, final boolean owner,
                                           final List<LinkIDApplicationPermissionType> permissions, @Nullable final LinkIDWalletOrganizationStats stats,
                                           final List<String> permissionAddCreditApplications, final List<String> permissionRemoveCreditApplications,
                                           final List<String> permissionRemoveApplications, final List<String> permissionEnrollApplications,
                                           final List<String> permissionUseApplications) {

        this.organization = organization;
        this.owner = owner;
        this.permissions = permissions;
        this.stats = stats;
        this.permissionAddCreditApplications = permissionAddCreditApplications;
        this.permissionRemoveCreditApplications = permissionRemoveCreditApplications;
        this.permissionRemoveApplications = permissionRemoveApplications;
        this.permissionEnrollApplications = permissionEnrollApplications;
        this.permissionUseApplications = permissionUseApplications;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletOrganizationDetails{" + "organization=" + organization + ", owner=" + owner + ", permissions=" + permissions + ", stats=" + stats
               + ", permissionAddCreditApplications=" + permissionAddCreditApplications + ", permissionRemoveCreditApplications="
               + permissionRemoveCreditApplications + ", permissionRemoveApplications=" + permissionRemoveApplications + ", permissionEnrollApplications="
               + permissionEnrollApplications + ", permissionUseApplications=" + permissionUseApplications + '}';
    }

    // Accessors

    public LinkIDWalletOrganization getOrganization() {

        return organization;
    }

    public boolean isOwner() {

        return owner;
    }

    public List<LinkIDApplicationPermissionType> getPermissions() {

        return permissions;
    }

    @Nullable
    public LinkIDWalletOrganizationStats getStats() {

        return stats;
    }

    public List<String> getPermissionAddCreditApplications() {

        return permissionAddCreditApplications;
    }

    public List<String> getPermissionRemoveCreditApplications() {

        return permissionRemoveCreditApplications;
    }

    public List<String> getPermissionRemoveApplications() {

        return permissionRemoveApplications;
    }

    public List<String> getPermissionEnrollApplications() {

        return permissionEnrollApplications;
    }

    public List<String> getPermissionUseApplications() {

        return permissionUseApplications;
    }
}
