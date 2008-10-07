/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.javaee;

import java.lang.reflect.Field;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.PersistenceUnit;

import org.apache.wicket.proxy.IProxyTargetLocator;
import org.wicketstuff.javaee.EntityManagerFactoryLocator;
import org.wicketstuff.javaee.injection.AnnotJavaEEInjector;
import org.wicketstuff.javaee.injection.JavaEEProxyFieldValueFactory;
import org.wicketstuff.javaee.naming.IJndiNamingStrategy;


/**
 * <h2>{@link DummyAnnotJavaEEInjector}<br>
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
public class DummyAnnotJavaEEInjector extends AnnotJavaEEInjector {

    @Override
    protected void initFactory(IJndiNamingStrategy namingStrategy) {

        this.factory = new JavaEEProxyFieldValueFactory(namingStrategy) {

            @Override
            protected IProxyTargetLocator getProxyTargetLocator(Field field) {

                if (field.isAnnotationPresent(EJB.class))
                    return new DummyJavaEEBeanLocator(field.getAnnotation(EJB.class).name(), field.getType(),
                            this.namingStrategy);

                if (field.isAnnotationPresent(PersistenceUnit.class))
                    return new EntityManagerFactoryLocator(field.getAnnotation(PersistenceUnit.class).unitName());

                if (field.isAnnotationPresent(Resource.class))
                    return new DummyJndiObjectLocator(field.getAnnotation(Resource.class).name(), field.getType());

                return null;
            }
        };
    }
}
