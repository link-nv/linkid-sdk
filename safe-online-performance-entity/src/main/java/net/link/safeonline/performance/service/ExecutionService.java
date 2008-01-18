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
package net.link.safeonline.performance.service;

import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;

/**
 * <h2>{@link ExecutionService} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
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
