/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.payment;

import net.link.safeonline.webapp.Page;
import net.link.safeonline.webapp.demo.ticket.DemoTicketList;


public class DemoPaymentCompleted extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX + "/completed.seam";


    public DemoPaymentCompleted() {

        super(PAGE_NAME);
    }

    public DemoTicketList done() {

        clickButtonAndWait("continue");
        waitForRedirect(DemoTicketList.PAGE_NAME);
        return new DemoTicketList();
    }

}
