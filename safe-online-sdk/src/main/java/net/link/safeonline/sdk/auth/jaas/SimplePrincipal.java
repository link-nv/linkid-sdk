/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.jaas;

import java.security.Principal;


/**
 * Simple userId-based JAAS principal.
 * 
 * @author fcorneli
 * 
 */
public class SimplePrincipal implements Principal {

    private final String userId;


    /**
     * Main constructor.
     * 
     * @param userId
     *            the user ID.
     */
    public SimplePrincipal(String userId) {

        this.userId = userId;
    }

    public String getName() {

        return userId;
    }

    @Override
    public boolean equals(Object obj) {

        /*
         * This method is required to be able to remove the principal from the subject on logout.
         */
        if (this == obj)
            return true;
        if (!(obj instanceof Principal))
            return false;
        Principal rhs = (Principal) obj;
        return userId.equals(rhs.getName());
    }

    @Override
    public String toString() {

        return "Principal: " + userId;
    }
}
