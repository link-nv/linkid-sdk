/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

@Local
public interface DeviceClass {

	/*
	 * Actions
	 */
	String view();

	String add();

	String edit();

	String save();

	String remove();

	/*
	 * Accessors
	 */
	String getName();

	void setName(String name);

	String getAuthenticationContextClass();

	void setAuthenticationContextClass(String authenticationContextClass);

	/*
	 * Factories
	 */
	void deviceClassListFactory();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

}
