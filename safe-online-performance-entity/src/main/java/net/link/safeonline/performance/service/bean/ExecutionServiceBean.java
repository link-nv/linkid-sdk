/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service.bean;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.ExecutionService;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * <h2>{@link ExecutionServiceBean}<br>
 * <sub>Service bean for {@link ExecutionEntity}.</sub></h2>
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
	public ExecutionEntity addExecution(String scenarioName, Integer agents,
			Integer workers, Date startTime, Long duration, String hostname,
			Boolean useSsl) {

		ExecutionEntity execution = new ExecutionEntity(scenarioName, agents,
				workers, startTime, duration, hostname, useSsl);
		this.em.persist(execution);

		return execution;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Set<Date> getExecutions() {

		Set<Date> executionIds = new HashSet<Date>();
		List<ExecutionEntity> executions = this.em.createNamedQuery(
				ExecutionEntity.findAll).getResultList();

		for (ExecutionEntity execution : executions)
			executionIds.add(execution.getStartTime());

		return executionIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ExecutionEntity getExecution(Date startTime) {

		return (ExecutionEntity) this.em.createNamedQuery(
				ExecutionEntity.findById).setParameter("startTime", startTime)
				.getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<DriverProfileEntity> getProfiles(Date startTime) {

		if (this.ctx == null)
			return getExecution(startTime).getProfiles();

		return this.ctx.getBusinessObject(ExecutionService.class).getExecution(
				startTime).getProfiles();
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScenarioTimingEntity start(ExecutionEntity execution) {

		ScenarioTimingEntity startTimeEntity = new ScenarioTimingEntity(
				execution);
		this.em.persist(startTimeEntity);

		return startTimeEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateSpeed(ExecutionEntity execution) {

		execution.setSpeed((Double) this.em.createNamedQuery(
				ExecutionEntity.calcSpeed).setParameter("execution", execution)
				.getSingleResult());
	}
}
