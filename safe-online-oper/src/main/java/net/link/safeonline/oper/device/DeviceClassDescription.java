/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

@Local
public interface DeviceClassDescription {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factories
	 */
	void deviceClassDescriptionsListFactory();

	/*
	 * Actions.
	 */
	String add();

	String edit();

	String save();

	String remove();

	String cancelEdit();

	/*
	 * Acccessors
	 */
	String getLanguage();

	void setLanguage(String language);

	String getDescription();

	void setDescription(String description);
}
