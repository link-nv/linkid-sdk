/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.consumer;

import java.util.List;

import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

/**
 * WS Notification Producer Service Client interface.
 * 
 * @author wvdhaute
 * 
 */
public interface NotificationConsumerClient extends MessageAccessor {

	void sendNotification(String topic, String destination, List<String> message)
			throws WSClientTransportException;
}
