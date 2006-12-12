/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

@Local
public interface Password {

	String getNewPassword();

	void setNewPassword(String newPassword);

	String getOldPassword();

	void setOldPassword(String oldPassword);

	String change();

	void destroyCallback();
}
