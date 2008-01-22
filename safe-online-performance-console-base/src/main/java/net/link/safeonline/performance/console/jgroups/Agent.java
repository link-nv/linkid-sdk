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

import net.link.safeonline.performance.console.ScenarioExecution;

/**
 * <h2>{@link Agent} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
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
	public ScenarioExecution getStats();

	/**
	 * Request permission to start a certain action. If permission is granted,
	 * the agent is locked until {@link #actionCompleted(boolean)} is called.
	 *
	 * @return <code>true</code> if agent is available for this action.
	 */
	public boolean actionRequest(AgentState action);

	/**
	 * Reset the transition state, abort the action.
	 */
	public void resetTransit();

	/**
	 * @return An error that occurred while interacting with this client.
	 */
	public Exception getError();
}
