/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.service;

import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.StartTimeEntity;
import net.link.safeonline.performance.service.bean.ExecutionServiceBean;

/**
 * <h2>{@link ExecutionServiceBean} - Service bean for {@link ExecutionEntity}.</h2>
 *
 * <p>
 * Create {@link ExecutionEntity}s and manage the {@link StartTimeEntity}s
 * that they're linked with.
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface ExecutionService {

	public static final String BINDING = "SafeOnline/ExecutionService";

	/**
	 * Add an entry for a new scenario execution to the database.
	 */
	public ExecutionEntity addExecution(String scenarioName, String hostname);

	/**
	 * Retrieve an already created execution by its Id.
	 */
	public ExecutionEntity getExecution(int executionId);

	/**
	 * Retrieve all driver profiles that were created for execution with the
	 * given Id.
	 */
	public Set<DriverProfileEntity> getProfiles(int executionId);

	/**
	 * Add the time that a recently completed scenario had started.
	 */
	public void addStartTime(ExecutionEntity execution, long startTime);

}
