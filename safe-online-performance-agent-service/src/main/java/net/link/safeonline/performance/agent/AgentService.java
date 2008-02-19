/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.agent;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.scenario.ExecutionMetadata;
import net.link.safeonline.performance.scenario.ScenarioController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link AgentService}<br>
 * <sub>This class provides all functionality of the agent that is available to
 * the console.</sub></h2>
 * 
 * <p>
 * This MBean launches the broadcaster service that provides agent visibility in
 * JGroups and the deployer service that is used for deploying uploaded
 * scenarios. It also keeps the current state of the agent (see
 * {@link AgentState}).<br>
 * <br>
 * The agent service delegates requests for uploading, deploying, executing and
 * charting scenarios.<br>
 * <br>
 * Metadata on previously performed executions are also cached by this agent as
 * they are requested by the console.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class AgentService implements AgentServiceMBean {

	static final Log LOG = LogFactory.getLog(AgentService.class);

	private Map<Integer, ScenarioExecution> stats;
	private AgentBroadcaster broadcaster;
	private ScenarioDeployer deployer;
	private ScenarioExecutor executor;
	private AgentState transit;
	private AgentState state;
	private Exception error;

	public AgentService() {

		this.stats = new HashMap<Integer, ScenarioExecution>();
		this.deployer = new ScenarioDeployer();
		this.broadcaster = new AgentBroadcaster();
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

		// Check to see if the scenario bean was deployed behind our backs.
		if (this.state == null || AgentState.RESET.equals(this.state))
			try {
				this.state = getScenarioController() == null ? AgentState.RESET
						: AgentState.DEPLOY;
			} catch (NamingException e) {
				this.state = AgentState.RESET;
			}

		return this.state;
	}

	/**
	 * {@inheritDoc}
	 */
	public void resetTransit() {

		if (this.executor != null) {
			this.executor.halt();
			this.executor = null;
		} else
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
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ScenarioExecution getStats(Integer execution) {

		if (!actionRequest(AgentState.CHART))
			return null;

		try {
			return getExecution(execution, true);
		} finally {
			actionCompleted(this.stats.containsKey(execution));
		}
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
	public Set<String> getScenarios() {

		try {
			return getScenarioController().getScenarios();
		} catch (NamingException e) {
			return new HashSet<String>();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<ScenarioExecution> getExecutions() throws NamingException {

		Set<ScenarioExecution> executions = new HashSet<ScenarioExecution>();
		for (Integer executionId : getScenarioController().getExecutions())
			executions.add(getExecution(executionId, false));

		return executions;
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
	 * Request permission to start a certain action. If permission is granted,
	 * the agent is locked until {@link #actionCompleted(boolean)} is called.
	 *
	 * @return <code>true</code> if agent is available for this action.
	 */
	private boolean actionRequest(AgentState action) {

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
	public void actionCompleted(boolean success) {

		if (this.transit == null)
			LOG.warn("No ongoing action to stop.", new IllegalStateException());

		else if (success)
			this.state = this.transit;

		this.executor = null;
		this.transit = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void upload(byte[] application) {

		if (!actionRequest(AgentState.UPLOAD))
			return;

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

		if (!actionRequest(AgentState.DEPLOY))
			return;

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

	public void execute(String scenarioName, Integer agents, Integer workers,
			Long duration, String hostname, Date startTime) {

		if (!actionRequest(AgentState.EXECUTE))
			return;

		try {
			setError(null);

			ExecutionMetadata request = ExecutionMetadata.createRequest(
					scenarioName, agents, workers, startTime, duration,
					hostname);

			(this.executor = new ScenarioExecutor(request, this)).start();
		}

		catch (Exception e) {
			setError(e);
			actionCompleted(false);
		}
	}

	/**
	 * Returns execution metadata for the execution with the given ID. This
	 * method uses a memory cache to store executions in that have been
	 * previously retrieved. If no charts are requested, it is guaranteed to
	 * return an execution object without charts, even if charts have been
	 * cached already.
	 */
	private ScenarioExecution getExecution(Integer executionId, boolean charts) {

		if (executionId == null)
			return null;

		try {
			ScenarioExecution execution = this.stats.get(executionId);

			if (execution == null) {
				ExecutionMetadata metaData = getScenarioController()
						.getExecutionMetadata(executionId);

				this.stats.put(executionId, execution = new ScenarioExecution(
						executionId, metaData.getScenarioName(), metaData
								.getScenarioDescription(),
						metaData.getAgents(), metaData.getWorkers(), metaData
								.getStartTime(), metaData.getDuration(),
						metaData.getHostname(), metaData.getSpeed()));
			}

			if (charts)
				synchronized (execution) {
					if (execution.getCharts() == null)
						execution.setCharts(charts ? getScenarioController()
								.createCharts(executionId) : null);
				}

			else if (execution.getCharts() != null)
				execution = execution.clone();

			return execution;
		}

		catch (NamingException e) {
			throw new RuntimeException(
					"Can't query stats without a scenario deployed.", e);
		}
	}

	private ScenarioController getScenarioController() throws NamingException {

		return (ScenarioController) new InitialContext()
				.lookup(ScenarioController.BINDING);
	}
}
