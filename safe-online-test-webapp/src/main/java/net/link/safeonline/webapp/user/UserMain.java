/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.user;

import net.link.safeonline.webapp.Page;
import net.link.safeonline.webapp.auth.AuthFirstTime;


public class UserMain extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_USER_WEBAPP_PREFIX + "/main.seam";


    public UserMain() {

        super(PAGE_NAME);
    }

    @Override
    public AuthFirstTime loginFirstTime() {

        clickLink("login");
        waitForRedirect(AuthFirstTime.PAGE_NAME);
        return new AuthFirstTime();
    }
}
