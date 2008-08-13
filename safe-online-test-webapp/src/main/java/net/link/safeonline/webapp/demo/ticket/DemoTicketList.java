/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.ticket;

import junit.framework.Assert;
import net.link.safeonline.webapp.Page;


public class DemoTicketList extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX + "/list.seam";


    public DemoTicketList() {

        super(PAGE_NAME);
    }

    public void checkTicketPresent(String text) {

        Assert.assertTrue(Page.getSelenium().isTextPresent(text));
    }

}
