/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
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
 * <h2>{@link DriverProfileServiceBean} - Service bean for
 * {@link DriverProfileEntity}.</h2>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @see DriverProfileService
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
