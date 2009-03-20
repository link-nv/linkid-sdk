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

import javax.servlet.http.HttpServletRequest;

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
     * 
     * @param request
     *            The servlet request: used to access {@link SafeOnlineConfig#wsbase}.
     */
    public static AttributeClient getAttributeService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getAttributeService(request, privateKeyEntry);
    }

    @Override
    protected AttributeClient _getAttributeService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new AttributeClientImpl(getWSLocation(request), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS data web service.
     * 
     * @param request
     *            The servlet request: used to access {@link SafeOnlineConfig#wsbase}.
     */
    public static DataClient getDataService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getDataService(request, privateKeyEntry);
    }

    @Override
    protected DataClient _getDataService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new DataClientImpl(getWSLocation(request), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS ID mapping web service.
     * 
     * @param request
     *            The servlet request: used to access {@link SafeOnlineConfig#wsbase}.
     */
    public static NameIdentifierMappingClient getIdMappingService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getIdMappingService(request, privateKeyEntry);
    }

    @Override
    protected NameIdentifierMappingClient _getIdMappingService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NameIdentifierMappingClientImpl(getWSLocation(request), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS Security Token web service.
     * 
     * @param request
     *            The servlet request: used to access {@link SafeOnlineConfig#wsbase}.
     */
    public static SecurityTokenServiceClient getStsService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getStsService(request, privateKeyEntry);
    }

    @Override
    protected SecurityTokenServiceClient _getStsService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new SecurityTokenServiceClientImpl(getWSLocation(request), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification consumer web service.
     * 
     * @param request
     *            The servlet request: used to access {@link SafeOnlineConfig#wsbase}.
     */
    public static NotificationConsumerClient getNotificationConsumerService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getNotificationConsumerService(request, privateKeyEntry);
    }

    @Override
    protected NotificationConsumerClient _getNotificationConsumerService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationConsumerClientImpl(getWSLocation(request), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification producer web service.
     * 
     * @param request
     *            The servlet request: used to access {@link SafeOnlineConfig#wsbase}.
     */
    public static NotificationProducerClient getNotificationProducerService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getNotificationProducerService(request, privateKeyEntry);
    }

    @Override
    protected NotificationProducerClient _getNotificationProducerService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationProducerClientImpl(getWSLocation(request), certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification subscription manager web service.
     * 
     * @param request
     *            The servlet request: used to access {@link SafeOnlineConfig#wsbase}.
     */
    public static NotificationSubscriptionManagerClient getNotificationSubscriptionService(HttpServletRequest request,
                                                                                           PrivateKeyEntry privateKeyEntry) {

        return getInstance()._getNotificationSubscriptionService(request, privateKeyEntry);
    }

    @Override
    protected NotificationSubscriptionManagerClient _getNotificationSubscriptionService(HttpServletRequest request,
                                                                                        PrivateKeyEntry privateKeyEntry) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationSubscriptionManagerClientImpl(getWSLocation(request), certificate, privateKey);
    }

    private static String getWSLocation(HttpServletRequest request) {

        SafeOnlineConfig safeOnlineConfig = SafeOnlineConfig.load(request);
        return safeOnlineConfig.wsbase();
    }
}
