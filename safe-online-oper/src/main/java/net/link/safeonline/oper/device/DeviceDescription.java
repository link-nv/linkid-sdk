/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

@Local
public interface DeviceDescription {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Actions.
	 */
	String add();

	String edit();

	String remove();

	String save();

	String cancelEdit();

	/*
	 * Factories
	 */
	void deviceDescriptionsListFactory();

	/*
	 * Acccessors
	 */
	String getLanguage();

	void setLanguage(String language);

	String getDescription();

	void setDescription(String description);
}
