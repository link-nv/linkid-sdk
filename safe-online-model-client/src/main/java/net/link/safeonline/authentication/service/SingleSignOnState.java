/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

/**
 * Single Sign on phases enumerate. This is used by the stateful {@link SingleSignOnService} to keep track of its current state.
 * 
 * @author wvdhaute
 * 
 */
public enum SingleSignOnState {
    /**
     * The initial state. At this point the single sign on bean has been constructed, marking the authentication is sso enabled.
     */
    INIT,
    /**
     * The initialized state for sign on. At this point the sso bean has been initialized with a list of application pools.
     */
    INITIALIZED,
    /**
     * Select user state. At this point the sso bean has found multiple possible users. Selection has to be made which one.
     */
    SELECT_USER,
    /**
     * Force Authenticate state. At this point user is forced to manually login. Either due to explicit Force Authentication or no valid
     * single sign on cookie was found. When successfully authenticated, update the valid sso cookies ( right user/device combination ) or
     * create a new SSO cookie.
     */
    FORCE_AUTHENTICATION,
    /**
     * Failed SSO state. SSO has failed, no valid SSO cookies found.
     */
    FAILED,
    /**
     * Successful SSO state. At this point the sso bean has found a single valid user.
     */
    SUCCESS
}
