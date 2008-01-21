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

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.service.DriverProfileService;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link DriverProfileServiceBean} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = DriverProfileService.BINDING)
public class DriverProfileServiceBean extends ProfilingServiceBean implements
		DriverProfileService {

	@Resource
	SessionContext ctx;

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DriverProfileEntity addProfile(String driverName,
			ExecutionEntity execution) {

		DriverProfileEntity profile = new DriverProfileEntity(driverName,
				execution);
		this.em.persist(profile);

		return profile;
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DriverProfileEntity getProfile(String driverName,
			ExecutionEntity execution) {

		try {
			LOG.debug("Looking for driver profile for '" + driverName
					+ "' in '" + execution.getScenarioName() + ":"
					+ execution.getHostname() + ":" + execution.getId() + "'");
			return (DriverProfileEntity) this.em.createNamedQuery(
					DriverProfileEntity.findByExecution).setParameter(
					"driverName", driverName).setParameter("execution",
					execution).getSingleResult();
		} catch (NoResultException e) {
			LOG.debug(" -> not found; creating.");
			return this.ctx.getBusinessObject(DriverProfileService.class)
					.addProfile(driverName, execution);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void register(DriverProfileEntity driverProfile,
			ProfileDataEntity data) {

		driverProfile.register(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public void register(DriverProfileEntity driverProfile,
			DriverExceptionEntity exception) {

		driverProfile.register(exception);
	}
}
