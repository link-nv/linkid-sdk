/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.data;

import java.util.ArrayList;
import java.util.List;

import net.link.safeonline.performance.console.swing.ui.AgentStatusListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;

/**
 * This object keeps the status of communication between the console and the
 * agent it represents. It also features locking such that only one operation
 * would be executed upon it at once.
 * 
 * @author mbillemo
 * 
 */
public class Agent {

	private static Log LOG = LogFactory.getLog(Agent.class);

	private static final long serialVersionUID = 1L;

	private List<AgentStatusListener> agentStatusListeners;
	private Address agentAddress;
	private Exception error;
	private boolean healthy;
	private boolean selected;
	private State state;
	private State transit;

	private List<byte[]> charts;

	/**
	 * Create a new {@link Agent} component based off the agent at the given
	 * {@link Address}.
	 */
	public Agent(Address agentAddress) {

		this.agentStatusListeners = new ArrayList<AgentStatusListener>();
		this.agentAddress = agentAddress;
		this.state = State.RESET;

		setHealthy(true);
	}

	/**
	 * @param error
	 *            An error that occurred while interacting with this client.
	 */
	public void setError(Exception error) {

		this.error = error;

		if (null != error)
			LOG.error("Scenario Failed During Execution", error);
	}

	/**
	 * @return An error that occurred while interacting with this client.
	 */
	public Exception getError() {

		return this.error;
	}

	/**
	 * @return false if JGroups suspects this agent of being unavailable.
	 */
	public boolean isHealthy() {

		return this.healthy;
	}

	/**
	 * Set the JGroups health status of this agent.
	 */
	public void setHealthy(boolean healthy) {

		this.healthy = healthy;
		fireAgentStatus();
	}

	private boolean isLocked() {

		return this.transit != null;
	}

	/**
	 * @return <code>true</code> if agent is available for this action.
	 */
	public boolean startAction(State action) {

		if (isLocked())
			return false;

		this.transit = action;

		fireAgentStatus();
		return true;
	}

	/**
	 * Signal the current action has stopped with success or not. This will
	 * transit the agent into the state it was performing if successful.
	 * 
	 * @param success
	 *            <code>true</code> if the action was a success.
	 */
	public void stopAction(boolean success) {

		if (this.transit == null)
			throw new IllegalStateException("No ongoing action to stop.");

		if (success)
			this.state = this.transit;

		this.transit = null;

		fireAgentStatus();
	}

	/**
	 * @return <code>true</code> if this agent is to participate in actions.
	 */
	public boolean isSelected() {

		return this.selected;
	}

	/**
	 * @param selected
	 *            <code>true</code> if this agent is selected for actions.
	 */
	public void setSelected(boolean selected) {

		this.selected = selected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		String health = this.healthy ? "Healthy" : "Unavailable";
		String action = State.RESET.getTransitioning();
		if (null != this.transit)
			action = this.transit.getTransitioning();

		return String.format("[%s:%s:%s] %s", health, this.state.getState(),
				action, this.agentAddress);
	}

	/**
	 * Define an object that should be notified when this agent changes. This
	 * should be an object that can fire the appropriate events in the UI
	 * required to render the change in this {@link Agent}'s status.
	 */
	public void addAgentStatusListener(AgentStatusListener agentStatusListener) {

		if (!this.agentStatusListeners.contains(agentStatusListener))
			this.agentStatusListeners.add(agentStatusListener);
	}

	/**
	 * Retrieve the current state transition action of the agent.
	 */
	public State getAction() {

		return this.transit;
	}

	/**
	 * Retrieve the current state of the agent.
	 */
	public State getState() {

		return this.state;
	}

	/**
	 * Reset this agent's current state if it is not doing something already.
	 * 
	 * @return <code>true</code> if the agent was not locked.
	 */
	public boolean reset() {

		if (isLocked())
			return false;

		this.state = State.RESET;
		fireAgentStatus();

		return true;
	}

	/**
	 * Manually fire an agent status event forcing the UI to update itself for
	 * this agent.
	 */
	public void fireAgentStatus() {

		for (AgentStatusListener listener : this.agentStatusListeners)
			listener.statusChanged(this);
	}

	/**
	 * Retrieve the JGroups address of this {@link Agent}.
	 */
	public Address getAddress() {

		return this.agentAddress;
	}

	/**
	 * Retrieve charts created by this {@link Agent}'s scenario.
	 */
	public List<byte[]> getCharts() {

		return this.charts;
	}

	/**
	 * Save charts created by this {@link Agent}'s scenario.
	 */
	public void setCharts(List<byte[]> charts) {

		this.charts = charts;
	}

	public enum State {
		RESET("Ready", "Idle"), UPLOAD("Scenario Uploaded", "Receiving"), DEPLOY(
				"Scenario Deployed", "Deploying"), EXECUTE("Charts Available",
				"Executing");

		private String state;
		private String transitioning;

		private State(String state, String transitioning) {

			this.state = state;
			this.transitioning = transitioning;
		}

		public String getState() {

			return this.state;
		}

		public String getTransitioning() {

			return this.transitioning;
		}
	}
}
