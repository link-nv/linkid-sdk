/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.permissions;

import java.util.List;


public class LinkIDApplicationPermissions {

    private final boolean                               owner;
    private final List<LinkIDApplicationPermissionType> permissions;

    public LinkIDApplicationPermissions(final boolean owner, final List<LinkIDApplicationPermissionType> permissions) {

        this.owner = owner;
        this.permissions = permissions;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDApplicationPermissions{" + "owner=" + owner + ", permissions=" + permissions + '}';
    }

    // Accessors

    public boolean isOwner() {

        return owner;
    }

    public List<LinkIDApplicationPermissionType> getPermissions() {

        return permissions;
    }
}
