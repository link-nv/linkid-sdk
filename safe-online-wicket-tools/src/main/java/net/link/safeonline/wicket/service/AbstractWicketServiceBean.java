/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.service;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.Id;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link AbstractWicketServiceBean}<br>
 * <sub>Abstract class for the entity services.</sub></h2>
 * 
 * <p>
 * Provides access to the {@link EntityManager} and provides a way of re-attaching detached entities.
 * </p>
 * 
 * <p>
 * Implementing classes should still provide the entity manager; use this code:
 * 
 * <code>
 * &#64;PersistenceContext(unitName = "DemoCinemaEntityManager") protected EntityManager em = defaultEntityManager;
 * </code>
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public abstract class AbstractWicketServiceBean {

    protected static EntityManager defaultEntityManager;

    protected final Log            LOG = LogFactory.getLog(getClass());


    /**
     * Install a default entity manager which will be used for any new services. This is mostly useful for installing an entity manager in
     * an environment where there is no enterprise container that provides one.
     */
    public static void setDefaultEntityManager(EntityManager entityManager) {

        defaultEntityManager = entityManager;
    }

    protected abstract EntityManager getEntityManager();

    public <T> T attach(T entity) {

        // Find the primary key of the entity.
        Object primaryKey = null;
        for (Field field : entity.getClass().getDeclaredFields()) {
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                try {
                    field.setAccessible(true);
                    primaryKey = field.get(entity);
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                    LOG.error("No access to Id annotation of entity.", e);
                }
            }
        }

        if (primaryKey == null)
            throw new IllegalArgumentException("Tried to attach an object that is either not an entity or has no primary key annotation.");

        // Create a real entity by searching for the primary key.
        @SuppressWarnings("unchecked")
        Class<T> entityClass = (Class<T>) entity.getClass();
        return getEntityManager().find(entityClass, primaryKey);
    }
}
