/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.user;

import net.link.safeonline.webapp.Page;
import net.link.safeonline.webapp.user.device.UserDevices;
import net.link.safeonline.webapp.user.profile.UserProfile;

public abstract class UserTemplate extends Page {

	public UserTemplate(String page) {
		super(page);
	}

	public UserOverview gotoHome() {
		clickLinkAndWait("page_home_link");
		return new UserOverview();
	}

	public UserProfile gotoProfile() {
		clickLinkAndWait("page_profile_link");
		return new UserProfile();
	}

	public UserApplications gotoApplications() {
		clickLinkAndWait("page_applications_link");
		return new UserApplications();
	}

	public UserDevices gotoDevices() {
		clickLinkAndWait("page_devices_link");
		return new UserDevices();
	}

	public UserAccount gotoAccount() {
		clickLinkAndWait("page_account_link");
		return new UserAccount();
	}

}
