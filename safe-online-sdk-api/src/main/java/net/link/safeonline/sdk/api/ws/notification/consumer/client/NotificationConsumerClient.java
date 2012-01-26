/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.notification.consumer.client;

import net.link.safeonline.sdk.api.exception.WSClientTransportException;


/**
 * WS Notification Producer Service Client interface.
 *
 * @author wvdhaute
 */
public interface NotificationConsumerClient {

    void sendNotification(String topic, String destination, String subject, String content)
            throws WSClientTransportException;
}
