/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.producer.ws;

import java.security.cert.X509Certificate;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.notification.producer.FilterType;
import net.lin_k.safe_online.notification.producer.NotificationProducerPort;
import net.lin_k.safe_online.notification.producer.SubscribeRequest;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.notification.service.NotificationProducerService;
import net.link.safeonline.sdk.ws.WSSecurityServerHandler;
import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.SubscribeCreationFailedFaultType;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


@WebService(endpointInterface = "net.lin_k.safe_online.notification.producer.NotificationProducerPort")
@HandlerChain(file = "auth-ws-handlers.xml")
@Injection
public class NotificationProducerPortImpl implements NotificationProducerPort {

    private final static Log            LOG = LogFactory.getLog(NotificationProducerPortImpl.class);

    @Resource
    private WebServiceContext           context;

    @EJB(mappedName = NotificationProducerService.JNDI_BINDING)
    private NotificationProducerService notificationProducerService;


    public SubscribeResponse subscribe(SubscribeRequest request) {

        LOG.debug("subscribe");

        X509Certificate certificate = WSSecurityServerHandler.getCertificate(context);

        W3CEndpointReference consumerReference = request.getConsumerReference();
        DOMResult consumerReferenceDom = new DOMResult();
        consumerReference.writeTo(consumerReferenceDom);
        String address = consumerReferenceDom.getNode().getFirstChild().getFirstChild().getFirstChild().getNodeValue();

        FilterType filter = request.getFilter();
        TopicExpressionType topicExpression = filter.getTopic();
        String topic = (String) topicExpression.getContent().get(0);

        try {
            notificationProducerService.subscribe(topic, address, certificate);
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied: " + e.getMessage());
            return createSubscribeCreationFailedResponse();
        }

        return createGenericResponse();
    }

    private SubscribeResponse createGenericResponse() {

        SubscribeResponse response = new SubscribeResponse();
        return response;
    }

    private SubscribeResponse createSubscribeCreationFailedResponse() {

        SubscribeResponse response = createGenericResponse();
        SubscribeCreationFailedFaultType error = new SubscribeCreationFailedFaultType();
        response.getAny().add(error);
        return response;
    }

    public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage request) {

        LOG.debug("getCurrentMessage");
        // TODO Auto-generated method stub
        return null;
    }
}
