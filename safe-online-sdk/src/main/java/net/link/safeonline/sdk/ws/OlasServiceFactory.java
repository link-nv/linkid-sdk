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
public class OlasServiceFactory {

    private static final String WS_LOCATION = "WsLocation";


    /**
     * Retrieve a proxy to the OLAS attribute web service.
     * 
     * @param httpRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used.
     */
    public static AttributeClient getAttributeService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        // Find the location of the OLAS web services to use.
        String wsLocation = httpRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new AttributeClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS data web service.
     * 
     * @param httpRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used.
     */
    public static DataClient getDataService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        // Find the location of the OLAS web services to use.
        String wsLocation = httpRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new DataClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS ID mapping web service.
     * 
     * @param httpRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used.
     */
    public static NameIdentifierMappingClient getIdMappingService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        // Find the location of the OLAS web services to use.
        String wsLocation = httpRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NameIdentifierMappingClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS Security Token web service.
     * 
     * @param httpRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used.
     */
    public static SecurityTokenServiceClient getStsService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        // Find the location of the OLAS web services to use.
        String wsLocation = httpRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new SecurityTokenServiceClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification consumer web service.
     * 
     * @param httpRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used.
     */
    public static NotificationConsumerClient getNotificationConsumerService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        // Find the location of the OLAS web services to use.
        String wsLocation = httpRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationConsumerClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification producer web service.
     * 
     * @param httpRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used.
     */
    public static NotificationProducerClient getNotificationProducerService(HttpServletRequest httpRequest, PrivateKeyEntry privateKeyEntry) {

        // Find the location of the OLAS web services to use.
        String wsLocation = httpRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationProducerClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * Retrieve a proxy to the OLAS notification subscription manager web service.
     * 
     * @param httpRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used.
     */
    public static NotificationSubscriptionManagerClient getNotificationSubscriptionService(HttpServletRequest httpRequest,
                                                                                           PrivateKeyEntry privateKeyEntry) {

        // Find the location of the OLAS web services to use.
        String wsLocation = httpRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationSubscriptionManagerClientImpl(wsLocation, certificate, privateKey);
    }
}
