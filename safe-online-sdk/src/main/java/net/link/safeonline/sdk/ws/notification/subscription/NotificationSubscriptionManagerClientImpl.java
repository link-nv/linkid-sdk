/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.subscription;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import net.lin_k.safe_online.notification.subscription.manager.*;
import net.link.safeonline.notification.subscription.manager.ws.NotificationSubscriptionManagerServiceFactory;
import net.link.safeonline.sdk.ws.NotificationErrorCode;
import net.link.safeonline.sdk.ws.WebServiceConstants;
import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.SubscriptionNotFoundException;
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
public class NotificationSubscriptionManagerClientImpl extends AbstractWSClient implements NotificationSubscriptionManagerClient {

    private static final Log LOG = LogFactory.getLog( NotificationSubscriptionManagerClientImpl.class );

    private final NotificationSubscriptionManagerPort port;

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
    public NotificationSubscriptionManagerClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                                                     X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        NotificationSubscriptionManagerService service = NotificationSubscriptionManagerServiceFactory.newInstance();
        port = service.getNotificationSubscriptionManagerPort();
        this.location = location + "/subscription";
        setEndpointAddress();

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

    public void unsubscribe(String topic, String address)
            throws SubscriptionNotFoundException, RequestDeniedException, WSClientTransportException {

        LOG.debug( "unsubscribe" );
        UnsubscribeRequest request = new UnsubscribeRequest();

        W3CEndpointReference endpoint = getEndpointReference( address );
        request.setConsumerReference( endpoint );

        TopicType topicType = new TopicType();
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect( WebServiceConstants.TOPIC_DIALECT_SIMPLE );
        topicExpression.getContent().add( topic );
        topicType.setTopic( topicExpression );
        request.setTopic( topicType );

        UnsubscribeResponse response;
        try {
            response = port.unsubscribe( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
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
