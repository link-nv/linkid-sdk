/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceClassDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceClassDescriptionException;

@Local
public interface DeviceClassDescription {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factories
	 */
	void deviceClassDescriptionsListFactory()
			throws DeviceClassNotFoundException;

	/*
	 * Actions.
	 */
	String add() throws ExistingDeviceClassDescriptionException,
			DeviceClassNotFoundException;

	String edit();

	String save();

	String remove() throws DeviceClassDescriptionNotFoundException,
			DeviceClassNotFoundException;

	String cancelEdit();

	/*
	 * Acccessors
	 */
	String getLanguage();

	void setLanguage(String language);

	String getDescription();

	void setDescription(String description);
}
