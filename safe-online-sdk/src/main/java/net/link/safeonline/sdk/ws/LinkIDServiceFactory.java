/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import net.link.safeonline.keystore.LinkIDKeyStore;
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
import net.link.safeonline.sdk.ws.notification.subscription.NotificationSubscriptionManagerClient;
import net.link.safeonline.sdk.ws.notification.subscription.NotificationSubscriptionManagerClientImpl;
import net.link.safeonline.sdk.ws.session.SessionTrackingClient;
import net.link.safeonline.sdk.ws.session.SessionTrackingClientImpl;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.xkms2.Xkms2Client;
import net.link.safeonline.sdk.ws.xkms2.Xkms2ClientImpl;
import org.jetbrains.annotations.NotNull;


/**
 * <h2>{@link LinkIDServiceFactory}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
public class LinkIDServiceFactory extends ServiceFactory {

    private static ServiceFactory instance = new LinkIDServiceFactory();

    protected LinkIDServiceFactory() {

    }

    private static ServiceFactory getInstance() {

        return instance;
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static AttributeClient getAttributeService() {

        return getAttributeService( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param keyStore keystore to get linkID service certificate / SSL certificate from
     *
     * @return proxy to the linkID attribute web service.
     */
    public static AttributeClient getAttributeService(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getAttributeService( keyStore._getPrivateKeyEntry(), //
                                                   keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                                   config().proto().maxTimeOffset(),
                                                   keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static AttributeClient getAttributeService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                      Long maxTimestampOffset, X509Certificate sslCertificate) {

        return getInstance()._getAttributeService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected AttributeClient _getAttributeService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                   Long maxTimestampOffset, X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new AttributeClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate, maxTimestampOffset,
                                        sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static DataClient getDataService() {

        return getDataService( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID data web service.
     *
     * @param keyStore linkID keystore to get linkID service and SSL certificate from used for validation
     *
     * @return proxy to the linkID data web service.
     */
    public static DataClient getDataService(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getDataService( keyStore._getPrivateKeyEntry(), //
                                              keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                              config().proto().maxTimeOffset(),
                                              keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID data web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID data web service.
     */
    public static DataClient getDataService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate, Long maxTimestampOffset,
                                            X509Certificate sslCertificate) {

        return getInstance()._getDataService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected DataClient _getDataService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate, Long maxTimestampOffset,
                                         X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new DataClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate, maxTimestampOffset,
                                   sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NameIdentifierMappingClient getIdMappingService() {

        return getIdMappingService( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service.
     *
     * @param keyStore linkID keystore to get linkID service and SSL certificate from used for validation
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NameIdentifierMappingClient getIdMappingService(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getIdMappingService( keyStore._getPrivateKeyEntry(), //
                                                   keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                                   config().proto().maxTimeOffset(),
                                                   keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID ID mapping web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID ID mapping web service.
     */
    public static NameIdentifierMappingClient getIdMappingService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                                  Long maxTimestampOffset, X509Certificate sslCertificate) {

        return getInstance()._getIdMappingService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected NameIdentifierMappingClient _getIdMappingService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                               Long maxTimestampOffset, X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NameIdentifierMappingClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate, maxTimestampOffset,
                                                    sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static SecurityTokenServiceClient getStsService() {

        return getStsService( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param keyStore linkID keystore to get linkID service and SSL certificate from used for validation
     *
     * @return proxy to the linkID attribute web service.
     */
    public static SecurityTokenServiceClient getStsService(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getStsService( keyStore._getPrivateKeyEntry(), //
                                             keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                             config().proto().maxTimeOffset(),
                                             keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID Security Token web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID Security Token web service.
     */
    public static SecurityTokenServiceClient getStsService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                           Long maxTimestampOffset, X509Certificate sslCertificate) {

        return getInstance()._getStsService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected SecurityTokenServiceClient _getStsService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                        Long maxTimestampOffset, X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new SecurityTokenServiceClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate, maxTimestampOffset,
                                                   sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID attribute web service.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NotificationConsumerClient getNotificationConsumerService() {

        return getNotificationConsumerService( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID notification consumer web service.
     *
     * @param keyStore linkID keystore to get linkID service and SSL certificate from used for validation
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NotificationConsumerClient getNotificationConsumerService(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getNotificationConsumerService( keyStore._getPrivateKeyEntry(), //
                                                              keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                                              config().proto().maxTimeOffset(),
                                                              keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID notification consumer web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID attribute web service.
     */
    public static NotificationConsumerClient getNotificationConsumerService(PrivateKeyEntry privateKeyEntry,
                                                                            X509Certificate serverCertificate, Long maxTimestampOffset,
                                                                            X509Certificate sslCertificate) {

        return getInstance()._getNotificationConsumerService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected NotificationConsumerClient _getNotificationConsumerService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                                         Long maxTimestampOffset, X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationConsumerClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate, maxTimestampOffset,
                                                   sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID notification producer web service.
     *
     * @return proxy to the linkID notification producer web service.
     */
    public static NotificationProducerClient getNotificationProducerService() {

        return getNotificationProducerService( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID notification producer web service.
     *
     * @param keyStore linkID keystore to get linkID service and SSL certificate from used for validation
     *
     * @return proxy to the linkID notification producer web service.
     */
    public static NotificationProducerClient getNotificationProducerService(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getNotificationProducerService( keyStore._getPrivateKeyEntry(), //
                                                              keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                                              config().proto().maxTimeOffset(),
                                                              keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID notification producer web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID notification producer web service.
     */
    public static NotificationProducerClient getNotificationProducerService(PrivateKeyEntry privateKeyEntry,
                                                                            X509Certificate serverCertificate, Long maxTimestampOffset,
                                                                            X509Certificate sslCertificate) {

        return getInstance()._getNotificationProducerService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected NotificationProducerClient _getNotificationProducerService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                                         Long maxTimestampOffset, X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationProducerClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate, maxTimestampOffset,
                                                   sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID notification subscription web service.
     *
     * @return proxy to the linkID notification subscription web service.
     */
    public static NotificationSubscriptionManagerClient getNotificationSubscriptionSvc() {

        return getNotificationSubscriptionSvc( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID notification subscription manager web service.
     *
     * @param keyStore linkID keystore to get linkID service and SSL certificate from used for validation
     *
     * @return proxy to the linkID notification subscription web service.
     */
    public static NotificationSubscriptionManagerClient getNotificationSubscriptionSvc(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getNotificationSubscriptionService( keyStore._getPrivateKeyEntry(), //
                                                                  keyStore.getOtherCertificates()
                                                                          .get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                                                  config().proto().maxTimeOffset(),
                                                                  keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID notification subscription manager web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID notification subscription web service.
     */
    public static NotificationSubscriptionManagerClient getNotificationSubscriptionSvc(PrivateKeyEntry privateKeyEntry,
                                                                                       X509Certificate serverCertificate,
                                                                                       Long maxTimestampOffset,
                                                                                       X509Certificate sslCertificate) {

        return getInstance()._getNotificationSubscriptionService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected NotificationSubscriptionManagerClient _getNotificationSubscriptionService(PrivateKeyEntry privateKeyEntry,
                                                                                        X509Certificate serverCertificate,
                                                                                        Long maxTimestampOffset,
                                                                                        X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new NotificationSubscriptionManagerClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate,
                                                              maxTimestampOffset, sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID session tracking web service.
     *
     * @return proxy to the linkID session tracking web service.
     */
    public static SessionTrackingClient getSessionTrackingService() {

        return getSessionTrackingService( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID session tracking web service.
     *
     * @param keyStore linkID keystore to get linkID service and SSL certificate from used for validation
     *
     * @return proxy to the linkID session tracking web service.
     */
    public static SessionTrackingClient getSessionTrackingService(@NotNull LinkIDKeyStore keyStore) {

        return getInstance()._getSessionTrackingService( keyStore._getPrivateKeyEntry(), //
                                                         keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SERVICE_ALIAS ),
                                                         config().proto().maxTimeOffset(),
                                                         keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID session tracking web service.
     *
     * @param privateKeyEntry    private key entry used to sign outgoing requests
     * @param serverCertificate  linkID server certificate for validation of signed incoming responses.
     * @param maxTimestampOffset maximum WS-Security timestamp offset (ms)
     * @param sslCertificate     linkID SSL certificate used for validation.
     *
     * @return proxy to the linkID session tracking web service.
     */
    public static SessionTrackingClient getSessionTrackingService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                                  Long maxTimestampOffset, X509Certificate sslCertificate) {

        return getInstance()._getSessionTrackingService( privateKeyEntry, serverCertificate, maxTimestampOffset, sslCertificate );
    }

    @Override
    protected SessionTrackingClient _getSessionTrackingService(PrivateKeyEntry privateKeyEntry, X509Certificate serverCertificate,
                                                               Long maxTimestampOffset, X509Certificate sslCertificate) {

        // Find the key and certificate of the application.
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new SessionTrackingClientImpl( config().web().wsBase(), certificate, privateKey, serverCertificate, maxTimestampOffset,
                                              sslCertificate );
    }

    /**
     * Retrieve a proxy to the linkID XKMS2 web service.
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static Xkms2Client getXkms2Client() {

        return getXkms2Client( config().linkID().app().keyStore() );
    }

    /**
     * Retrieve a proxy to the linkID XKMS 2 web service.
     *
     * @param keyStore keystore to get linkID service certificate / SSL certificate from
     *
     * @return proxy to the linkID XKMS2 web service.
     */
    public static Xkms2Client getXkms2Client(@NotNull LinkIDKeyStore keyStore) {

        return getXkms2Client( keyStore.getOtherCertificates().get( LinkIDKeyStore.LINKID_SSL_ALIAS ) );
    }

    /**
     * Retrieve a proxy to the linkID XKMS 2 web service.
     *
     * @param sslCertificate SSL certificate for validation of the linkID SSL certificate
     *
     * @return proxy to the linkID XKMS 2 web service.
     */
    public static Xkms2Client getXkms2Client(X509Certificate sslCertificate) {

        return getInstance()._getXkms2Client( sslCertificate );
    }

    @Override
    protected Xkms2Client _getXkms2Client(X509Certificate sslCertificate) {

        return new Xkms2ClientImpl( config().web().wsBase(), sslCertificate );
    }
}
