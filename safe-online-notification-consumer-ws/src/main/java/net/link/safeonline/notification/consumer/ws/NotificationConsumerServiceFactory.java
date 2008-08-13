/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.consumer.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.notification.consumer.NotificationConsumerService;


public class NotificationConsumerServiceFactory {

    private NotificationConsumerServiceFactory() {

        // empty
    }

    public static NotificationConsumerService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("notification-consumer.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException("Notification Consumer WSDL not found");

        NotificationConsumerService service = new NotificationConsumerService(wsdlUrl, new QName(
                "urn:net:lin-k:safe-online:notification:consumer", "NotificationConsumerService"));

        return service;
    }
}
