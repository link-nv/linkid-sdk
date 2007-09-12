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
public interface Device {

	public static final String AUTHN_DEVICE_ATTRIBUTE = "AuthenticationDevice";

	/*
	 * Accessors.
	 */
	String getSelection();

	void setSelection(String deviceSelection);

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Actions.
	 */
	/**
	 * This action is used by both the main.xhtml page as the all-devices.xhtml
	 * page. This means it cannot enforce the device restrictions itself. We
	 * will leave this over to the LoginServlet.
	 * 
	 * @return
	 */
	String next();

	/*
	 * Factories.
	 */
	List<SelectItem> applicationDevicesFactory();
}
