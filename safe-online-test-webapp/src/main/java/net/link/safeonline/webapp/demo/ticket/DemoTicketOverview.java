/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.ticket;

import net.link.safeonline.webapp.Page;
import junit.framework.Assert;

public class DemoTicketOverview extends Page {

	public static final String PAGE_NAME = SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX
			+ "/overview.seam";

	public DemoTicketOverview() {
		super(PAGE_NAME);
	}

	public DemoTicketList list() {
		clickLinkAndWait("list");
		return new DemoTicketList();
	}

	public DemoTicketAdd add() {
		clickLinkAndWait("add");
		return new DemoTicketAdd();
	}

	public void remove() {
		clickButtonAndWait("remove");
	}

	public void checkLoggedIn(String login) {
		Assert.assertTrue(checkTextPresent("Welcome " + login));
	}

}
