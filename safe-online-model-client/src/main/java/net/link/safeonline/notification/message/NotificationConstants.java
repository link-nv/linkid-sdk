/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.message;

public interface NotificationConstants {

    /**
     * The notification queue.
     */
    public final static String NOTIFICATIONS_QUEUE_NAME = "queue/notifications";

    /**
     * The name of the connection factory used for publishing JMS messages.
     */
    public final static String CONNECTION_FACTORY_NAME  = "java:/JmsXA";
}
