/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.auth;

import net.link.safeonline.webapp.Page;


public class AuthSubscription extends Page {

    public static final String PAGE_NAME = SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/subscription.seam";


    public AuthSubscription() {

        super(PAGE_NAME);
    }

    public AuthIdentityConfirmation confirm() {

        clickButtonAndWait("confirm");
        return new AuthIdentityConfirmation();
    }

    public AuthIdentityConfirmation subscribe() {

        clickButtonAndWait("subscribe");
        return new AuthIdentityConfirmation();
    }

    public AuthSubscriptionRejection reject() {

        clickButtonAndWait("reject");
        return new AuthSubscriptionRejection();
    }
}
