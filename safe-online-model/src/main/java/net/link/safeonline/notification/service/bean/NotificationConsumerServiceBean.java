/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.SafeOnlineNodeRoles;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.notification.message.MessageHandlerManager;
import net.link.safeonline.notification.service.NotificationConsumerService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_NODE_SECURITY_DOMAIN)
public class NotificationConsumerServiceBean implements NotificationConsumerService {

    private static final Log LOG = LogFactory.getLog(NotificationConsumerServiceBean.class);


    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public void handleMessage(String topic, String destination, String subject, String content) {

        LOG.debug("handle message for topic: " + topic);
        try {
            MessageHandlerManager.handleMessage(topic, destination, subject, content);
        } catch (MessageHandlerNotFoundException e) {
            LOG.debug("Exception: " + e.getMessage());
            return;
        }
    }
}
