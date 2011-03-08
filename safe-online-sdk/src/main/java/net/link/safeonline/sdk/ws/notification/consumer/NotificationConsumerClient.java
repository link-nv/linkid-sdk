/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.consumer;

import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.WSClient;


/**
 * WS Notification Producer Service Client interface.
 *
 * @author wvdhaute
 */
public interface NotificationConsumerClient extends WSClient {

    void sendNotification(String topic, String destination, String subject, String content)
            throws WSClientTransportException;
}
