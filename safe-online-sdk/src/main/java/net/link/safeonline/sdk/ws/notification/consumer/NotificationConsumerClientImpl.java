/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.consumer;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import net.lin_k.safe_online.notification.consumer.NotificationConsumerPort;
import net.lin_k.safe_online.notification.consumer.NotificationConsumerService;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType;
import net.lin_k.safe_online.notification.consumer.Notify;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType.Message;
import net.link.safeonline.notification.consumer.ws.NotificationConsumerServiceFactory;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

/**
 * Implementation of the WS-Notification consumer interface. This class is using
 * JAX-WS, secured via WS-Security and server-side SSL.
 * 
 * @author wvdhaute
 * 
 */
public class NotificationConsumerClientImpl extends AbstractMessageAccessor
		implements NotificationConsumerClient {

	private static final Log LOG = LogFactory
			.getLog(NotificationConsumerClientImpl.class);

	private static final String TOPIC_DIALECT_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";

	private final NotificationConsumerPort port;

	private final String location;

	/**
	 * Main constructor.
	 * 
	 * @param location
	 *            the location (full) of the notification web service.
	 * @param clientCertificate
	 *            the X509 certificate to use for WS-Security signature.
	 * @param clientPrivateKey
	 *            the private key corresponding with the client certificate.
	 */
	public NotificationConsumerClientImpl(String location,
			X509Certificate clientCertificate, PrivateKey clientPrivateKey) {
		NotificationConsumerService consumerService = NotificationConsumerServiceFactory
				.newInstance();
		this.port = consumerService.getNotificationConsumerPort();
		this.location = location;
		setEndpointAddress();

		registerMessageLoggerHandler(this.port);
		WSSecurityClientHandler.addNewHandler(this.port, clientCertificate,
				clientPrivateKey);
	}

	private void setEndpointAddress() {
		BindingProvider bindingProvider = (BindingProvider) this.port;

		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location);
	}

	private W3CEndpointReference getEndpointReference(String address) {
		W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
		builder.address(address);
		return builder.build();
	}

	public void sendNotification(String topic, String destination,
			List<String> message) {
		LOG.debug("send notification to " + this.location + " for topic: "
				+ topic + " (destination=" + destination + ")");

		// TODO: wrong, should be the producer's EP, not the consumers ...
		W3CEndpointReference endpoint = getEndpointReference(this.location);

		TopicExpressionType topicExpression = new TopicExpressionType();
		topicExpression.setDialect(TOPIC_DIALECT_SIMPLE);
		topicExpression.getContent().add(topic);

		Notify notifications = new Notify();
		NotificationMessageHolderType notification = new NotificationMessageHolderType();
		notification.setProducerReference(endpoint);
		notification.setTopic(topicExpression);
		Message notificationMessage = new Message();
		notificationMessage.getContent().addAll(message);
		notificationMessage.setDestination(destination);
		notification.setMessage(notificationMessage);
		notifications.getNotificationMessage().add(notification);

		SafeOnlineTrustManager.configureSsl();

		this.port.notify(notifications);
	}
}