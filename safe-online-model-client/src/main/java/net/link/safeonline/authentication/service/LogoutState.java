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
     * The initial state. At this point the user is not logging out, the initial logout request has yet to be received.
     */
    INIT,
    /**
     * The initialized state. At this point we have received a logout request for a certain application.
     */
    INITIALIZED,
    /**
     * This state marks that a logout request is being sent to a single sign-on application.
     */
    LOGGING_OUT
}
