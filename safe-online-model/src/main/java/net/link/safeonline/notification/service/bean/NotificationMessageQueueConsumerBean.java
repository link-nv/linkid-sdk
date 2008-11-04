/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.notification.service.bean;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationMessageEntity;
import net.link.safeonline.notification.dao.EndpointReferenceDAO;
import net.link.safeonline.notification.dao.NotificationMessageDAO;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.notification.message.MessageHandlerManager;
import net.link.safeonline.notification.message.NotificationConstants;
import net.link.safeonline.notification.message.NotificationMessage;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link NotificationMessageQueueConsumerBean}<br>
 * <sub>MDB that handles the notifications queue.</sub></h2>
 * 
 * <p>
 * Handles the notification messages on the notification message queue and tries to send them to the specified consumer.
 * </p>
 * 
 * <p>
 * <i>Oct 28, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = NotificationConstants.NOTIFICATIONS_QUEUE_NAME),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "2") })
public class NotificationMessageQueueConsumerBean implements MessageListener {

    private static final Log       LOG = LogFactory.getLog(NotificationMessageQueueConsumerBean.class);

    @EJB
    private NotificationMessageDAO notificationMessageDAO;

    @EJB
    private EndpointReferenceDAO   endpointReferenceDAO;

    @EJB
    private ResourceAuditLogger    resourceAuditLogger;

    @EJB
    private SecurityAuditLogger    securityAuditLogger;


    /**
     * {@inheritDoc}
     */
    public void onMessage(Message message) {

        NotificationMessage notificationMessage;
        try {
            notificationMessage = new NotificationMessage(message);
        } catch (JMSException e) {
            LOG.debug("received bogus JMS message, rejecting ...");
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "bogus notification message on notifications queue");
            return;
        }

        EndpointReferenceEntity consumer = this.endpointReferenceDAO.findEndpointReference(notificationMessage.getConsumerId());
        try {
            MessageHandlerManager.sendMessage(notificationMessage, consumer);
        } catch (WSClientTransportException e) {
            String msg = "Failed to send messsage for topic " + notificationMessage.getTopic() + " to consumer: " + e.getLocation();
            LOG.debug(msg);
            this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e.getLocation(), msg);
            this.notificationMessageDAO.addNotificationAttempt(notificationMessage, consumer);
            /*
             * NotificationMessageEntity notificationMessageEntity = this.notificationMessageDAO.findNotificationMessage(
             * notificationMessage, consumer); if (null == notificationMessageEntity) {
             * this.notificationMessageDAO.addNotificationMessage(notificationMessage, consumer); } else { LOG.debug("attempts: " +
             * notificationMessageEntity.getAttempts()); notificationMessageEntity.setAttempts(notificationMessageEntity.getAttempts() + 1);
             * }
             */
            return;
        } catch (MessageHandlerNotFoundException e) {
            String msg = "no message handler found for notification message on notifications queue, topic="
                    + notificationMessage.getTopic();
            LOG.debug(msg);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, msg);
            return;
        }

        /*
         * If persisted, remove so we do not send it again
         */
        NotificationMessageEntity notificationMessageEntity = this.notificationMessageDAO.findNotificationMessage(notificationMessage,
                consumer);
        if (null != notificationMessageEntity) {
            this.notificationMessageDAO.removeNotificationMessage(notificationMessageEntity);
        }

    }
}
