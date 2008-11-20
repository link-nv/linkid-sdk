/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.auth;

import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.webapp.Page;
import net.link.safeonline.webapp.auth.password.AuthRegisterPassword;


public class AuthNewUserDevice extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/new-user-device.seam";

    private String             device;


    public AuthNewUserDevice() {

        super(PAGE_NAME);
    }

    public void selectDevice(String deviceName) {

        this.device = deviceName;
        clickRadioButton(this.device);
    }

    public Page next() {

        clickButtonAndWait("next");
        if (this.device.equals(PasswordConstants.PASSWORD_DEVICE_ID))
            return new AuthRegisterPassword();
        return null;
    }
}
