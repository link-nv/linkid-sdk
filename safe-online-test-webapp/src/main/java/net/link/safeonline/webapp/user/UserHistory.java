/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.user;

import junit.framework.Assert;
import net.link.safeonline.webapp.Page;


public class UserHistory extends UserTemplate {

    public static final String PAGE_NAME = SAFE_ONLINE_USER_WEBAPP_PREFIX + "/history.seam";


    public UserHistory() {

        super(PAGE_NAME);
    }

    public void checkHistoryPasswordLogon() {

        Assert.assertTrue(Page.getSelenium().isTextPresent(
                "Logged in successfully into application 'olas-user' using device 'password'."));
    }
}
