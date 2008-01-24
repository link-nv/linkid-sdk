/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.StartTimeEntity;
import net.link.safeonline.performance.service.ExecutionService;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link ExecutionServiceBean} - Service bean for {@link ExecutionEntity}.</h2>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @see ExecutionService
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = ExecutionService.BINDING)
public class ExecutionServiceBean extends ProfilingServiceBean implements
		ExecutionService {

	@Resource
	SessionContext ctx;

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ExecutionEntity addExecution(String scenarioName, String hostname) {

		ExecutionEntity execution = new ExecutionEntity(scenarioName, hostname);
		this.em.persist(execution);

		return execution;
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addStartTime(ExecutionEntity execution, long startTime) {

		StartTimeEntity startTimeEntity = new StartTimeEntity(startTime);
		this.em.persist(startTimeEntity);

		execution.getStartTimes().add(startTimeEntity);
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ExecutionEntity getExecution(int executionId) {

		return (ExecutionEntity) this.em.createNamedQuery(
				ExecutionEntity.findById).setParameter("executionId",
				executionId).getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<DriverProfileEntity> getProfiles(int executionId) {

		return this.ctx.getBusinessObject(ExecutionService.class).getExecution(
				executionId).getProfiles();
	}
}
