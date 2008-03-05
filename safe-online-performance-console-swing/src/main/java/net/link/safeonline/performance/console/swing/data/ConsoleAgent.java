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

import javax.naming.NameNotFoundException;

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
	private UpdateAgentState updater;

	/**
	 * Create a new {@link ConsoleAgent} component based off the agent at the
	 * given {@link Address}.
	 */
	public ConsoleAgent(Address agentAddress) {

		this.agentRemoting = ConsoleData.getRemoting();
		this.agentAddress = agentAddress;
		this.autoUpdate = true;
		this.healthy = true;

		this.updater = new UpdateAgentState();
		this.updater.start();
	}

	/**
	 * Order the agent to prepare for GC by stopping the update cycle.
	 */
	public void shutdown() {

		this.updater.shutdown();
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

		return String.format("%s: [%s:%s]", this.agentAddress, stateStr,
				transitStr);
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
			boolean isDeployed = this.state != null
					&& AgentState.UPLOAD.compareTo(this.state) < 0;

			synchronized (ConsoleData.lock) {
				this.transit = notifyOnChange(this.transit, this.agentRemoting
						.getTransit(this.agentAddress));
			}
			synchronized (ConsoleData.lock) {
				this.state = notifyOnChange(this.state, this.agentRemoting
						.getState(this.agentAddress));
			}
			synchronized (ConsoleData.lock) {
				this.error = notifyOnChange(this.error, this.agentRemoting
						.getError(this.agentAddress));
			}
			synchronized (ConsoleData.lock) {
				this.scenarios = notifyOnChange(this.scenarios,
						isDeployed ? this.agentRemoting
								.getScenarios(this.agentAddress) : null);
			}
			synchronized (ConsoleData.lock) {
				this.executions = notifyOnChange(this.executions,
						isDeployed ? this.agentRemoting
								.getExecutions(this.agentAddress) : null);
			}
		}

		catch (IllegalStateException e) {
			this.state = notifyOnChange(this.state, null);
		}

		catch (NameNotFoundException e) {
			this.scenarios = notifyOnChange(this.scenarios, null);
			this.executions = notifyOnChange(this.executions, null);
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
		Object fixedOld = oldValue, fixedNew = newValue;
		if (oldValue instanceof Set && !(oldValue instanceof SortedSet))
			fixedOld = new TreeSet<Object>((Set<? extends Object>) oldValue);
		if (newValue instanceof Set && !(newValue instanceof SortedSet))
			fixedNew = new TreeSet<Object>((Set<? extends Object>) newValue);

		if (fixedOld != null) {
			if (fixedNew == null || !fixedOld.equals(fixedNew))
				ConsoleData.fireAgentStatus(this);
		}

		else if (fixedNew != null)
			ConsoleData.fireAgentStatus(this);

		return newValue;
	}

	private class UpdateAgentState extends Thread {

		private static final long INTERVAL = 2000;
		private boolean shutdown;

		public UpdateAgentState() {

			setDaemon(true);
		}

		protected void shutdown() {

			this.shutdown = true;
			interrupt();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {

			while (!this.shutdown)
				try {
					if (ConsoleAgent.this.autoUpdate)
						updateState();

					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
				}
		}
	}
}
