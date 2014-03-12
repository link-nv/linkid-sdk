/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.producer;

import net.link.util.logging.Logger;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import net.lin_k.safe_online.notification.producer.*;
import net.link.safeonline.sdk.api.exception.SubscriptionFailedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.NotificationErrorCode;
import net.link.safeonline.sdk.api.ws.WebServiceConstants;
import net.link.safeonline.sdk.api.ws.notification.producer.client.NotificationProducerClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.notification.NotificationProducerServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


/**
 * Implementation WS-Notification producer service.
 *
 * @author wvdhaute
 */
public class NotificationProducerClientImpl extends AbstractWSClient<NotificationProducerPort> implements NotificationProducerClient {

    private static final Logger logger = Logger.get( NotificationProducerClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public NotificationProducerClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( NotificationProducerServiceFactory.newInstance().getNotificationProducerPort(), sslCertificate );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.notification.producer.path" ) ) );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    private static W3CEndpointReference getEndpointReference(String address) {

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address( address );
        return builder.build();
    }

    @Override
    public void subscribe(String topic, String address)
            throws SubscriptionFailedException, WSClientTransportException {

        logger.dbg( "subscribe \"%s\" to \"%s\"", address, topic );
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
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( response );
    }

    private static void validateStatus(SubscribeResponse response)
            throws SubscriptionFailedException {

        if (response.getSubscribeStatus().getStatusCode().equals( NotificationErrorCode.SUBSCRIPTION_FAILED.getErrorCode() ))
            throw new SubscriptionFailedException();
    }
}
