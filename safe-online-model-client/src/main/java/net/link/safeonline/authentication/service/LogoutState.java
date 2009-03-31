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
     * An application began the SSO logout sequence, but this application has not yet started its logout process.
     */
    INITIALIZED,

    /**
     * An application was selected to begin the logout process. This state is only used during the sequential logout process.
     */
    INITIATED,

    /**
     * This application began its logout process.
     */
    LOGGING_OUT,

    /**
     * This application's logout request completed but the application reported that it couldn't successfully log the user session out.
     */
    LOGOUT_FAILED,

    /**
     * This application's logout request completed and the application reported that it has successfully logged out the user session.
     */
    LOGOUT_SUCCESS
}
