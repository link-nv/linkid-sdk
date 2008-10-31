/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.message;

import net.link.safeonline.entity.notification.EndpointReferenceEntity;


/**
 * Interface for WS-Notification messages.
 * 
 * @author wvdhaute
 * 
 */
public interface MessageHandler {

    void init();

    void handleMessage(String destination, String subject, String content);

    NotificationMessage createMessage(String topic, String subject, String content, EndpointReferenceEntity consumer);
}