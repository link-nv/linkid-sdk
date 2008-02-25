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

	/**
	 * Create a new {@link ConsoleAgent} component based off the agent at the
	 * given {@link Address}.
	 */
	public ConsoleAgent(Address agentAddress) {

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
	public ScenarioExecution getStats(Date startTime) {

		return this.agentRemoting.getStats(this.agentAddress, startTime);
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
			notifyOnChange(this.transit, this.transit = this.agentRemoting
					.getTransit(this.agentAddress));
			notifyOnChange(this.state, this.state = this.agentRemoting
					.getState(this.agentAddress));
			notifyOnChange(this.error, this.error = this.agentRemoting
					.getError(this.agentAddress));

			// Only sync these if a scenario is deployed.
			if (AgentState.UPLOAD.compareTo(this.state) < 0) {
				notifyOnChange(this.scenarios,
						this.scenarios = this.agentRemoting
								.getScenarios(this.agentAddress));
				notifyOnChange(this.executions,
						this.executions = this.agentRemoting
								.getExecutions(this.agentAddress));
			}
		}

		catch (IllegalStateException e) {
			notifyOnChange(this.state, this.state = null);

			e.printStackTrace();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void notifyOnChange(Object o1, Object o2) {

		Object fixed1 = o1, fixed2 = o2;

		// Equals is broken for non-sorted sets when the order gets shaken up.
		if (o1 instanceof Set && !(o1 instanceof SortedSet))
			fixed1 = new TreeSet<Object>((Set<? extends Object>) o1);
		if (o2 instanceof Set && !(o2 instanceof SortedSet))
			fixed2 = new TreeSet<Object>((Set<? extends Object>) o2);

		if (fixed1 != null) {
			if (fixed2 == null || !fixed1.equals(fixed2))
				ConsoleData.fireAgentStatus(this);
		}

		else if (fixed2 != null)
			ConsoleData.fireAgentStatus(this);
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
				updateState();

				try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
