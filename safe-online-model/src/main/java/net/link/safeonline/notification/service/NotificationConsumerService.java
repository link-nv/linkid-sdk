/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.notification.consumer.ws.AbstractNotificationConsumerService;


@Local
public interface NotificationConsumerService extends SafeOnlineService, AbstractNotificationConsumerService {

    public static final String JNDI_BINDING = JNDI_PREFIX + "NotificationConsumerServiceBean/local";
}
