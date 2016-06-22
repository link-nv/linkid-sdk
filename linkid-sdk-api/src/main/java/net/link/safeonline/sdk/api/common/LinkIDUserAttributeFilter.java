/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.common;

import java.io.Serializable;


/**
 * Search users by the given email-address
 */
public class LinkIDUserAttributeFilter implements Serializable {

    private final String email;

    public LinkIDUserAttributeFilter(final String email) {

        this.email = email;
    }

    @Override
    public String toString() {

        return "LinkIDUserAttributeFilter{" +
               "email='" + email + '\'' +
               '}';
    }

    public String getEmail() {

        return email;
    }
}
