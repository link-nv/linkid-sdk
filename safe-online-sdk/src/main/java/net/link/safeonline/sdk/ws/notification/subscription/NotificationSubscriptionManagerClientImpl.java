/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.subscription;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import net.lin_k.safe_online.notification.subscription.manager.*;
import net.link.safeonline.notification.subscription.manager.ws.NotificationSubscriptionManagerServiceFactory;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.NotificationErrorCode;
import net.link.safeonline.sdk.api.ws.WebServiceConstants;
import net.link.safeonline.sdk.api.ws.notification.subscription.client.NotificationSubscriptionManagerClient;
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
public class NotificationSubscriptionManagerClientImpl extends AbstractWSClient<NotificationSubscriptionManagerPort> implements
        NotificationSubscriptionManagerClient {

    private static final Log LOG = LogFactory.getLog( NotificationSubscriptionManagerClientImpl.class );

    private final String location;

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     * @param configuration  The WS-Security configuration.
     */
    public NotificationSubscriptionManagerClientImpl(String location, X509Certificate sslCertificate,
                                                     final WSSecurityConfiguration configuration) {

        super( NotificationSubscriptionManagerServiceFactory.newInstance().getNotificationSubscriptionManagerPort() );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location = location + "/subscription" );

        registerTrustManager( sslCertificate );
        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

        private W3CEndpointReference getEndpointReference(String address) {

            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            builder.address( address );
            return builder.build();
        }

//    private EndpointReferenceType getEndpointReference(String address) {
//
//        EndpointReferenceType endpoint = new EndpointReferenceType();
//        AttributedURIType addressType = new AttributedURIType();
//        addressType.setValue( address );
//        endpoint.setAddress( addressType );
//        return endpoint;
//    }

    public void unsubscribe(String topic, String address)
            throws SubscriptionNotFoundException, RequestDeniedException, WSClientTransportException {

        LOG.debug( "unsubscribe" );
        UnsubscribeRequest request = new UnsubscribeRequest();

                W3CEndpointReference endpoint = getEndpointReference( address );
//        request.setConsumerReference( getEndpointReference( address ) );

        TopicType topicType = new TopicType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect( WebServiceConstants.TOPIC_DIALECT_SIMPLE );
        topicExpression.getContent().add( topic );
        topicType.setTopic( topicExpression );
        request.setTopic( topicType );

        UnsubscribeResponse response;
        try {
            response = getPort().unsubscribe( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        checkStatus( response );
    }

    private void checkStatus(UnsubscribeResponse response)
            throws SubscriptionNotFoundException, RequestDeniedException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        NotificationErrorCode errorCode = NotificationErrorCode.getNotificationErrorCode( statusCodeValue );
        if (NotificationErrorCode.SUBSCRIPTION_NOT_FOUND == errorCode)
            throw new SubscriptionNotFoundException();
        else if (NotificationErrorCode.PERMISSION_DENIED == errorCode)
            throw new RequestDeniedException();
    }
}
