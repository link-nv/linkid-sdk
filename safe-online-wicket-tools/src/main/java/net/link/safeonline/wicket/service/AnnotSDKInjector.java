/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.service;

import java.lang.reflect.Field;
import java.security.KeyStore.PrivateKeyEntry;

import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.sdk.ws.OlasServiceFactory;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClient;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClientImpl;
import net.link.safeonline.sdk.ws.notification.subscription.manager.NotificationSubscriptionManagerClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.markup.html.WebPage;


/**
 * <h2>{@link AnnotSDKInjector}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 7, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class AnnotSDKInjector extends ConfigurableInjector {

    static final Log           LOG = LogFactory.getLog(AnnotSDKInjector.class);

    private IFieldValueFactory factory;


    /**
     * {@inheritDoc}
     */
    @Override
    protected IFieldValueFactory getFieldValueFactory() {

        if (factory == null) {
            factory = new IFieldValueFactory() {

                public boolean supportsField(Field field) {

                    return field.isAnnotationPresent(OlasService.class);
                }

                public Object getFieldValue(Field field, Object fieldOwner) {

                    HttpServletRequest request = WicketUtil.toServletRequest(((WebPage) fieldOwner).getRequest());

                    OlasService olasService = field.getAnnotation(OlasService.class);
                    if (olasService == null)
                        throw new IllegalStateException(field + " is not supported by " + getClass());

                    try {
                        // Load the specified key store.
                        PrivateKeyEntry privateKeyEntry = olasService.keyStore().newInstance()._getPrivateKeyEntry();

                        // Load the OLAS service determined by the field's type using the specified key store.
                        if (AttributeClient.class.isAssignableFrom(field.getType()))
                            return OlasServiceFactory.getAttributeService(request, privateKeyEntry);

                        else if (DataClient.class.isAssignableFrom(field.getType()))
                            return OlasServiceFactory.getDataService(request, privateKeyEntry);

                        else if (NameIdentifierMappingClient.class.isAssignableFrom(field.getType()))
                            return OlasServiceFactory.getIdMappingService(request, privateKeyEntry);

                        else if (SecurityTokenServiceClient.class.isAssignableFrom(field.getType()))
                            return OlasServiceFactory.getStsService(request, privateKeyEntry);

                        else if (NotificationSubscriptionManagerClient.class.isAssignableFrom(field.getType()))
                            return OlasServiceFactory.getNotificationSubscriptionService(request, privateKeyEntry);

                        else if (NotificationProducerClientImpl.class.isAssignableFrom(field.getType()))
                            return OlasServiceFactory.getNotificationProducerService(request, privateKeyEntry);

                        else if (NotificationConsumerClient.class.isAssignableFrom(field.getType()))
                            return OlasServiceFactory.getNotificationConsumerService(request, privateKeyEntry);

                        else
                            throw new UnsupportedOperationException("The type of " + field + " does not support any known OLAS services.");
                    }

                    catch (InstantiationException e) {
                        LOG.error("Keystore class is not instantiatable for: " + field, e);
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        LOG.error("Keystore class or default constructor is not accessible for: " + field, e);
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        return factory;
    }
}
