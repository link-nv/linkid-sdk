/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.wallet;

import com.google.common.base.MoreObjects;
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

    private final LinkIDWalletOrganization              organization;
    //
    private final boolean                               owner;
    private final boolean                               pendingRemoval;
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

    public LinkIDWalletOrganizationDetails(final LinkIDWalletOrganization organization, final boolean owner, final boolean pendingRemoval,
                                           final List<LinkIDApplicationPermissionType> permissions, @Nullable final LinkIDWalletOrganizationStats stats,
                                           final List<String> permissionAddCreditApplications, final List<String> permissionRemoveCreditApplications,
                                           final List<String> permissionRemoveApplications, final List<String> permissionEnrollApplications,
                                           final List<String> permissionUseApplications) {

        this.organization = organization;
        this.owner = owner;
        this.pendingRemoval = pendingRemoval;
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

        return MoreObjects.toStringHelper( this )
                          .add( "organization", organization )
                          .add( "owner", owner )
                          .add( "pendingRemoval", pendingRemoval )
                          .add( "permissions", permissions )
                          .add( "stats", stats )
                          .add( "permissionAddCreditApplications", permissionAddCreditApplications )
                          .add( "permissionRemoveCreditApplications", permissionRemoveCreditApplications )
                          .add( "permissionRemoveApplications", permissionRemoveApplications )
                          .add( "permissionEnrollApplications", permissionEnrollApplications )
                          .add( "permissionUseApplications", permissionUseApplications )
                          .toString();
    }

    // Accessors

    public LinkIDWalletOrganization getOrganization() {

        return organization;
    }

    public boolean isOwner() {

        return owner;
    }

    public boolean isPendingRemoval() {

        return pendingRemoval;
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
