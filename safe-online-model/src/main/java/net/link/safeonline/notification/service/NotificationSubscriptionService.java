/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.service;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;


@Local
public interface NotificationSubscriptionService {

    List<NotificationProducerSubscriptionEntity> listTopics();

    void removeSubscription(String topic, EndpointReferenceEntity subscription) throws SubscriptionNotFoundException,
            EndpointReferenceNotFoundException, PermissionDeniedException;

    Set<EndpointReferenceEntity> listSubscriptions(String topic) throws SubscriptionNotFoundException;

    void addSubscription(String topic, String address, String consumer) throws PermissionDeniedException;
}
