/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.auth;

import net.link.safeonline.webapp.Page;


public class AuthMissingAttributes extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/missing-attributes.seam";


    public AuthMissingAttributes() {

        super(PAGE_NAME);
    }

    public void setAttributeValue(String label, String value) {

        fillInputFieldInRepeat(label, "value", value);
    }

    public void save() {

        clickButtonAndWait("save");
    }

}
