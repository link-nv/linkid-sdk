/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import javax.ejb.Local;

@Local
public interface DeviceRegistration {

	/*
	 * Accessors.
	 */
	String getDevice();

	void setDevice(String device);

	String getPassword();

	void setPassword(String password);

	String getUsername();

	/*
	 * Actions.
	 */
	String deviceNext();

	String passwordNext();

	/*
	 * Factories
	 */

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
