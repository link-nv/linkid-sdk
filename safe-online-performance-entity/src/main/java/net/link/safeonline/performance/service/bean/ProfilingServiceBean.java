/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.service.bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link ProfilingServiceBean} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
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
