/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.auth.password;

import net.link.safeonline.webapp.Page;

public class AuthUserNamePassword extends Page {

	public final static String PAGE_NAME = SAFE_ONLINE_AUTH_WEBAPP_PREFIX
			+ "/password/username-password.seam";

	public AuthUserNamePassword() {
		super(PAGE_NAME);
	}

	public void setLogin(String login) {
		fillInputField(":username", login);
	}

	public void setPassword(String password) {
		fillInputField(":password", password);
	}

	public void logon() {
		clickButtonAndWait(":login");
	}

}
