/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.voucher;

import java.util.List;


public class LinkIDVoucherOrganizationUsers {

    private final List<String> userIds;
    private final long         total;

    public LinkIDVoucherOrganizationUsers(final List<String> userIds, final long total) {

        this.total = total;
        this.userIds = userIds;
    }

    public List<String> getUserIds() {

        return userIds;
    }

    public long getTotal() {

        return total;
    }
}
