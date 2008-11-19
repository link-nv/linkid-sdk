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
import net.link.safeonline.authentication.exception.NotificationMessageNotFoundException;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationMessageEntity;
import net.link.safeonline.notification.message.NotificationMessage;


@Local
public interface NotificationMessageDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "NotificationMessageDAOBean/local";


    NotificationMessageEntity addNotificationMessage(NotificationMessage notificationMessage, EndpointReferenceEntity consumer);

    NotificationMessageEntity findNotificationMessage(NotificationMessage notificationMessage, EndpointReferenceEntity consumer);

    NotificationMessageEntity getNotificationMessage(NotificationMessage notificationMessage, EndpointReferenceEntity consumer)
            throws NotificationMessageNotFoundException;

    void removeNotificationMessage(NotificationMessageEntity notificationMessageEntity);

    List<NotificationMessageEntity> listNotificationMessages();

    void addNotificationAttempt(NotificationMessage message, EndpointReferenceEntity consumer);
}
