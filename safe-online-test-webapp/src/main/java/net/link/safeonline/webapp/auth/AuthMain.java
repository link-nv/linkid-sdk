/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.auth;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.webapp.Page;
import net.link.safeonline.webapp.PageUtils;
import net.link.safeonline.webapp.auth.password.AuthUserNamePassword;


public class AuthMain extends Page {

    public final static String PAGE_NAME = SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/main.seam";

    private String             device;


    public AuthMain() {

        super(PAGE_NAME);
    }

    public void selectDevice(String deviceName) {

        this.device = deviceName;
        clickRadioButton(deviceName);
    }

    /**
     * Result of this action has following possibilities :
     * <ul>
     * <li>Password device: {@link AuthUserNamePassword}</li>
     * <li>BeID device : null, this can have the need for manual intervention, you need to use
     * {@link PageUtils#waitForRedirect(net.link.safeonline.webapp.AcceptanceTestManager, String)}</li>
     * <li>Encap device:</li>
     * </ul>
     */
    public Page next() {

        clickButtonAndWait("next");
        if (this.device.equals(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID))
            return new AuthUserNamePassword();
        else if (this.device.equals(BeIdConstants.BEID_DEVICE_ID))
            return null;
        else if (this.device.equals(EncapConstants.ENCAP_DEVICE_ID))
            return null;
        else if (this.device.equals(DigipassConstants.DIGIPASS_DEVICE_ID))
            return null;
        return null;
    }

    public AuthNewUser newUser() {

        clickButtonAndWait("new-user");
        return new AuthNewUser();
    }

}
