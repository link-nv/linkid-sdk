/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClient;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClientImpl;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClient;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClientImpl;
import net.link.safeonline.sdk.ws.notification.subscription.manager.NotificationSubscriptionManagerClient;
import net.link.safeonline.sdk.ws.notification.subscription.manager.NotificationSubscriptionManagerClientImpl;
import net.link.safeonline.sdk.ws.session.SessionTrackingClient;
import net.link.safeonline.sdk.ws.session.SessionTrackingClientImpl;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.util.servlet.SafeOnlineConfig;


/**
 * <h2>{@link OlasServiceFactory}<br>
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
public class OlasServiceFactory extends ServiceFactory {

    private static ServiceFactory instance;


    protected OlasServiceFactory() {

    }

    private static ServiceFactory getInstance() {

        if (instance == null) {
            instance = new OlasServiceFactory();
        }

        return instance;
    }

    /**
     * Retrieve a proxy to the OLAS attribute web service.
     */
    public static AttributeClient getAttributeService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getAttributeService(privateKeyEntry);
    }

    @Override
    protected AttributeClient _getAttributeService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new AttributeClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS data web service.
     */
    public static DataClient getDataService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getDataService(privateKeyEntry);
    }

    @Override
    protected DataClient _getDataService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new DataClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS ID mapping web service.
     */
    public static NameIdentifierMappingClient getIdMappingService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getIdMappingService(privateKeyEntry);
    }

    @Override
    protected NameIdentifierMappingClient _getIdMappingService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NameIdentifierMappingClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS Security Token web service.
     */
    public static SecurityTokenServiceClient getStsService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getStsService(privateKeyEntry);
    }

    @Override
    protected SecurityTokenServiceClient _getStsService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new SecurityTokenServiceClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification consumer web service.
     */
    public static NotificationConsumerClient getNotificationConsumerService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getNotificationConsumerService(privateKeyEntry);
    }

    @Override
    protected NotificationConsumerClient _getNotificationConsumerService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationConsumerClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification producer web service.
     */
    public static NotificationProducerClient getNotificationProducerService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getNotificationProducerService(privateKeyEntry);
    }

    @Override
    protected NotificationProducerClient _getNotificationProducerService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationProducerClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification subscription manager web service.
     */
    public static NotificationSubscriptionManagerClient getNotificationSubscriptionService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getNotificationSubscriptionService(privateKeyEntry);
    }

    @Override
    protected NotificationSubscriptionManagerClient _getNotificationSubscriptionService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationSubscriptionManagerClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS session tracking web service.
     */
    public static SessionTrackingClient getSessionTrackingService(PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getSessionTrackingService(privateKeyEntry);
    }

    @Override
    protected SessionTrackingClient _getSessionTrackingService(PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new SessionTrackingClientImpl(SafeOnlineConfig.wsbase(), certificate, privateKey);
    }
}
