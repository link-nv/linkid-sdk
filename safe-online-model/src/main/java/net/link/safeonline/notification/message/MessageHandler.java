/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.message;

import java.util.List;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;


/**
 * Interface for WS-Notification messages.
 * 
 * @author wvdhaute
 * 
 */
public interface MessageHandler {

    void init();

    List<String> createApplicationMessage(List<String> message, ApplicationEntity application);

    List<String> createNodeMessage(List<String> message, NodeEntity node);

    void handleMessage(String destination, List<String> message);
}
