/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.producer;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import net.lin_k.safe_online.notification.producer.FilterType;
import net.lin_k.safe_online.notification.producer.NotificationProducerPort;
import net.lin_k.safe_online.notification.producer.SubscribeRequest;
import net.lin_k.safe_online.notification.producer.SubscribeResponse;
import net.link.safeonline.notification.producer.ws.NotificationProducerServiceFactory;
import net.link.safeonline.sdk.api.exception.SubscriptionFailedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.NotificationErrorCode;
import net.link.safeonline.sdk.api.ws.WebServiceConstants;
import net.link.safeonline.sdk.api.ws.notification.producer.client.NotificationProducerClient;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


/**
 * Implementation WS-Notification producer service.
 *
 * @author wvdhaute
 */
public class NotificationProducerClientImpl extends AbstractWSClient<NotificationProducerPort> implements NotificationProducerClient {

    private static final Log LOG = LogFactory.getLog( NotificationProducerClientImpl.class );

    private final String location;


    /**
     * Main constructor.
     *
     * @param location the location (host:port) of the attribute web service.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     * @param configuration
     */
    public NotificationProducerClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super(NotificationProducerServiceFactory.newInstance().getNotificationProducerPort() );
        getBindingProvider().getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location = location + "/producer" );

        registerTrustManager( sslCertificate );
        WSSecurityHandler.install( getBindingProvider(), configuration );
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
            response = getPort().subscribe( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        checkStatus( response );
    }

    private void checkStatus(SubscribeResponse response)
            throws SubscriptionFailedException {

        if (response.getSubscribeStatus().getStatusCode().equals( NotificationErrorCode.SUBSCRIPTION_FAILED.getErrorCode() ))
            throw new SubscriptionFailedException();
    }
}
