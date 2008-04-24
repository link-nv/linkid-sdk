/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.consumer.ws;

import java.util.List;

import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebService;

import net.lin_k.safe_online.notification.consumer.NotificationConsumerPort;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType;
import net.lin_k.safe_online.notification.consumer.Notify;
import net.link.safeonline.notification.service.NotificationConsumerService;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

@WebService(endpointInterface = "net.lin_k.safe_online.notification.consumer.NotificationConsumerPort")
@HandlerChain(file = "auth-ws-handlers.xml")
@Injection
public class NotificationConsumerPortImpl implements NotificationConsumerPort {

	private final static Log LOG = LogFactory
			.getLog(NotificationConsumerPortImpl.class);

	@EJB(mappedName = "SafeOnline/NotificationConsumerServiceBean/local")
	private NotificationConsumerService notificationConsumerService;

	public void notify(Notify notify) {
		LOG.debug("received notification");
		List<NotificationMessageHolderType> notifications = notify
				.getNotificationMessage();
		for (NotificationMessageHolderType notification : notifications) {
			TopicExpressionType topicExpression = notification.getTopic();
			List<Object> topics = topicExpression.getContent();
			for (Object topic : topics) {
				String topicString = (String) topic;
				LOG.debug("topic: " + topicString + " destination: "
						+ notification.getMessage().getDestination());
				this.notificationConsumerService.handleMessage(topicString,
						notification.getMessage().getDestination(),
						notification.getMessage().getContent());
			}
		}
	}

}
