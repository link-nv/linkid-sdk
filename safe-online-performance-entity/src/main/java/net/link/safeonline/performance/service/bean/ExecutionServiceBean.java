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
 * <h2>{@link ExecutionServiceBean} - [in short] (TODO).</h2>
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
