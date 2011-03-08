/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.notification.consumer;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.notification.consumer.NotificationConsumerPort;
import net.lin_k.safe_online.notification.consumer.NotificationConsumerService;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType;
import net.lin_k.safe_online.notification.consumer.Notify;
import net.lin_k.safe_online.notification.consumer.NotificationMessageHolderType.Message;
import net.link.safeonline.notification.consumer.ws.NotificationConsumerServiceFactory;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;


/**
 * Implementation of the WS-Notification consumer interface. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author wvdhaute
 */
public class NotificationConsumerClientImpl extends AbstractWSClient implements NotificationConsumerClient {

    private static final Log LOG = LogFactory.getLog( NotificationConsumerClientImpl.class );

    private static final String TOPIC_DIALECT_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";

    private final NotificationConsumerPort port;

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
    public NotificationConsumerClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                                          X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        NotificationConsumerService consumerService = NotificationConsumerServiceFactory.newInstance();
        port = consumerService.getNotificationConsumerPort();
        this.location = location;
        setEndpointAddress();

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );

        WSSecurityClientHandler.addNewHandler( port, clientCertificate, clientPrivateKey, serverCertificate, maxOffset );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    public void sendNotification(String topic, String destination, String subject, String content)
            throws WSClientTransportException {

        LOG.debug( "send notification to " + location + " for topic: " + topic + ", destination:" + destination + ", subject:" + subject
                + ", content:" + content );

        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect( TOPIC_DIALECT_SIMPLE );
        topicExpression.getContent().add( topic );

        Notify notifications = new Notify();
        NotificationMessageHolderType notification = new NotificationMessageHolderType();
        notification.setTopic( topicExpression );
        Message notificationMessage = new Message();
        notificationMessage.setDestination( destination );
        notificationMessage.setSubject( subject );
        notificationMessage.setContent( content );
        notification.setMessage( notificationMessage );
        notifications.getNotificationMessage().add( notification );

        try {
            port.notify( notifications );
        } catch (ClientTransportException e) {
            LOG.debug( "Failed to send notification" );
            throw new WSClientTransportException( location, e );
        }
    }
}
