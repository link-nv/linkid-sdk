/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.user;

public class UserRemove extends UserTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_USER_WEBAPP_PREFIX
			+ "/remove.seam";

	public UserRemove() {
		super(PAGE_NAME);
	}

	public UserMain remove() {
		clickButtonAndWait("remove");
		checkTextPresent("Login");
		return new UserMain();
	}

	public UserAccount cancel() {
		clickButtonAndWait("cancel");
		return new UserAccount();
	}
}
