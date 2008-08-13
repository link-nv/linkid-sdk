/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;


@Local
public interface AuthenticationSubscription {

    /*
     * Actions.
     */
    String subscribe() throws ApplicationNotFoundException, AlreadySubscribedException, PermissionDeniedException,
            SubscriptionNotFoundException, ApplicationIdentityNotFoundException, AttributeTypeNotFoundException;

    /*
     * Accessors
     */
    String getUsageAgreement();
}
