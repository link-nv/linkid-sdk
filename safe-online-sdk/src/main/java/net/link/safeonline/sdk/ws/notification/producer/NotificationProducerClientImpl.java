/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.producer;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import net.lin_k.safe_online.notification.producer.*;
import net.link.safeonline.notification.producer.ws.NotificationProducerServiceFactory;
import net.link.safeonline.sdk.ws.NotificationErrorCode;
import net.link.safeonline.sdk.ws.WebServiceConstants;
import net.link.safeonline.sdk.logging.exception.SubscriptionFailedException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


/**
 * Implementation WS-Notification producer service.
 *
 * @author wvdhaute
 */
public class NotificationProducerClientImpl extends AbstractWSClient implements NotificationProducerClient {

    private static final Log LOG = LogFactory.getLog( NotificationProducerClientImpl.class );

    private final NotificationProducerPort port;

    private final String location;


    /**
     * Main constructor.
     *
     * @param location the location (host:port) of the attribute web service.
     * @param clientCertificate the X509 certificate to use for WS-Security signature.
     * @param clientPrivateKey the private key corresponding with the client certificate.
     * @param serverCertificate the X509 certificate of the server
     * @param maxOffset the maximum offset of the WS-Security timestamp received. If <code>null</code> default offset configured in
     *            {@link WSSecurityClientHandler} will be used.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public NotificationProducerClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                                          X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        NotificationProducerService service = NotificationProducerServiceFactory.newInstance();
        port = service.getNotificationProducerPort();
        this.location = location + "/producer";
        setEndpointAddress();

        LOG.debug( "endpoint: " + this.location );

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );

        WSSecurityClientHandler.addNewHandler( port, clientCertificate, clientPrivateKey, serverCertificate, maxOffset );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    private W3CEndpointReference getEndpointReference(String address) {

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address( address );
        return builder.build();
    }

    public void subscribe(String topic, String address)
            throws SubscriptionFailedException, WSClientTransportException {

        LOG.debug( "subscribe " + address + " to " + topic );
        SubscribeRequest request = new SubscribeRequest();

        W3CEndpointReference endpoint = getEndpointReference( address );
        request.setConsumerReference( endpoint );

        FilterType filter = new FilterType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect( WebServiceConstants.TOPIC_DIALECT_SIMPLE );
        topicExpression.getContent().add( topic );
        filter.setTopic( topicExpression );
        request.setFilter( filter );

        SubscribeResponse response;
        try {
            response = port.subscribe( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
        }

        checkStatus( response );
    }

    private void checkStatus(SubscribeResponse response)
            throws SubscriptionFailedException {

        if (response.getSubscribeStatus().getStatusCode().equals( NotificationErrorCode.SUBSCRIPTION_FAILED.getErrorCode() ))
            throw new SubscriptionFailedException();
    }
}
