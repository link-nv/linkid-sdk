/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.notification.consumer.ws;

import java.util.List;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import net.lin_k.safe_online.notification.consumer.NotificationConsumerPort;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType;
import net.lin_k.safe_online.notification.consumer.Notify;
import net.link.safeonline.demo.model.NotificationConsumerService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


@WebService(endpointInterface = "net.lin_k.safe_online.notification.consumer.NotificationConsumerPort")
@HandlerChain(file = "demo-ws-handlers.xml")
public class NotificationConsumerPortImpl implements NotificationConsumerPort {

    private final static Log            LOG = LogFactory.getLog(NotificationConsumerPortImpl.class);

    private NotificationConsumerService notificationConsumerService;


    public void notify(Notify notify) {

        LOG.debug("demo received notification");

        loadDependencies();

        List<NotificationMessageHolderType> notifications = notify.getNotificationMessage();
        for (NotificationMessageHolderType notification : notifications) {
            TopicExpressionType topicExpression = notification.getTopic();
            List<Object> topics = topicExpression.getContent();
            for (Object topic : topics) {
                String topicString = (String) topic;
                LOG.debug("topic: " + topicString + " destination: " + notification.getMessage().getDestination());
                this.notificationConsumerService.handleMessage(topicString, notification.getMessage().getDestination(),
                        notification.getMessage().getSubject(), notification.getMessage().getContent());
            }
        }
    }

    private void loadDependencies() {

        this.notificationConsumerService = EjbUtils.getEJB(NotificationConsumerService.JNDI_BINDING,
                NotificationConsumerService.class);
    }

}
