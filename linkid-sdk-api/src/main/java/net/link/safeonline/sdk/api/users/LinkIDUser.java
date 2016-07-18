/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.users;

import java.io.Serializable;
import java.util.Date;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 18/07/16
 * Time: 10:58
 */
public class LinkIDUser implements Serializable {

    private final String userId;
    private final Date   created;
    private final Date   lastAuthenticated;
    @Nullable
    private final Date   removed;

    public LinkIDUser(final String userId, final Date created, final Date lastAuthenticated, @Nullable final Date removed) {

        this.userId = userId;
        this.created = created;
        this.lastAuthenticated = lastAuthenticated;
        this.removed = removed;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDUser{" + "userId='" + userId + '\'' + ", created=" + created + ", lastAuthenticated=" + lastAuthenticated + ", removed=" + removed + '}';
    }

    // Accessors

    public String getUserId() {

        return userId;
    }

    public Date getCreated() {

        return created;
    }

    public Date getLastAuthenticated() {

        return lastAuthenticated;
    }

    @Nullable
    public Date getRemoved() {

        return removed;
    }
}
