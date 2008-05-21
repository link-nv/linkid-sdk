/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.data.AttributeDO;

@Local
public interface Removal {

	/*
	 * Accessors
	 */
	String getLoginName();

	void setLoginName(String loginName);

	/*
	 * Actions.
	 */
	String getRegistrations();

	String remove();

	/*
	 * Factories
	 */
	List<AttributeDO> digipassAttributesFactory();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();
}
