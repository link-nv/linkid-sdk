/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline;

/**
 * Components implementing this interface can be notified during application
 * startup and shutdown.
 * 
 * @author fcorneli
 * 
 */
public interface Startable {

	/**
	 * Callback to notify that the SafeOnline system has started.
	 */
	void postStart();

	/**
	 * Callback to notify that the SafeOnline system is about to be stopped.
	 */
	void preStop();
}
