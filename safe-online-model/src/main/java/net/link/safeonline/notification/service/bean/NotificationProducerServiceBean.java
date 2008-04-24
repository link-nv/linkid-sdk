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

import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.notification.dao.EndpointReferenceDAO;
import net.link.safeonline.notification.dao.NotificationProducerDAO;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.notification.message.MessageHandlerManager;
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
public class NotificationProducerServiceBean implements
		NotificationProducerService {

	@EJB
	private NotificationProducerDAO notificationProducerDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private DeviceDAO deviceDAO;

	@EJB
	private EndpointReferenceDAO endpointReferenceDAO;

	private static final Log LOG = LogFactory
			.getLog(NotificationProducerServiceBean.class);

	public void subscribe(String topic, String address,
			X509Certificate certificate) throws PermissionDeniedException {
		LOG.debug("subscribe");

		NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO
				.findSubscription(topic);
		if (null == subscription)
			subscription = this.notificationProducerDAO.addSubscription(topic);

		ApplicationEntity application = this.applicationDAO
				.findApplication(certificate);
		if (null == application) {
			DeviceEntity device = this.deviceDAO.findDevice(certificate);
			if (null == device) {
				throw new PermissionDeniedException(
						"Application or device not found.");
			}
			EndpointReferenceEntity endpointReference = this.endpointReferenceDAO
					.findEndpointReference(address, device);
			if (null == endpointReference)
				endpointReference = this.endpointReferenceDAO
						.addEndpointReference(address, device);
			subscription.getConsumers().add(endpointReference);
		} else {
			EndpointReferenceEntity endpointReference = this.endpointReferenceDAO
					.addEndpointReference(address, application);
			if (null == endpointReference)
				endpointReference = this.endpointReferenceDAO
						.addEndpointReference(address, application);
			subscription.getConsumers().add(endpointReference);
		}
	}

	public void unsubscribe(String topic, String address,
			X509Certificate certificate) throws SubscriptionNotFoundException,
			PermissionDeniedException, EndpointReferenceNotFoundException {
		LOG.debug("subscribe");
		NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO
				.getSubscription(topic);
		ApplicationEntity application = this.applicationDAO
				.findApplication(certificate);
		if (null == application) {
			DeviceEntity device = this.deviceDAO.findDevice(certificate);
			if (null == device) {
				throw new PermissionDeniedException(
						"Application or device not found.");
			}
			EndpointReferenceEntity endpointReference = this.endpointReferenceDAO
					.getEndpointReference(address, device);
			subscription.getConsumers().remove(endpointReference);
		} else {
			EndpointReferenceEntity endpointReference = this.endpointReferenceDAO
					.getEndpointReference(address, application);
			subscription.getConsumers().remove(endpointReference);
		}

	}

	public void sendNotification(String topic, List<String> message)
			throws SubscriptionNotFoundException,
			MessageHandlerNotFoundException {
		LOG.debug("send notification for topic: " + topic);
		NotificationProducerSubscriptionEntity subscription = this.notificationProducerDAO
				.getSubscription(topic);
		for (EndpointReferenceEntity consumer : subscription.getConsumers()) {
			MessageHandlerManager.sendMessage(topic, message, consumer);
		}
	}
}
