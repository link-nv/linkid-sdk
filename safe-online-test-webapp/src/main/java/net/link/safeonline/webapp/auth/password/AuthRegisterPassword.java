/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.auth.password;

import net.link.safeonline.webapp.Page;


public class AuthRegisterPassword extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/password/register-password.seam";


    public AuthRegisterPassword() {

        super(PAGE_NAME);
    }

    public void setPassword1(String password) {

        fillInputField("password1", password);
    }

    public void setPassword2(String password) {

        fillInputField("password2", password);
    }

    public void register() {

        clickButton("change");
        waitForRedirect("overview.seam");
    }
}
