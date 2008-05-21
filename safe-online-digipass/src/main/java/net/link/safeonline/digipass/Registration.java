/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass;

import javax.ejb.Local;

@Local
public interface Registration {

	/*
	 * Accessors.
	 */
	String getLoginName();

	void setLoginName(String loginName);

	String getSerialNumber();

	void setSerialNumber(String serialNumber);

	/*
	 * Actions.
	 */
	String register();

	/*
	 * Factories
	 */

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
