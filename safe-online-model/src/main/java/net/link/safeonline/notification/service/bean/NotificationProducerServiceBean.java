/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.service.bean;

import java.security.cert.X509Certificate;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.NodeDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationMessageEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.notification.dao.EndpointReferenceDAO;
import net.link.safeonline.notification.dao.NotificationMessageDAO;
import net.link.safeonline.notification.dao.NotificationProducerDAO;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.notification.message.MessageHandlerManager;
import net.link.safeonline.notification.message.NotificationConstants;
import net.link.safeonline.notification.message.NotificationMessage;
import net.link.safeonline.notification.service.NotificationProducerService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Service bean used by the WS-Notification producer service.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@Interceptors( { AuditContextManager.class })
public class NotificationProducerServiceBean implements NotificationProducerService {

    private static final Log        LOG = LogFactory.getLog(NotificationProducerServiceBean.class);

    @Resource(mappedName = NotificationConstants.CONNECTION_FACTORY_NAME)
    private ConnectionFactory       factory;

    @Resource(mappedName = NotificationConstants.NOTIFICATIONS_QUEUE_NAME)
    private Queue                   notificationsQueue;

    @EJB
    private NotificationProducerDAO notificationProducerDAO;

    @EJB
    private ApplicationDAO          applicationDAO;

    @EJB
    private NodeDAO                 nodeDAO;

    @EJB
    private EndpointReferenceDAO    endpointReferenceDAO;

    @EJB
    private NotificationMessageDAO  notificationMessageDAO;


    public void subscribe(String topic, String address, X509Certificate certificate) throws PermissionDeniedException {

        LOG.debug("subscribe");

        ApplicationEntity application = this.applicationDAO.findApplication(certificate);
        if (null != application) {
            subscribe(topic, address, application);
        } else {
            NodeEntity node = this.nodeDAO.findNodeFromAuthnCertificate(certificate);
            if (null != node) {
                subscribe(topic, address, node);
            } else
                throw new PermissionDeniedException("application or node not found.");
        }
    }

    public void subscribe(String topic, String address, NodeEntity node) {

        LOG.debug("subscribe node " + node.getName() + " to topic: " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.findSubscription(topic);
        if (null == subscription) {
            subscription = this.notificationProducerDAO.addSubscription(topic);
        }

        EndpointReferenceEntity endpointReference = this.endpointReferenceDAO.findEndpointReference(address, node);
        if (null == endpointReference) {
            endpointReference = this.endpointReferenceDAO.addEndpointReference(address, node);
        }
        subscription.getConsumers().add(endpointReference);
    }

    public void subscribe(String topic, String address, ApplicationEntity application) {

        LOG.debug("subscribe application " + application.getName() + " to topic: " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.findSubscription(topic);
        if (null == subscription) {
            subscription = this.notificationProducerDAO.addSubscription(topic);
        }

        EndpointReferenceEntity endpointReference = this.endpointReferenceDAO.findEndpointReference(address,
                application);
        if (null == endpointReference) {
            endpointReference = this.endpointReferenceDAO.addEndpointReference(address, application);
        }
        subscription.getConsumers().add(endpointReference);
    }

    public void unsubscribe(String topic, String address, X509Certificate certificate)
            throws SubscriptionNotFoundException, PermissionDeniedException, EndpointReferenceNotFoundException {

        LOG.debug("unsubscribe");
        ApplicationEntity application = this.applicationDAO.findApplication(certificate);
        if (null != application) {
            unsubscribe(topic, address, application);
        } else {
            NodeEntity node = this.nodeDAO.findNodeFromAuthnCertificate(certificate);
            if (null != node) {
                unsubscribe(topic, address, node);
            } else
                throw new PermissionDeniedException("application or node not found.");
        }
    }

    public void unsubscribe(String topic, String address, NodeEntity node) throws SubscriptionNotFoundException,
            EndpointReferenceNotFoundException {

        LOG.debug("unsubscribe node " + node.getName() + " from topic " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.getSubscription(topic);

        EndpointReferenceEntity endpointReference = this.endpointReferenceDAO.getEndpointReference(address, node);
        subscription.getConsumers().remove(endpointReference);
    }

    public void unsubscribe(String topic, String address, ApplicationEntity application)
            throws SubscriptionNotFoundException, EndpointReferenceNotFoundException {

        LOG.debug("unsubscribe application " + application.getName() + " from topic " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.getSubscription(topic);

        EndpointReferenceEntity endpointReference = this.endpointReferenceDAO
                .getEndpointReference(address, application);
        subscription.getConsumers().remove(endpointReference);
    }

    public void sendNotification(String topic, String subject, String content) throws MessageHandlerNotFoundException {

        LOG.debug("send notification for topic: " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.findSubscription(topic);
        if (null == subscription) {
            LOG.debug("no subscriptions found for topic: " + topic);
            return;
        }
        for (EndpointReferenceEntity consumer : subscription.getConsumers()) {
            NotificationMessage message = MessageHandlerManager.getMessage(topic, subject, content, consumer);
            if (null != message) {
                pushMessage(message, consumer);
            }
        }
    }

    public void sendNotification(NotificationMessageEntity notification) throws MessageHandlerNotFoundException {

        LOG.debug("send persisted notification for topic: " + notification.getTopic() + " subject: "
                + notification.getSubject() + " content: " + notification.getContent() + " consumerId: "
                + notification.getConsumer().getId());
        NotificationMessage message = MessageHandlerManager.getMessage(notification.getTopic(), notification
                .getSubject(), notification.getContent(), notification.getConsumer());
        if (null != message) {
            pushMessage(message, notification.getConsumer());
        }

    }

    private void pushMessage(NotificationMessage notificationMessage, EndpointReferenceEntity consumer) {

        LOG.debug("push notification message");
        try {
            Connection connection = this.factory.createConnection();
            try {
                Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                try {
                    MessageProducer producer = session.createProducer(this.notificationsQueue);
                    try {
                        Message message = notificationMessage.getJMSMessage(session);
                        producer.send(message);
                    } finally {
                        producer.close();
                    }
                } finally {
                    session.close();
                }
            } finally {
                connection.close();
            }
        } catch (JMSException e) {
            LOG.debug("Failed to push notification message on JMS queue: " + e.getMessage());
            NotificationMessageEntity notificationMessageEntity = this.notificationMessageDAO.findNotificationMessage(
                    notificationMessage, consumer);
            if (null == notificationMessageEntity) {
                this.notificationMessageDAO.addNotificationMessage(notificationMessage, consumer);
            } else {
                notificationMessageEntity.addAttempt();
            }
        }
    }

}