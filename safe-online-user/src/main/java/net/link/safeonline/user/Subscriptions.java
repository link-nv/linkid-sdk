/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;


@Local
public interface Subscriptions {

    void subscriptionListFactory();

    String viewSubscription() throws SubscriptionNotFoundException, ApplicationNotFoundException, ApplicationIdentityNotFoundException;

    String unsubscribe() throws SubscriptionNotFoundException, ApplicationNotFoundException;

    String getUsageAgreement() throws ApplicationNotFoundException;

    String getGlobalUsageAgreement();

    void destroyCallback();
}
