/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.subscription;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import net.lin_k.safe_online.notification.subscription.manager.*;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.NotificationErrorCode;
import net.link.safeonline.sdk.api.ws.WebServiceConstants;
import net.link.safeonline.sdk.api.ws.notification.subscription.client.NotificationSubscriptionManagerClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.notification.NotificationSubscriptionManagerServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


/**
 * Implementation WS-Notification producer service.
 *
 * @author wvdhaute
 */
public class NotificationSubscriptionManagerClientImpl extends AbstractWSClient<NotificationSubscriptionManagerPort> implements
        NotificationSubscriptionManagerClient {

    private static final Logger logger = Logger.get( NotificationSubscriptionManagerClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  The WS-Security configuration.
     */
    public NotificationSubscriptionManagerClientImpl(String location, X509Certificate sslCertificate,
                                                     final WSSecurityConfiguration configuration) {

        super( NotificationSubscriptionManagerServiceFactory.newInstance().getNotificationSubscriptionManagerPort(), sslCertificate );

        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.notification.subscription.path" ) ) );

        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

    private static W3CEndpointReference getEndpointReference(String address) {

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address( address );
        return builder.build();
    }

    @Override
    public void unsubscribe(String topic, String address)
            throws SubscriptionNotFoundException, RequestDeniedException, WSClientTransportException {

        UnsubscribeRequest request = new UnsubscribeRequest();

        TopicType topicType = new TopicType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect( WebServiceConstants.TOPIC_DIALECT_SIMPLE );
        topicExpression.getContent().add( topic );
        topicType.setTopic( topicExpression );
        request.setTopic( topicType );
        request.setConsumerReference( getEndpointReference( address ) );

        UnsubscribeResponse response;
        try {
            response = getPort().unsubscribe( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( response );
    }

    private static void validateStatus(UnsubscribeResponse response)
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
