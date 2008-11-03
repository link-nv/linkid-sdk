/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

import net.link.safeonline.user.UserConstants;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;


@Local
public interface Actions {

    public static final String JNDI_BINDING = UserConstants.JNDI_PREFIX + "ActionsBean/local";

    /*
     * Actions.
     */
    String removeAccount() throws SubscriptionNotFoundException, MessageHandlerNotFoundException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
