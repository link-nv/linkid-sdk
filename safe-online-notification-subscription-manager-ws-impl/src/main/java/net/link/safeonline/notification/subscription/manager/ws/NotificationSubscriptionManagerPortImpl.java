/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.subscription.manager.ws;

import java.security.cert.X509Certificate;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerPort;
import net.lin_k.safe_online.notification.subscription.manager.StatusCodeType;
import net.lin_k.safe_online.notification.subscription.manager.StatusType;
import net.lin_k.safe_online.notification.subscription.manager.UnsubscribeRequest;
import net.lin_k.safe_online.notification.subscription.manager.UnsubscribeResponse;
import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.notification.service.NotificationProducerService;
import net.link.safeonline.sdk.ws.WSSecurityServerHandler;
import net.link.safeonline.ws.common.NotificationErrorCode;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

@WebService(endpointInterface = "net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerPort")
@HandlerChain(file = "auth-ws-handlers.xml")
@Injection
public class NotificationSubscriptionManagerPortImpl implements
		NotificationSubscriptionManagerPort {

	private final static Log LOG = LogFactory
			.getLog(NotificationSubscriptionManagerPortImpl.class);

	@Resource
	private WebServiceContext context;

	@EJB(mappedName = "SafeOnline/NotificationProducerServiceBean/local")
	private NotificationProducerService notificationProducerService;

	public RenewResponse renew(Renew request) {
		LOG.debug("renew");
		// TODO Auto-generated method stub
		return null;
	}

	public UnsubscribeResponse unsubscribe(UnsubscribeRequest request) {
		LOG.debug("unsubscribe");

		X509Certificate certificate = WSSecurityServerHandler
				.getCertificate(this.context);

		W3CEndpointReference consumerReference = request.getConsumerReference();
		DOMResult consumerReferenceDom = new DOMResult();
		consumerReference.writeTo(consumerReferenceDom);
		String address = consumerReferenceDom.getNode().getFirstChild()
				.getFirstChild().getFirstChild().getNodeValue();

		TopicExpressionType topicExpression = request.getTopic().getTopic();
		String topic = (String) topicExpression.getContent().get(0);

		try {
			this.notificationProducerService.unsubscribe(topic, address,
					certificate);
		} catch (SubscriptionNotFoundException e) {
			LOG.debug("Subscription not found: " + e.getMessage());
			return createSubscriptionNotFoundResponse(e.getMessage());
		} catch (EndpointReferenceNotFoundException e) {
			LOG.debug("Endpoint reference not found: " + e.getMessage());
			return createSubscriptionNotFoundResponse(e.getMessage());
		} catch (PermissionDeniedException e) {
			LOG.debug("Permission denied: " + e.getMessage());
			return createPermissionDeniedResponse(e.getMessage());
		}

		return createGenericResponse(NotificationErrorCode.SUCCESS);
	}

	private UnsubscribeResponse createPermissionDeniedResponse(String message) {
		UnsubscribeResponse response = createGenericResponse(NotificationErrorCode.PERMISSION_DENIED);
		response.getStatus().setStatusMessage(message);
		return response;
	}

	private UnsubscribeResponse createSubscriptionNotFoundResponse(
			String message) {
		UnsubscribeResponse response = createGenericResponse(NotificationErrorCode.SUBSCRIPTION_NOT_FOUND);
		response.getStatus().setStatusMessage(message);
		return response;
	}

	private UnsubscribeResponse createGenericResponse(
			NotificationErrorCode errorCode) {
		UnsubscribeResponse response = new UnsubscribeResponse();
		StatusType statusType = new StatusType();
		StatusCodeType statusCode = new StatusCodeType();
		statusCode.setValue(errorCode.getErrorCode());
		statusType.setStatusCode(statusCode);
		response.setStatus(statusType);
		return response;
	}
}
