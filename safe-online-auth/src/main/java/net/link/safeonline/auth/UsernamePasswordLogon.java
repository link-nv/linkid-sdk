/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import javax.ejb.Local;


import net.link.safeonline.auth.AuthenticationConstants;

@Local
public interface UsernamePasswordLogon {

    public static final String JNDI_BINDING = AuthenticationConstants.JNDI_PREFIX + "UsernamePasswordLogonBean/local";

    /*
     * Accessors.
     */
    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    /*
     * Actions.
     */
    String login();

    /*
     * Lifecycle.
     */
    void init();

    void destroyCallback();
}
