/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

/**
 * Authentication phases enumerate. This is used by the stateful {@link AuthenticationService} to keep track of its current state.
 * 
 * @author fcorneli
 * 
 */
public enum AuthenticationState {
    /**
     * The initial state. At this point the user has not been authenticated yet via any authentication device.
     */
    INIT,
    /**
     * The initialized state. At this point we have received an authentication or logout request for a certain application, containing a
     * possible device policy in case it was an authentication request.
     */
    INITIALIZED,
    /**
     * This state marks that the user has been redirected to an external device issuer to authenticate remotely. State can be active also
     * when a user manually went back from a remote registration, or a remote authentication has failed and the user requested to try
     * another device.
     */
    REDIRECTED,
    /**
     * This state marks that the user has been authenticated via some authentication device.
     */
    USER_AUTHENTICATED,
    /**
     * This state marks that the user authentication has been committed to the core.
     */
    COMMITTED
}
