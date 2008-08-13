/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link AbstractCinemaServiceBean}<br>
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
public abstract class AbstractCinemaServiceBean {

    static final Log             LOG = LogFactory.getLog(AbstractCinemaServiceBean.class);

    private static EntityManager defaultEntityManager;

    @PersistenceContext(unitName = "DemoCinemaEntityManager")
    EntityManager                em  = defaultEntityManager;


    /**
     * Install a default entity manager which will be used for any new services. This is mostly useful for installing an
     * entity manager in an environment where there is no enterprise container that provides one.
     */
    public static void setDefaultEntityManager(EntityManager entityManager) {

        defaultEntityManager = entityManager;
    }
}
