/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

/**
 * Authentication phases enumerate. This is used by the stateful
 * {@link AuthenticationService} to keep track of its current state.
 * 
 * @author fcorneli
 * 
 */
public enum AuthenticationState {
	/**
	 * The initial state. At this point the user has not been authenticated yet
	 * via any authentication device.
	 */
	INIT,
	/**
	 * This state marks that the user has been authenticated via some
	 * authentication device.
	 */
	USER_AUTHENTICATED
}
