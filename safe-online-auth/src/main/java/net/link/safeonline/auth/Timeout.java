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
public interface Timeout {

    public static final String JNDI_BINDING = AuthenticationConstants.JNDI_PREFIX + "TimeoutBean/local";


    /*
     * Accessors.
     */
    String getApplicationUrl();

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
