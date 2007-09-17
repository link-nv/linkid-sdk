/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.service.AttributeDO;

@Local
public interface Devices {

	/*
	 * Accessors.
	 */
	String getNewPassword();

	void setNewPassword(String newPassword);

	String getOldPassword();

	void setOldPassword(String oldPassword);

	boolean isPasswordConfigured();

	/*
	 * Actions.
	 */
	String changePassword();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factory.
	 */
	List<AttributeDO> beidAttributesFactory();
}
