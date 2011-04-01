/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.producer;

import net.link.safeonline.sdk.logging.exception.SubscriptionFailedException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;


/**
 * WS Notification Producer Service Client interface.
 *
 * @author wvdhaute
 */
public interface NotificationProducerClient {

    /**
     * Subscribe the specified consumer address to the specified Topic.
     *
     * @throws SubscriptionFailedException
     * @throws WSClientTransportException
     */
    void subscribe(String topic, String address)
            throws SubscriptionFailedException, WSClientTransportException;
}
