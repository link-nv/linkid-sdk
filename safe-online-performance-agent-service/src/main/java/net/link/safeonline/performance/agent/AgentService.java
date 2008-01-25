/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.agent;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.jgroups.AgentState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mbillemo
 *
 */
public class AgentService implements AgentServiceMBean {

	static final Log LOG = LogFactory.getLog(AgentService.class);

	private AgentBroadcaster broadcaster;
	private ScenarioDeployer deployer;
	private ScenarioExecutor executor;
	private ScenarioExecution stats;
	private AgentState transit;
	private AgentState state;
	private Exception error;

	public AgentService() {

		this.broadcaster = new AgentBroadcaster();
		this.deployer = new ScenarioDeployer();
		this.state = AgentState.RESET;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {

		this.broadcaster.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {

		this.broadcaster.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isStarted() {

		return this.broadcaster.isConnected();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGroup() {

		return this.broadcaster.getGroup();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setGroup(String group) {

		this.broadcaster.setGroup(group);
	}

	/**
	 * {@inheritDoc}
	 */
	public AgentState getState() {

		return this.state;
	}

	/**
	 * {@inheritDoc}
	 */
	public void resetTransit() {

		if (this.executor != null)
			this.executor.halt();

		this.transit = null;
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
	public ScenarioExecution getStats() {

		return this.stats;
	}

	/**
	 * @param stats
	 *            The statistics generated from the executed scenario.
	 */
	public void setStats(ScenarioExecution stats) {

		this.stats = stats;
	}

	/**
	 * {@inheritDoc}
	 */
	public Exception getError() {

		return this.error;
	}

	/**
	 * @param error
	 *            An error that occurred while interacting with this client.
	 */
	public void setError(Exception error) {

		this.error = error;
	}

	private boolean isLocked() {

		return this.transit != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean actionRequest(AgentState action) {

		if (isLocked())
			return false;

		this.transit = action;
		return true;
	}

	/**
	 * Call this to notify the agent that its current action was completed. It
	 * will receive a new status depending on whether the action was successful
	 * (state becomes transit) or failed (state remains). Either way, transit
	 * becomes <code>null</code> since no more action is happening.
	 */
	public void actionCompleted(Boolean success) {

		if (this.transit == null)
			LOG.warn("No ongoing action to stop.", new IllegalStateException());

		else if (success)
			this.state = this.transit;

		this.transit = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void upload(byte[] application) {

		try {
			setError(null);

			this.deployer.upload(application);
			actionCompleted(true);
		}

		catch (Exception e) {
			setError(e);
		} finally {
			// If transit != null we didn't complete the action successfully.
			if (this.transit != null)
				actionCompleted(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deploy() {

		try {
			setError(null);

			this.deployer.deploy();
			actionCompleted(true);
		}

		catch (Exception e) {
			setError(e);
		} finally {
			// If transit != null we didn't complete the action successfully.
			if (this.transit != null)
				actionCompleted(false);
		}
	}

	public void execute(String hostname, Integer agents, Integer workers,
			Long duration) {

		try {
			setError(null);

			this.executor = new ScenarioExecutor(hostname, agents, workers,
					duration, this);
			this.executor.start();
		}

		catch (Exception e) {
			setError(e);
			actionCompleted(false);
		}
	}
}
