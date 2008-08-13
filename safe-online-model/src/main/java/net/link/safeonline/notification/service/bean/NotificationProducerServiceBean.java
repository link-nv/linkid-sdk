/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.service.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.notification.dao.EndpointReferenceDAO;
import net.link.safeonline.notification.dao.NotificationProducerDAO;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.notification.message.MessageHandlerManager;
import net.link.safeonline.notification.service.NotificationProducerService;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

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

    @EJB
    private NotificationProducerDAO notificationProducerDAO;

    @EJB
    private ApplicationDAO          applicationDAO;

    @EJB
    private DeviceDAO               deviceDAO;

    @EJB
    private EndpointReferenceDAO    endpointReferenceDAO;

    @EJB
    private ResourceAuditLogger     resourceAuditLogger;


    public void subscribe(String topic, String address, X509Certificate certificate) throws PermissionDeniedException {

        LOG.debug("subscribe");

        ApplicationEntity application = this.applicationDAO.findApplication(certificate);
        if (null != application) {
            subscribe(topic, address, application);
        } else {
            DeviceEntity device = this.deviceDAO.findDevice(certificate);
            if (null != device) {
                subscribe(topic, address, device);
            } else
                throw new PermissionDeniedException("application or device not found.");
        }
    }

    public void subscribe(String topic, String address, DeviceEntity device) {

        LOG.debug("subscribe device " + device.getName() + " to topic: " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.findSubscription(topic);
        if (null == subscription) {
            subscription = this.notificationProducerDAO.addSubscription(topic);
        }

        EndpointReferenceEntity endpointReference = this.endpointReferenceDAO.findEndpointReference(address, device);
        if (null == endpointReference) {
            endpointReference = this.endpointReferenceDAO.addEndpointReference(address, device);
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
            DeviceEntity device = this.deviceDAO.findDevice(certificate);
            if (null != device) {
                unsubscribe(topic, address, device);
            } else
                throw new PermissionDeniedException("application or device not found.");
        }
    }

    public void unsubscribe(String topic, String address, DeviceEntity device) throws SubscriptionNotFoundException,
            EndpointReferenceNotFoundException {

        LOG.debug("unsubscribe device " + device.getName() + " from topic " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.getSubscription(topic);

        EndpointReferenceEntity endpointReference = this.endpointReferenceDAO.getEndpointReference(address, device);
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

    public void sendNotification(String topic, List<String> message) throws SubscriptionNotFoundException,
            MessageHandlerNotFoundException {

        LOG.debug("send notification for topic: " + topic);
        NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO.findSubscription(topic);
        if (null == subscription) {
            LOG.debug("no subscriptions found for topic: " + topic);
            return;
        }
        for (EndpointReferenceEntity consumer : subscription.getConsumers()) {
            try {
                MessageHandlerManager.sendMessage(topic, message, consumer);
            } catch (WSClientTransportException e) {
                LOG.debug("Failed to send messsage for topic " + topic + " to consumer: " + e.getLocation());
                this.resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE,
                        e.getLocation(), "Failed to send notification for topic " + topic);
            }
        }
    }
}
