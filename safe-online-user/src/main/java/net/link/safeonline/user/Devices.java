/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.data.AttributeDO;

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

	String getMobile();

	void setMobile(String mobile);

	String getMobileActivationCode();

	/*
	 * Actions.
	 */
	String changePassword();

	String mobileRegister();

	String mobileActivationOk();

	String mobileActivationCancel();

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Factory.
	 */
	List<AttributeDO> beidAttributesFactory();

	List<AttributeDO> mobileWeakAttributesFactory();

	List<AttributeDO> mobileStrongAttributesFactory();
}
