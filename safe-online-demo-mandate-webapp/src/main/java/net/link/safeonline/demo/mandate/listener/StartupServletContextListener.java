/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.listener;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.demo.mandate.keystore.DemoMandateKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.SubscriptionFailedException;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClient;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This servlet context listener will subscribe demo-mandate to the wanted olas
 * notifications.
 * 
 * @author wvdhaute
 * 
 */
public class StartupServletContextListener implements ServletContextListener {

	private static final Log LOG = LogFactory
			.getLog(StartupServletContextListener.class);

	public void contextInitialized(ServletContextEvent event) {
		LOG.debug("context initialized");

		String wsLocation = event.getServletContext().getInitParameter(
				"WsLocation");
		PrivateKeyEntry privateKeyEntry = DemoMandateKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate certificate = (X509Certificate) privateKeyEntry
				.getCertificate();
		PrivateKey privateKey = privateKeyEntry.getPrivateKey();

		String address = wsLocation + DemoConstants.DEMO_WS_RUNTIME
				+ "/consumer";

		NotificationProducerClient client = new NotificationProducerClientImpl(
				wsLocation, certificate, privateKey);
		try {
			client.subscribe(SafeOnlineConstants.TOPIC_REMOVE_USER, address);
		} catch (SubscriptionFailedException e) {
			LOG.debug("Failed to subscribe to topic: "
					+ SafeOnlineConstants.TOPIC_REMOVE_USER);
			return;
		}
		LOG.debug("subscribed to topic "
				+ SafeOnlineConstants.TOPIC_REMOVE_USER);
	}

	public void contextDestroyed(ServletContextEvent event) {
		LOG.debug("context destroyed");
	}

}
