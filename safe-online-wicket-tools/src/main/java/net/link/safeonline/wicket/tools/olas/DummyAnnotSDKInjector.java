/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.tools.olas;

import java.lang.reflect.Field;

import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.notification.consumer.NotificationConsumerClient;
import net.link.safeonline.sdk.ws.notification.producer.NotificationProducerClientImpl;
import net.link.safeonline.sdk.ws.notification.subscription.manager.NotificationSubscriptionManagerClient;
import net.link.safeonline.sdk.ws.sts.SecurityTokenServiceClient;
import net.link.safeonline.wicket.service.AnnotSDKInjector;
import net.link.safeonline.wicket.service.OlasService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.injection.IFieldValueFactory;


/**
 * <h2>{@link DummyAnnotSDKInjector}<br>
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
public class DummyAnnotSDKInjector extends AnnotSDKInjector {

    static final Log           LOG = LogFactory.getLog(DummyAnnotSDKInjector.class);

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

                    OlasService olasService = field.getAnnotation(OlasService.class);
                    if (olasService == null)
                        throw new IllegalStateException(field + " is not supported by " + getClass());

                    // Load the OLAS service determined by the field's type using the specified key store.
                    if (AttributeClient.class.isAssignableFrom(field.getType()))
                        return new DummyAnnotSDKInjector();

                    else if (DataClient.class.isAssignableFrom(field.getType()))
                        throw new UnsupportedOperationException("Not yet supported");

                    else if (NameIdentifierMappingClient.class.isAssignableFrom(field.getType()))
                        return new DummyNameIdentifierMappingClient();

                    else if (SecurityTokenServiceClient.class.isAssignableFrom(field.getType()))
                        throw new UnsupportedOperationException("Not yet supported");

                    else if (NotificationSubscriptionManagerClient.class.isAssignableFrom(field.getType()))
                        throw new UnsupportedOperationException("Not yet supported");

                    else if (NotificationProducerClientImpl.class.isAssignableFrom(field.getType()))
                        throw new UnsupportedOperationException("Not yet supported");

                    else if (NotificationConsumerClient.class.isAssignableFrom(field.getType()))
                        throw new UnsupportedOperationException("Not yet supported");

                    else
                        throw new UnsupportedOperationException("The type of " + field + " does not support any known OLAS services.");
                }
            };
        }

        return factory;
    }
}
