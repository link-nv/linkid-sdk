/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.data;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.Agent;
import net.link.safeonline.performance.console.jgroups.AgentState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;

/**
 * <h2>{@link ConsoleAgent}<br>
 * <sub>Proxy that maintains the status of the remote agent and provides access
 * to its functionality.</sub></h2>
 *
 * <p>
 * This is a proxy for the remote agent and provides access to all functionality
 * offered and all state made available by the agent. It takes care of keeping
 * the status information synchronised by a daemon thread that checks the remote
 * status every two seconds.
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ConsoleAgent implements Agent {

	private static final long serialVersionUID = 1L;
	static final Log LOG = LogFactory.getLog(ConsoleAgent.class);

	private ScenarioRemoting agentRemoting;
	private Address agentAddress;
	private boolean healthy;
	private AgentState transit;
	private AgentState state;
	private Exception error;
	private Set<String> scenarios;
	private Set<ScenarioExecution> executions;
	public boolean autoUpdate;

	/**
	 * Create a new {@link ConsoleAgent} component based off the agent at the
	 * given {@link Address}.
	 */
	public ConsoleAgent(Address agentAddress) {

		this.agentRemoting = ConsoleData.getRemoting();
		this.agentAddress = agentAddress;
		this.autoUpdate = true;
		this.healthy = true;

		new UpdateAgentState().start();
	}

	/**
	 * @param autoUpdate
	 *            <code>true</code> to sync the stats with the remote agent at
	 *            a certain interval. <code>false</code> to suspend this
	 *            updating (until set to <code>true</code> again).
	 */
	public void setAutoUpdate(boolean autoUpdate) {

		this.autoUpdate = autoUpdate;
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
		ConsoleData.fireAgentStatus(this);
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
	 * <b>Temporarily</b> change the <b>local</b> transition state of the
	 * agent.<br>
	 * <br>
	 * You should only use this to set the transition state just before making a
	 * request to the remote agent that will result in the same state transition
	 * if all goes well in order to have the state reflected in the UI sooner.
	 */
	public void setTransit(AgentState transit) {

		this.transit = transit;

		ConsoleData.fireAgentStatus(this);
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
	public ScenarioExecution getCharts(Date startTime) {

		return this.agentRemoting.getCharts(this.agentAddress, startTime);
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

	public void updateState() {

		try {
			this.transit = notifyOnChange(this.transit, this.agentRemoting
					.getTransit(this.agentAddress));
			this.state = notifyOnChange(this.state, this.agentRemoting
					.getState(this.agentAddress));
			this.error = notifyOnChange(this.error, this.agentRemoting
					.getError(this.agentAddress));

			// Only sync these if a scenario is deployed.
			if (AgentState.UPLOAD.compareTo(this.state) < 0) {
				this.scenarios = notifyOnChange(this.scenarios,
						this.agentRemoting.getScenarios(this.agentAddress));
				this.executions = notifyOnChange(this.executions,
						this.agentRemoting.getExecutions(this.agentAddress));
			}
		}

		catch (IllegalStateException e) {
			this.state = notifyOnChange(this.state, null);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private <V> V notifyOnChange(V oldValue, V newValue) {

		// Don't accept new value when autoUpdate has been disabled.
		if (Thread.currentThread() instanceof UpdateAgentState
				&& !this.autoUpdate)
			return oldValue;

		// Equals is broken for non-sorted sets when the order gets shaken up.
		Object fixed1 = oldValue, fixed2 = newValue;
		if (oldValue instanceof Set && !(oldValue instanceof SortedSet))
			fixed1 = new TreeSet<Object>((Set<? extends Object>) oldValue);
		if (newValue instanceof Set && !(newValue instanceof SortedSet))
			fixed2 = new TreeSet<Object>((Set<? extends Object>) newValue);

		if (fixed1 != null) {
			if (fixed2 == null || !fixed1.equals(fixed2))
				ConsoleData.fireAgentStatus(this);
		}

		else if (fixed2 != null)
			ConsoleData.fireAgentStatus(this);

		return newValue;
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
				if (ConsoleAgent.this.autoUpdate)
					updateState();

				try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
