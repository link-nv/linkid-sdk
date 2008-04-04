/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.ticket;

import net.link.safeonline.webapp.Page;

public class DemoTicketAdd extends Page {

	public static final String PAGE_NAME = SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX
			+ "/add.seam";

	public DemoTicketAdd() {
		super(PAGE_NAME);
	}

	public DemoTicketCheckout checkout() {
		clickButtonAndWait("checkout");
		return new DemoTicketCheckout();
	}

	public DemoTicketList cancel() {
		clickButtonAndWait("cancel");
		return new DemoTicketList();
	}

	public DemoTicketOverview overview() {
		clickButtonAndWait("overview");
		return new DemoTicketOverview();
	}

}
