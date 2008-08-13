/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

public class AuthenticationConstants {

    private AuthenticationConstants() {

        // empty
    }


    public static final String JNDI_PREFIX     = "SafeOnline/auth/";

    /**
     * The name of the security domain used within the SafeOnline authentication web application control components.
     */
    public static final String SECURITY_DOMAIN = "safe-online-auth";

    /**
     * The EJB RBAC role used within the SafeOnline authentication web application components.
     */
    public static final String USER_ROLE       = "user";
}
