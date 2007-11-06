/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

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

	String getMobile();

	void setMobile(String mobile);

	/*
	 * Actions.
	 */
	String deviceNext();

	String passwordNext();

	String mobileNext();

	/*
	 * Factories
	 */
	List<SelectItem> allDevicesFactory();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
