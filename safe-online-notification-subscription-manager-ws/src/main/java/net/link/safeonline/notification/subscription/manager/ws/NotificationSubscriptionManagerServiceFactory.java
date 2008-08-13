/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.subscription.manager.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerService;


public class NotificationSubscriptionManagerServiceFactory {

    private NotificationSubscriptionManagerServiceFactory() {

        // empty
    }

    public static NotificationSubscriptionManagerService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("notification-subscription-manager.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException("Notification Subscription Manager WSDL not found");

        NotificationSubscriptionManagerService service = new NotificationSubscriptionManagerService(wsdlUrl,
                new QName("urn:net:lin-k:safe-online:notification:subscription:manager",
                        "NotificationSubscriptionManagerService"));

        return service;
    }
}
