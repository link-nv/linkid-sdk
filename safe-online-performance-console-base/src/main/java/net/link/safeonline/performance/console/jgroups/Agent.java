/*
 *   Copyright 2007, Maarten Billemont
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
package net.link.safeonline.performance.console.jgroups;

import java.util.Set;

import javax.naming.NamingException;

import net.link.safeonline.performance.console.ScenarioExecution;

/**
 * <h2>{@link Agent} - Interface to all actions that can be remotely performed
 * on an agent.</h2>
 *
 * <p>
 * <i>Dec 19, 2007</i>
 * </p>
 *
 * @author mbillemo
 */
public interface Agent {

	/**
	 * Retrieve the current state of the agent.
	 */
	public AgentState getState();

	/**
	 * Retrieve the current state transition action of the agent.
	 */
	public AgentState getTransit();

	/**
	 * Retrieve charts created by this {@link Agent}'s scenario.
	 */
	public ScenarioExecution getStats(Integer execution);

	/**
	 * The executionIds for which metadata is available.
	 */
	public Set<ScenarioExecution> getExecutions() throws NamingException;

	/**
	 * Retrieve all scenarios registered for use.
	 */
	public Set<String> getScenarios() throws NamingException;

	/**
	 * Reset the transition state, abort the action.
	 */
	public void resetTransit();

	/**
	 * @return An error that occurred while interacting with this client.
	 */
	public Exception getError();
}
