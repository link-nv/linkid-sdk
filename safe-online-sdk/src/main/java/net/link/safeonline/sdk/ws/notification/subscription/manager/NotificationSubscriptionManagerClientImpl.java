/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.subscription.manager;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerPort;
import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerService;
import net.lin_k.safe_online.notification.subscription.manager.StatusCodeType;
import net.lin_k.safe_online.notification.subscription.manager.StatusType;
import net.lin_k.safe_online.notification.subscription.manager.TopicType;
import net.lin_k.safe_online.notification.subscription.manager.UnsubscribeRequest;
import net.lin_k.safe_online.notification.subscription.manager.UnsubscribeResponse;
import net.link.safeonline.notification.subscription.manager.ws.NotificationSubscriptionManagerServiceFactory;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubscriptionNotFoundException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.ws.common.NotificationErrorCode;
import net.link.safeonline.ws.common.WebServiceConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * Implementation WS-Notification producer service.
 * 
 * @author wvdhaute
 * 
 */
public class NotificationSubscriptionManagerClientImpl extends AbstractMessageAccessor implements NotificationSubscriptionManagerClient {

    private static final Log                          LOG = LogFactory.getLog(NotificationSubscriptionManagerClientImpl.class);

    private final NotificationSubscriptionManagerPort port;

    private final String                              location;


    /**
     * Main constructor.
     * 
     * @param location
     *            the location (host:port) of the attribute web service.
     * @param clientCertificate
     *            the X509 certificate to use for WS-Security signature.
     * @param clientPrivateKey
     *            the private key corresponding with the client certificate.
     */
    public NotificationSubscriptionManagerClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

        NotificationSubscriptionManagerService service = NotificationSubscriptionManagerServiceFactory.newInstance();
        this.port = service.getNotificationSubscriptionManagerPort();
        this.location = location + "/safe-online-ws/subscription";
        setEndpointAddress();

        registerMessageLoggerHandler(this.port);
        WSSecurityClientHandler.addNewHandler(this.port, clientCertificate, clientPrivateKey);
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) this.port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location);
    }

    private W3CEndpointReference getEndpointReference(String address) {

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(address);
        return builder.build();
    }

    public void unsubscribe(String topic, String address) throws SubscriptionNotFoundException, RequestDeniedException,
                                                         WSClientTransportException {

        LOG.debug("unsubscribe");
        UnsubscribeRequest request = new UnsubscribeRequest();

        W3CEndpointReference endpoint = getEndpointReference(address);
        request.setConsumerReference(endpoint);

        TopicType topicType = new TopicType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WebServiceConstants.TOPIC_DIALECT_SIMPLE);
        topicExpression.getContent().add(topic);
        topicType.setTopic(topicExpression);
        request.setTopic(topicType);

        SafeOnlineTrustManager.configureSsl();

        UnsubscribeResponse response;
        try {
            response = this.port.unsubscribe(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.location);
        }

        checkStatus(response);
    }

    private void checkStatus(UnsubscribeResponse response) throws SubscriptionNotFoundException, RequestDeniedException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        NotificationErrorCode errorCode = NotificationErrorCode.getNotificationErrorCode(statusCodeValue);
        if (NotificationErrorCode.SUBSCRIPTION_NOT_FOUND == errorCode)
            throw new SubscriptionNotFoundException();
        else if (NotificationErrorCode.PERMISSION_DENIED == errorCode)
            throw new RequestDeniedException();
    }
}
