/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;


@Local
public interface NotificationProducerDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "NotificationProducerDAOBean/local";


    NotificationProducerSubscriptionEntity addSubscription(String topic);

    NotificationProducerSubscriptionEntity findSubscription(String topic);

    NotificationProducerSubscriptionEntity getSubscription(String topic)
            throws SubscriptionNotFoundException;

    List<NotificationProducerSubscriptionEntity> listTopics();
}
