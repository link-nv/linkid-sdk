/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.management.JMException;
import javax.naming.NamingException;

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
	private List<byte[]> charts;
	private AgentState transit;
	private AgentState state;

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
	public List<byte[]> getCharts() {

		return this.charts;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCharts(List<byte[]> charts) {

		this.charts = charts;
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
			throw new IllegalStateException("No ongoing action to stop.");

		if (success)
			this.state = this.transit;

		this.transit = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void upload(byte[] application) throws IOException {

		try {
			this.deployer.upload(application);
			actionCompleted(true);
		}

		finally {
			// If transit != null we didn't complete the action successfully.
			if (this.transit != null)
				actionCompleted(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deploy() throws JMException, NamingException,
			MalformedURLException, IOException {

		try {
			this.deployer.deploy();
			actionCompleted(true);
		}

		finally {
			// If transit != null we didn't complete the action successfully.
			if (this.transit != null)
				actionCompleted(false);
		}
	}

	public void execute(final String hostname, final Integer workers,
			final Long duration) throws NamingException {

		new ScenarioExecutor(hostname, workers, duration, this).start();
	}
}
