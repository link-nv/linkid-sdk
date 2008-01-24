/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link ProfilingServiceBean} - Abstract class for the entity services.</h2>
 * <p>
 * Provides access to the {@link EntityManager}.
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public abstract class ProfilingServiceBean {

	static final Log LOG = LogFactory.getLog(ProfilingServiceBean.class);

	@PersistenceContext(unitName = "AgentEntityManager")
	EntityManager em;

	void persist(Object object) {

		if (this.em != null)
			this.em.persist(object);

		else
			LOG.warn("No entity manager available!");

	}

	Query createNamedQuery(String query) {

		if (this.em != null)
			return this.em.createNamedQuery(query);

		LOG.warn("No entity manager available!");
		throw new IllegalStateException(
				"Can't execute query; no entity manager available.");
	}
}
