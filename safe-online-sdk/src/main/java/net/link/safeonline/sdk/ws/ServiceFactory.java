/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws;

import java.security.KeyStore.PrivateKeyEntry;

import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClient;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClient;
import net.link.safeonline.sdk.ws.notification.subscription.manager.NotificationSubscriptionManagerClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;


/**
 * <h2>{@link ServiceFactory}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 15, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class ServiceFactory {

    protected abstract AttributeClient _getAttributeService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry);

    protected abstract DataClient _getDataService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry);

    protected abstract NameIdentifierMappingClient _getIdMappingService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry);

    protected abstract SecurityTokenServiceClient _getStsService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry);

    protected abstract NotificationConsumerClient _getNotificationConsumerService(HttpServletRequest httpRequest,
                                                                                  PrivateKeyEntry privateKeyEntry);

    protected abstract NotificationProducerClient _getNotificationProducerService(HttpServletRequest httpRequest,
                                                                                  PrivateKeyEntry privateKeyEntry);

    protected abstract NotificationSubscriptionManagerClient _getNotificationSubscriptionService(HttpServletRequest httpRequest,
                                                                                                 PrivateKeyEntry privateKeyEntry);
}
