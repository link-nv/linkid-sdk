/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.Agent;
import net.link.safeonline.performance.console.jgroups.AgentState;
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
public class ConsoleAgent implements Agent {

	private static final long serialVersionUID = 1L;
	static final Log LOG = LogFactory.getLog(ConsoleAgent.class);

	private List<AgentStatusListener> agentStatusListeners;
	private ScenarioRemoting agentRemoting;
	private Address agentAddress;
	private boolean healthy;
	private AgentState transit = AgentState.RESET;
	private AgentState state = AgentState.RESET;
	private Exception error;
	private Set<String> scenarios;
	private Set<ScenarioExecution> executions;

	/**
	 * Create a new {@link ConsoleAgent} component based off the agent at the
	 * given {@link Address}.
	 */
	public ConsoleAgent(Address agentAddress) {

		this.agentStatusListeners = new ArrayList<AgentStatusListener>();
		this.agentRemoting = ConsoleData.getRemoting();
		this.agentAddress = agentAddress;
		this.healthy = true;

		new UpdateAgentState().start();
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

	/**
	 * Define an object that should be notified when this agent changes. This
	 * should be an object that can fire the appropriate events in the UI
	 * required to render the change in this {@link ConsoleAgent}'s status.
	 */
	public void addAgentStatusListener(AgentStatusListener agentStatusListener) {

		if (!this.agentStatusListeners.contains(agentStatusListener))
			this.agentStatusListeners.add(agentStatusListener);
	}

	/**
	 * Manually fire an agent status event forcing the UI to update itself for
	 * this agent.
	 *
	 * @param descr
	 */
	public void fireAgentStatus() {

		for (AgentStatusListener listener : this.agentStatusListeners)
			listener.statusChanged(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		String transitStr = AgentState.RESET.getTransitioning();
		String stateStr = AgentState.RESET.getTransitioning();
		if (null != this.transit)
			transitStr = this.transit.getTransitioning();
		if (null != this.state)
			stateStr = this.state.getState();

		String health = this.healthy ? "Healthy" : "Unavailable";
		return String.format("%s: [%s]", health, stateStr, transitStr,
				this.agentAddress);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {

		return this.agentAddress.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof ConsoleAgent))
			return false;

		return this.agentAddress.equals(((ConsoleAgent) obj).agentAddress);
	}

	/**
	 * Retrieve the JGroups address of this {@link ConsoleAgent}.
	 */
	public Address getAddress() {

		return this.agentAddress;
	}

	/**
	 * Will never be <code>null</code>.
	 *
	 * {@inheritDoc}
	 */
	public AgentState getState() {

		if (this.state == null)
			this.state = AgentState.RESET;

		return this.state;
	}

	/**
	 * {@inheritDoc}
	 */
	public void resetTransit() {

		this.agentRemoting.resetTransit(this.agentAddress);
		updateState();
	}

	/**
	 * {@inheritDoc}
	 */
	public AgentState getTransit() {

		return this.transit;
	}

	/**
	 * {@inheritDoc}
	 */
	public Exception getError() {

		return this.error;
	}

	/**
	 * {@inheritDoc}
	 */
	public ScenarioExecution getStats(Integer execution) {

		return this.agentRemoting.getStats(this.agentAddress, execution);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<ScenarioExecution> getExecutions() {

		return this.executions;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getScenarios() {

		return this.scenarios;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean actionRequest(AgentState action) {

		try {
			return this.agentRemoting.actionRequest(this.agentAddress, action);
		} finally {
			updateState();
		}
	}

	public void updateState() {

		notifyOnChange(this.transit, this.transit = this.agentRemoting
				.getTransit(this.agentAddress));
		notifyOnChange(this.state, this.state = this.agentRemoting
				.getState(this.agentAddress));
		notifyOnChange(this.error, this.error = this.agentRemoting
				.getError(this.agentAddress));
		notifyOnChange(this.scenarios, this.scenarios = this.agentRemoting
				.getScenarios(this.agentAddress));
		notifyOnChange(this.executions, this.executions = this.agentRemoting
				.getExecutions(this.agentAddress));
	}

	@SuppressWarnings("unchecked")
	private void notifyOnChange(Object o1, Object o2) {

		// Equals is broken for non-sorted sets when the order gets shaken up.
		if (o1 instanceof Set && !(o1 instanceof SortedSet))
			o1 = new TreeSet<Object>((Set<? extends Object>) o1);
		if (o2 instanceof Set && !(o2 instanceof SortedSet))
			o2 = new TreeSet<Object>((Set<? extends Object>) o2);

		if (o1 != null) {
			if (o2 == null || !o1.equals(o2))
				fireAgentStatus();
		}

		else if (o2 != null)
			fireAgentStatus();
	}

	private class UpdateAgentState extends Thread {

		private static final long INTERVAL = 2000;

		public UpdateAgentState() {

			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {

			while (true) {
				try {
					updateState();
				} catch (Exception e) {
				}

				try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
