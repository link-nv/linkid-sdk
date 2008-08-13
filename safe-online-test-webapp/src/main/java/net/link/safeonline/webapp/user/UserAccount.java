/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.user;

public class UserAccount extends UserTemplate {

    public static final String PAGE_NAME = SAFE_ONLINE_USER_WEBAPP_PREFIX + "/account.seam";


    public UserAccount() {

        super(PAGE_NAME);
    }

    public UserHistory gotoHistory() {

        clickLinkAndWait("history");
        return new UserHistory();
    }

    public UserUsage gotoUsage() {

        clickLinkAndWait("usage");
        return new UserUsage();
    }

    public UserRemove gotoRemove() {

        clickLinkAndWait("remove");
        return new UserRemove();
    }

}
