/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link AbstractBankServiceBean}<br>
 * <sub>Abstract class for the entity services.</sub></h2>
 * 
 * <p>
 * Provides access to the {@link EntityManager}.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public abstract class AbstractBankServiceBean<E> {

    static final Log                                                             LOG            = LogFactory
                                                                                                        .getLog(AbstractBankServiceBean.class);

    private static EntityManager                                                 defaultEntityManager;

    @PersistenceContext(unitName = "DemoCinemaEntityManager")
    EntityManager                                                                em             = defaultEntityManager;

    @SuppressWarnings("unchecked")
    private static final Map<Class<?>, Class<? extends AbstractBankServiceBean>> attachServices = new HashMap<Class<?>, Class<? extends AbstractBankServiceBean>>();

    {
        attachServices.put(getClass().getAnnotation(Attaches.class).value(), getClass());
    }


    /**
     * Install a default entity manager which will be used for any new services. This is mostly useful for installing an
     * entity manager in an environment where there is no enterprise container that provides one.
     */
    public static void setDefaultEntityManager(EntityManager entityManager) {

        defaultEntityManager = entityManager;
    }

    abstract E attachEntity(E entity);

    public <T> T attach(T entity) {

        for (Class<?> entityClass : AbstractBankServiceBean.attachServices.keySet())
            if (entity.getClass().isAssignableFrom(entityClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends AbstractBankServiceBean<T>> serviceClass = (Class<? extends AbstractBankServiceBean<T>>) attachServices
                        .get(entityClass);

                try {
                    String serviceBinding = serviceClass.getField("BINDING").get(null).toString();
                    try {
                        AbstractBankServiceBean<T> service = serviceClass.cast(new InitialContext()
                                .lookup(serviceBinding));

                        return service.attach(entity);
                    } catch (NamingException e) {
                        LOG.error("Required service not bound: " + serviceBinding, e);
                    }
                } catch (IllegalArgumentException e) {
                    LOG.error("[BUG] BINDING field is not static.", e);
                } catch (SecurityException e) {
                    LOG.error("[BUG] Access to BINDING field denied.", e);
                } catch (IllegalAccessException e) {
                    LOG.error("[BUG] BINDING field is inaccessible.", e);
                } catch (NoSuchFieldException e) {
                    LOG.error("[BUG] BINDING field is not declared with this service.", e);
                }
            }

        LOG.warn("Couldn't attach entity: " + entity.getClass());
        return entity;
    }
}
