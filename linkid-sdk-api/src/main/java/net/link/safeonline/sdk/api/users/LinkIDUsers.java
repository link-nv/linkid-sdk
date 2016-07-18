/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.users;

import java.io.Serializable;
import java.util.List;


public class LinkIDUsers implements Serializable {

    private final List<LinkIDUser> users;
    private final long             total;

    public LinkIDUsers(final List<LinkIDUser> users, final long total) {

        this.total = total;
        this.users = users;
    }

    public List<LinkIDUser> getUsers() {

        return users;
    }

    public long getTotal() {

        return total;
    }
}
