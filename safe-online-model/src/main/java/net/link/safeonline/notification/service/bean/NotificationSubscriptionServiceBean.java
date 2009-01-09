/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.service.bean;

import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.NodeDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.notification.dao.NotificationProducerDAO;
import net.link.safeonline.notification.service.NotificationProducerService;
import net.link.safeonline.notification.service.NotificationSubscriptionService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = NotificationSubscriptionService.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class NotificationSubscriptionServiceBean implements NotificationSubscriptionService {

    private static final Log            LOG = LogFactory.getLog(NotificationSubscriptionServiceBean.class);

    @EJB(mappedName = NotificationProducerDAO.JNDI_BINDING)
    private NotificationProducerDAO     notificationProducerDAO;

    @EJB(mappedName = NotificationProducerService.JNDI_BINDING)
    private NotificationProducerService notificationProducerService;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO              applicationDAO;

    @EJB(mappedName = NodeDAO.JNDI_BINDING)
    private NodeDAO                     nodeDAO;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<NotificationProducerSubscriptionEntity> listTopics() {

        LOG.debug("list topics");
        return notificationProducerDAO.listTopics();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeSubscription(String topic, EndpointReferenceEntity subscription)
            throws SubscriptionNotFoundException, EndpointReferenceNotFoundException, PermissionDeniedException {

        LOG.debug("remove subscription " + subscription.getName() + " for topic " + topic);
        if (null != subscription.getApplication()) {
            notificationProducerService.unsubscribe(topic, subscription.getAddress(), subscription.getApplication());

        } else {
            notificationProducerService.unsubscribe(topic, subscription.getAddress(), subscription.getNode());
        }
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public Set<EndpointReferenceEntity> listSubscriptions(String topic)
            throws SubscriptionNotFoundException {

        LOG.debug("list subscriptions for topic: " + topic);
        NotificationProducerSubscriptionEntity topicEntity = notificationProducerDAO.getSubscription(topic);
        return topicEntity.getConsumers();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addSubscription(String topic, String address, String consumer)
            throws PermissionDeniedException {

        LOG.debug("add subscription for topic " + topic + " address=" + address + " consumer=" + consumer);
        ApplicationEntity application = applicationDAO.findApplication(consumer);
        if (null == application) {
            NodeEntity node = nodeDAO.findNode(consumer);
            if (null == node) {
                LOG.debug("consumer not found: " + consumer);
                throw new PermissionDeniedException("Consumer not found", "errorConsumerNotFound");
            }
            notificationProducerService.subscribe(topic, address, node);
        } else {
            notificationProducerService.subscribe(topic, address, application);
        }
    }
}
