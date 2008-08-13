/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.payment;

import net.link.safeonline.webapp.Page;


public class DemoPaymentCards extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX + "/cards.seam";


    public DemoPaymentCards() {

        super(PAGE_NAME);
    }

    public DemoPaymentCompleted confirm() {

        clickButtonAndWait("confirm");
        return new DemoPaymentCompleted();
    }

    public void cancel() {

        clickButtonAndWait("cancel");
    }

}
