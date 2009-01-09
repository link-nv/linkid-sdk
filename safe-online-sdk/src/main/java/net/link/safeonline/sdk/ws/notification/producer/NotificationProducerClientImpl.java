/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.producer;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import net.lin_k.safe_online.notification.producer.FilterType;
import net.lin_k.safe_online.notification.producer.NotificationProducerPort;
import net.lin_k.safe_online.notification.producer.NotificationProducerService;
import net.lin_k.safe_online.notification.producer.SubscribeRequest;
import net.lin_k.safe_online.notification.producer.SubscribeResponse;
import net.link.safeonline.notification.producer.ws.NotificationProducerServiceFactory;
import net.link.safeonline.sdk.exception.SubscriptionFailedException;
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
public class NotificationProducerClientImpl extends AbstractMessageAccessor implements NotificationProducerClient {

    private static final Log               LOG = LogFactory.getLog(NotificationProducerClientImpl.class);

    private final NotificationProducerPort port;

    private final String                   location;


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
    public NotificationProducerClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

        NotificationProducerService service = NotificationProducerServiceFactory.newInstance();
        port = service.getNotificationProducerPort();
        this.location = location + "/safe-online-ws/producer";
        setEndpointAddress();

        LOG.debug("endpoint: " + this.location);

        registerMessageLoggerHandler(port);
        WSSecurityClientHandler.addNewHandler(port, clientCertificate, clientPrivateKey);
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
    }

    private W3CEndpointReference getEndpointReference(String address) {

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(address);
        return builder.build();
    }

    public void subscribe(String topic, String address)
            throws SubscriptionFailedException, WSClientTransportException {

        LOG.debug("subscribe " + address + " to " + topic);
        SubscribeRequest request = new SubscribeRequest();

        W3CEndpointReference endpoint = getEndpointReference(address);
        request.setConsumerReference(endpoint);

        FilterType filter = new FilterType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WebServiceConstants.TOPIC_DIALECT_SIMPLE);
        topicExpression.getContent().add(topic);
        filter.setTopic(topicExpression);
        request.setFilter(filter);

        SafeOnlineTrustManager.configureSsl();

        SubscribeResponse response;
        try {
            response = port.subscribe(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(location);
        }

        checkStatus(response);
    }

    private void checkStatus(SubscribeResponse response)
            throws SubscriptionFailedException {

        if (response.getSubscribeStatus().getStatusCode().equals(NotificationErrorCode.SUBSCRIPTION_FAILED.getErrorCode()))
            throw new SubscriptionFailedException();
    }
}
