/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.payment;

import net.link.safeonline.webapp.Page;


public class DemoPaymentSearch extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX + "/search.seam";


    public DemoPaymentSearch() {

        super(PAGE_NAME);
    }

    public void setName(String name) {

        fillInputField("name", name);
    }

    public DemoPaymentSearchResult search() {

        clickButtonAndWait("search");
        return new DemoPaymentSearchResult();
    }
}
