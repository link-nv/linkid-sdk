/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

/**
 * Logout phases enumerate. This is used by the stateful {@link LogoutService} to keep track of its current state.
 * 
 * @author wvdhaute
 * 
 */
public enum LogoutState {
    /**
     * Logout has not yet begin.
     */
    INIT,
    /**
     * Logout requested.
     */
    INITIALIZED,
    /**
     * This state marks that a logout request is being sent to a single sign-on application.
     */
    LOGGING_OUT
}
