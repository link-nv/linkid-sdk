/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.wicket.service.AbstractWicketServiceBean;


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
public abstract class AbstractCinemaServiceBean extends AbstractWicketServiceBean {

    @PersistenceContext(unitName = "DemoCinemaEntityManager")
    EntityManager em = defaultEntityManager;


    /**
     * {@inheritDoc}
     */
    @Override
    protected EntityManager getEntityManager() {

        return this.em;
    }
}
