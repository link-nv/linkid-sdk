/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceDescriptionException;

@Local
public interface DeviceDescription {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Actions.
	 */
	String add() throws ExistingDeviceDescriptionException,
			DeviceNotFoundException;

	String edit();

	String remove() throws DeviceNotFoundException,
			DeviceDescriptionNotFoundException;

	String save();

	String cancelEdit();

	/*
	 * Factories
	 */
	void deviceDescriptionsListFactory() throws DeviceNotFoundException;

	/*
	 * Acccessors
	 */
	String getLanguage();

	void setLanguage(String language);

	String getDescription();

	void setDescription(String description);
}
