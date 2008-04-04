/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.user.device.password;

import net.link.safeonline.webapp.user.UserTemplate;

public class UserConfirmPassword extends UserTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_USER_WEBAPP_PREFIX
			+ "/device/password/confirm-password.seam";

	public UserConfirmPassword() {
		super(PAGE_NAME);
	}
}
