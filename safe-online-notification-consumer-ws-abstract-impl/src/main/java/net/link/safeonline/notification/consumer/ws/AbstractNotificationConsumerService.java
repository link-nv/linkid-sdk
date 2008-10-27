/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.consumer.ws;



/**
 * Abstract WS-Notification consumer service interface to be used for specific implementations.
 *
 * @author wvdhaute
 *
 */
public interface AbstractNotificationConsumerService {

    void handleMessage(String topic, String destination, String subject, String content);

}
