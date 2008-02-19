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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.entity.AgentTimeEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
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
			Integer workers, Date startTime, Long duration, String hostname) {

		ExecutionEntity execution = new ExecutionEntity(scenarioName, agents,
				workers, startTime, duration, hostname);
		this.em.persist(execution);

		return execution;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Set<Integer> getExecutions() {

		Set<Integer> executionIds = new HashSet<Integer>();
		List<ExecutionEntity> executions = this.em.createNamedQuery(
				ExecutionEntity.findAll).getResultList();

		for (ExecutionEntity execution : executions)
			executionIds.add(execution.getId());

		return executionIds;
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
	@SuppressWarnings("unchecked")
	public SortedSet<AgentTimeEntity> getExecutionTimes(
			ExecutionEntity execution) {

		return new TreeSet<AgentTimeEntity>(this.em.createNamedQuery(
				ExecutionEntity.getTimes).setParameter("execution", execution)
				.getResultList());
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<DriverProfileEntity> getProfiles(int executionId) {

		return this.ctx.getBusinessObject(ExecutionService.class).getExecution(
				executionId).getProfiles();
	}

	/**
	 * {@inheritDoc}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public AgentTimeEntity start(ExecutionEntity execution) {

		AgentTimeEntity startTimeEntity = new AgentTimeEntity(execution);
		this.em.persist(startTimeEntity);

		return startTimeEntity;
	}
}
