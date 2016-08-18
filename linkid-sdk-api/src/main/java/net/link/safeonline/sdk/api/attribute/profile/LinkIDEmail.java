/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute.profile;

import java.io.Serializable;
import java.util.Collection;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 18/08/16
 * Time: 11:24
 */
public class LinkIDEmail implements Serializable {

    private String  email;
    private boolean confirmed;

    // Helper methods

    @Nullable
    public static LinkIDEmail findEmail(final Collection<? extends LinkIDAttribute<Serializable>> attributes) {

        if (null == attributes) {
            return null;
        }

        LinkIDEmail linkIDEmail = null;
        for (LinkIDAttribute<Serializable> attribute : attributes) {

            if (attribute.getName().equals( LinkIDProfileConstants.EMAIL_ADDRESS )) {
                if (null == linkIDEmail) {
                    linkIDEmail = new LinkIDEmail();
                    linkIDEmail.setEmail( (String) attribute.getValue() );
                }
            }
            if (attribute.getName().equals( LinkIDProfileConstants.EMAIL_CONFIRMED )) {
                if (null == linkIDEmail) {
                    linkIDEmail = new LinkIDEmail();
                    linkIDEmail.setConfirmed( (Boolean) attribute.getValue() );
                }
            }
        }

        return linkIDEmail;
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
