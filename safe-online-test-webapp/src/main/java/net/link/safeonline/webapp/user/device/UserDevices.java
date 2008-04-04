/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.user.device;

import net.link.safeonline.webapp.user.UserTemplate;
import junit.framework.Assert;

import com.thoughtworks.selenium.SeleniumException;

public class UserDevices extends UserTemplate {

	public static final String PAGE_NAME = SAFE_ONLINE_USER_WEBAPP_PREFIX
			+ "/device/devices.seam";

	private static final String BEID = "Belgian eID";

	public UserDevices() {
		super(PAGE_NAME);
	}

	public void registerBeId() {
		clickLinkInRowAndWait("devicesTable", BEID, "register");

		// BeId registration done manually, we wait till its done
		waitForRedirect(PAGE_NAME);
		Assert.assertTrue(checkLinkInRow("deviceRegistrationsTable", BEID,
				"remove"));
		Assert.assertTrue(checkLinkInRow("deviceRegistrationsTable", BEID,
				"update"));
	}

	public void removeBeId() {
		clickLinkInRowAndWait("deviceRegistrationsTable", BEID, "remove");

		waitForRedirect(PAGE_NAME);
		try {
			checkLinkInRow("deviceRegistrationsTable", BEID, "remove");
		} catch (SeleniumException e) {
			return;
		}
		Assert.fail();
	}
}
