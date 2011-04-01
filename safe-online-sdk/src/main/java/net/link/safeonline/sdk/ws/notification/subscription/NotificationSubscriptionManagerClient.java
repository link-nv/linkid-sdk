/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.subscription;

import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.SubscriptionNotFoundException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;


/**
 * WS Notification Subscription Manager Service Client interface.
 *
 * @author wvdhaute
 */
public interface NotificationSubscriptionManagerClient {

    /**
     * Unsubscribe the specified consumer address to the specified Topic.
     *
     * @throws SubscriptionNotFoundException
     * @throws RequestDeniedException
     * @throws WSClientTransportException
     */
    void unsubscribe(String topic, String address)
            throws SubscriptionNotFoundException, RequestDeniedException, WSClientTransportException;
}
