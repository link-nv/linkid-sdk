/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

@Local
public interface DeviceProperty {

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factories
	 */
	void devicePropertiesListFactory();

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
	String getName();

	void setName(String name);

	String getValue();

	void setValue(String value);
}
