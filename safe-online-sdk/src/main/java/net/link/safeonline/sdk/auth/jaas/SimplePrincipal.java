/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.jaas;

import java.security.Principal;


/**
 * Simple username-based JAAS principal.
 * 
 * @author fcorneli
 * 
 */
public class SimplePrincipal implements Principal {

    private final String username;


    /**
     * Main constructor.
     * 
     * @param username
     *            the user name.
     */
    public SimplePrincipal(String username) {

        this.username = username;
    }

    public String getName() {

        return this.username;
    }

    @Override
    public boolean equals(Object obj) {

        /*
         * This method is required to be able to remove the principal from the subject on logout.
         */
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Principal)) {
            return false;
        }
        Principal rhs = (Principal) obj;
        return this.username.equals(rhs.getName());
    }

    @Override
    public String toString() {

        return "Principal: " + this.username;
    }
}
