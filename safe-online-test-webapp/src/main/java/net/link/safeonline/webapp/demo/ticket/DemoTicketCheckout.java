/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.ticket;

import net.link.safeonline.webapp.Page;
import net.link.safeonline.webapp.demo.payment.DemoPaymentEntry;


public class DemoTicketCheckout extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX + "/checkout.seam";


    public DemoTicketCheckout() {

        super(PAGE_NAME);
    }

    public DemoPaymentEntry confirm() {

        clickButtonAndWait("confirm");
        waitForRedirect(DemoPaymentEntry.PAGE_NAME);
        return new DemoPaymentEntry();

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
