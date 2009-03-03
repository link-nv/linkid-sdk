/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.test;

import java.lang.reflect.Field;
import java.security.KeyStore.PrivateKeyEntry;

import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.sdk.ws.OlasServiceFactory;
import net.link.safeonline.sdk.ws.ServiceFactory;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClient;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClient;
import net.link.safeonline.sdk.ws.notification.subscription.manager.NotificationSubscriptionManagerClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;


/**
 * <h2>{@link DummyServiceFactory}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 3, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class DummyServiceFactory extends ServiceFactory {

    private static DummyServiceFactory instance;


    protected DummyServiceFactory() {

    }

    private static DummyServiceFactory getInstance() {

        if (instance == null) {
            instance = new DummyServiceFactory();
        }

        return instance;
    }

    public static void install()
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Field olasInstance = OlasServiceFactory.class.getDeclaredField("instance");
        olasInstance.setAccessible(true);
        olasInstance.set(null, getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributeClient _getAttributeService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        return new DummyAttributeClient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataClient _getDataService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NameIdentifierMappingClient _getIdMappingService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        return new DummyNameIdentifierMappingClient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NotificationConsumerClient _getNotificationConsumerService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NotificationProducerClient _getNotificationProducerService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NotificationSubscriptionManagerClient _getNotificationSubscriptionService(HttpServletRequest httpRequest,
                                                                                        PrivateKeyEntry privateKeyEntry) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SecurityTokenServiceClient _getStsService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
