/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap;

import javax.ejb.Local;

@Local
public interface Registration {

	/*
	 * Accessors.
	 */
	String getMobile();

	void setMobile(String mobile);

	String getMobileActivationCode();

	/*
	 * Actions.
	 */
	String mobileRegister();

	String mobileActivationOk();

	String mobileActivationCancel();

	String mobileExit();

	/*
	 * Factories
	 */

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
