/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.user.profile;

import net.link.safeonline.webapp.user.UserTemplate;

public class UserProfile extends UserTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_USER_WEBAPP_PREFIX
			+ "/profile/profile.seam";

	public UserProfile() {
		super(PAGE_NAME);
	}

	public String getAttributeValue(String attribute) {
		return this.getSafeOnlineAttributeValue(attribute);
	}

}
