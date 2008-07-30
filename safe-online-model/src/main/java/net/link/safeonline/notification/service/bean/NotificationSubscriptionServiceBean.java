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
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.notification.dao.NotificationProducerDAO;
import net.link.safeonline.notification.service.NotificationProducerService;
import net.link.safeonline.notification.service.NotificationSubscriptionService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class NotificationSubscriptionServiceBean implements
		NotificationSubscriptionService {

	private static final Log LOG = LogFactory
			.getLog(NotificationSubscriptionServiceBean.class);

	@EJB
	private NotificationProducerDAO notificationProducerDAO;

	@EJB
	private NotificationProducerService notificationProducerService;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private DeviceDAO deviceDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<NotificationProducerSubscriptionEntity> listTopics() {
		LOG.debug("list topics");
		return this.notificationProducerDAO.listTopics();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeSubscription(String topic,
			EndpointReferenceEntity subscription)
			throws SubscriptionNotFoundException,
			EndpointReferenceNotFoundException, PermissionDeniedException {
		LOG.debug("remove subscription " + subscription.getName()
				+ " for topic " + topic);
		if (null != subscription.getApplication()) {
			this.notificationProducerService.unsubscribe(topic, subscription
					.getAddress(), subscription.getApplication());

		} else {
			this.notificationProducerService.unsubscribe(topic, subscription
					.getAddress(), subscription.getDevice());
		}
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public Set<EndpointReferenceEntity> listSubscriptions(String topic)
			throws SubscriptionNotFoundException {
		LOG.debug("list subscriptions for topic: " + topic);
		NotificationProducerSubscriptionEntity topicEntity = this.notificationProducerDAO
				.getSubscription(topic);
		return topicEntity.getConsumers();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addSubscription(String topic, String address, String consumer)
			throws PermissionDeniedException {
		LOG.debug("add subscription for topic " + topic + " address=" + address
				+ " consumer=" + consumer);
		ApplicationEntity application = this.applicationDAO
				.findApplication(consumer);
		if (null == application) {
			DeviceEntity device = this.deviceDAO.findDevice(consumer);
			if (null == device) {
				LOG.debug("consumer not found: " + consumer);
				throw new PermissionDeniedException("Consumer not found",
						"errorConsumerNotFound");
			}
			this.notificationProducerService.subscribe(topic, address, device);
		} else {
			this.notificationProducerService.subscribe(topic, address,
					application);
		}
	}
}
