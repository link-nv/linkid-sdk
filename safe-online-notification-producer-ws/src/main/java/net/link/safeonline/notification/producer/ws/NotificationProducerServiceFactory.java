/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.producer.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.notification.producer.NotificationProducerService;

public class NotificationProducerServiceFactory {

	private NotificationProducerServiceFactory() {
		// empty
	}

	public static NotificationProducerService newInstance() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		URL wsdlUrl = classLoader.getResource("notification-producer.wsdl");
		if (null == wsdlUrl) {
			throw new RuntimeException("Notification Producer WSDL not found");
		}

		NotificationProducerService service = new NotificationProducerService(
				wsdlUrl, new QName(
						"urn:net:lin-k:safe-online:notification:producer",
						"NotificationProducerService"));

		return service;
	}
}
