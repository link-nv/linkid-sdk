/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.demo.payment;

import net.link.safeonline.webapp.Page;


public class DemoPaymentSearchResult extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX + "/search-result.seam";


    public DemoPaymentSearchResult() {

        super(PAGE_NAME);
    }

    public void setJunior(boolean junior) {

        setCheckBox("junior", junior);
    }

    public boolean getJunior() {

        return isCheckedCheckBox("junior");
    }

    public void setPaymentAdmin(boolean admin) {

        setCheckBox("paymentadmin", admin);
    }

    public boolean getPaymentAdmin() {

        return isCheckedCheckBox("paymentadmin");
    }

    public void save() {

        clickButtonAndWait("save");
    }

    public DemoPaymentSearch back() {

        clickButtonAndWait("back");
        return new DemoPaymentSearch();
    }
}
