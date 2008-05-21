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
	String next();

	/*
	 * Factories.
	 */
	List<SelectItem> applicationDevicesFactory();

	List<SelectItem> allDevicesFactory();
}
