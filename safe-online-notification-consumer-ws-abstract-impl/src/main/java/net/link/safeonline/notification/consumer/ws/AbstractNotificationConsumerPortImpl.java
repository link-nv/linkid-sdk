/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.consumer.ws;

import net.lin_k.safe_online.notification.consumer.NotificationConsumerPort;


public abstract class AbstractNotificationConsumerPortImpl implements NotificationConsumerPort {

    protected AbstractNotificationConsumerService notificationConsumerService;


    public AbstractNotificationConsumerPortImpl(AbstractNotificationConsumerService notificationConsumerService) {

        this.notificationConsumerService = notificationConsumerService;
    }

}
