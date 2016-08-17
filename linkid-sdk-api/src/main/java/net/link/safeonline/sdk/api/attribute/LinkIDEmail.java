/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute;

import com.google.common.base.MoreObjects;
import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 17/08/16
 * Time: 14:03
 */
public class LinkIDEmail implements Serializable {

    private String  email;
    private boolean confirmed;

    public LinkIDEmail() {

    }

    public LinkIDEmail(final String email, final boolean confirmed) {

        this.email = email;
        this.confirmed = confirmed;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this ).add( "email", email ).add( "confirmed", confirmed ).toString();
    }

    // Accessors

    public String getEmail() {

        return email;
    }

    public void setEmail(final String email) {

        this.email = email;
    }

    public boolean isConfirmed() {

        return confirmed;
    }

    public void setConfirmed(final boolean confirmed) {

        this.confirmed = confirmed;
    }
}
