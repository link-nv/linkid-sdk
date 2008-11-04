/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.message;

import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.notification.message.handler.RemoveUserMessageHandler;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClient;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClientImpl;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;


/**
 * Manager class for registered WS-Notification message handlers.
 * 
 * @author wvdhaute
 * 
 */
public class MessageHandlerManager {

    private MessageHandlerManager() {

        // empty
    }


    private static final Map<String, MessageHandler> messageHandlerMap = new HashMap<String, MessageHandler>();

    static {
        registerProtocolHandler(SafeOnlineConstants.TOPIC_REMOVE_USER, RemoveUserMessageHandler.class);
    }


    private static void registerProtocolHandler(String topic, Class<? extends MessageHandler> messageHandlerClass) {

        try {
            MessageHandler messageHandler = messageHandlerClass.newInstance();
            messageHandlerMap.put(topic, messageHandler);
        } catch (Exception e) {
            throw new RuntimeException("could not initialize protocol handler: " + messageHandlerClass.getName() + "; message: "
                    + e.getMessage(), e);
        }
    }

    public static NotificationMessage getMessage(String topic, String subject, String content, EndpointReferenceEntity consumer)
            throws MessageHandlerNotFoundException {

        MessageHandler messageHandler = messageHandlerMap.get(topic);
        if (null == messageHandler)
            throw new MessageHandlerNotFoundException(topic);
        messageHandler.init();

        return messageHandler.createMessage(topic, subject, content, consumer);
    }

    public static void sendMessage(NotificationMessage message, EndpointReferenceEntity consumer)
            throws MessageHandlerNotFoundException, WSClientTransportException {

        MessageHandler messageHandler = messageHandlerMap.get(message.getTopic());
        if (null == messageHandler)
            throw new MessageHandlerNotFoundException(message.getTopic());
        messageHandler.init();

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        NotificationConsumerClient consumerClient = new NotificationConsumerClientImpl(consumer.getAddress(),
                authIdentityServiceClient.getCertificate(), authIdentityServiceClient.getPrivateKey());
        consumerClient.sendNotification(message.getTopic(), message.getDestination(), message.getSubject(), message.getContent());
    }

    public static void handleMessage(String topic, String destination, String subject, String content)
            throws MessageHandlerNotFoundException {

        MessageHandler messageHandler = messageHandlerMap.get(topic);
        if (null == messageHandler)
            throw new MessageHandlerNotFoundException(topic);
        messageHandler.init();

        messageHandler.handleMessage(destination, subject, content);
    }

}
