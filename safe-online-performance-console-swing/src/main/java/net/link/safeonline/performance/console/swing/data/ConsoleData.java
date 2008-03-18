/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.AgentRemoting;
import net.link.safeonline.performance.console.swing.model.AgentSelectionListener;
import net.link.safeonline.performance.console.swing.model.ExecutionSelectionListener;
import net.link.safeonline.performance.console.swing.model.ExecutionSettingsListener;
import net.link.safeonline.performance.console.swing.ui.AgentStatusListener;

import org.jgroups.Address;

/**
 * <h2>{@link ConsoleData}<br>
 * <sub>A central location for console configuration data.</sub></h2>
 *
 * <p>
 * All configuration settings are kept in this class. The UI needs to make sure
 * to update this class whenever settings are modified and needs to read the
 * settings in from this class rather than trying to parse them out of the UI.
 * <br>
 * This includes the mappings of known agent addresses to {@link ConsoleAgent}
 * objects. It is the responsibility of the UI to call the appropriate methods (
 * {@link #getAgent(Address)} and {@link #removeStaleAgents()} ) whenever agent
 * addresses join or leave the group so that the mappings kept by this class are
 * up-to-date.
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ConsoleData {

	public static final Object lock = new Object();

	static final List<ExecutionSelectionListener> executionSelectionListeners = new ArrayList<ExecutionSelectionListener>();
	static final List<ExecutionSettingsListener> executionSettingsListeners = new ArrayList<ExecutionSettingsListener>();
	static final List<AgentSelectionListener> agentSelectionListeners = new ArrayList<AgentSelectionListener>();
	static final List<AgentStatusListener> agentStatusListeners = new ArrayList<AgentStatusListener>();

	private static final Map<Address, ConsoleAgent> agents = new HashMap<Address, ConsoleAgent>();
	private static final AgentRemoting agentDiscoverer = new AgentRemoting();
	private static final ScenarioRemoting remoting = new ScenarioRemoting();
	private static String hostname = "sebeco-dev-10";
	private static int port = 8443;

	static int workers = 5;
	private static long duration = 600000;
	static Set<ConsoleAgent> selectedAgents = new HashSet<ConsoleAgent>();
	static ScenarioExecution execution;
	private static String scenarioName;

	public static Address getSelf() {

		return ConsoleData.agentDiscoverer.getSelf();
	}

	/**
	 * @return an unmodifiable view of the currently known agents for read-only
	 *         access.
	 */
	public static synchronized Map<Address, ConsoleAgent> getAgents() {

		return Collections.unmodifiableMap(Collections
				.synchronizedMap(new HashMap<Address, ConsoleAgent>(
						ConsoleData.agents)));
	}

	/**
	 * Retrieve the {@link ConsoleAgent} object for a given address. If there is
	 * no such object yet, and the {@link Address} is part of the group; create
	 * an {@link ConsoleAgent} object for it.
	 */
	public static synchronized ConsoleAgent getAgent(Address agentAddress) {

		ConsoleAgent agent = ConsoleData.agents.get(agentAddress);
		if (null == agent
				&& ConsoleData.agentDiscoverer.hasMember(agentAddress))
			ConsoleData.agents.put(agentAddress, agent = new ConsoleAgent(
					agentAddress));

		return agent;
	}

	/**
	 * Remove {@link ConsoleAgent} objects for agents that disappeared from the
	 * group.
	 *
	 * @return All agents that were removed.
	 */
	public static synchronized List<ConsoleAgent> removeStaleAgents() {

		List<ConsoleAgent> staleAgents = new ArrayList<ConsoleAgent>();
		for (Address agentAddress : getAgents().keySet())
			if (!ConsoleData.agentDiscoverer.hasMember(agentAddress)) {
				ConsoleAgent staleAgent = ConsoleData.agents
						.remove(agentAddress);

				staleAgents.add(staleAgent);
				staleAgent.shutdown();
			}

		return staleAgents;
	}

	/**
	 * @return the agentDiscoverer
	 */
	public static AgentRemoting getAgentDiscoverer() {

		return ConsoleData.agentDiscoverer;
	}

	/**
	 * @param hostname
	 *            the hostname of the OLAS application.
	 */
	public static synchronized void setHostname(String hostname) {

		ConsoleData.hostname = hostname;
	}

	/**
	 * @return the hostname of the OLAS application.
	 */
	public static synchronized String getHostname() {

		return ConsoleData.hostname;
	}

	/**
	 * @param port
	 *            the port of the OLAS application.
	 */
	public static synchronized void setPort(int port) {

		ConsoleData.port = port;
	}

	/**
	 * @return the port of the OLAS application.
	 */
	public static synchronized int getPort() {

		return ConsoleData.port;
	}

	/**
	 * @return the amount of simultaneous threads that execute a scenario.
	 */
	public static synchronized int getWorkers() {

		return ConsoleData.workers;
	}

	/**
	 * @param workers
	 *            The amount of simultaneous threads that execute a scenario.
	 */
	public static synchronized void setWorkers(int workers) {

		ConsoleData.workers = workers;
	}

	/**
	 * @return The amount of time to keep the scenario running (in
	 *         milliseconds).
	 */
	public static long getDuration() {

		return ConsoleData.duration;
	}

	/**
	 * @param duration
	 *            The amount of time to keep the scenario running (in
	 *            milliseconds).
	 */
	public static void setDuration(long duration) {

		ConsoleData.duration = duration;
	}

	/**
	 * @return The instance that supplies remoting to the agent service.
	 */
	public static ScenarioRemoting getRemoting() {

		return ConsoleData.remoting;
	}

	/**
	 * Update the set of selected agents.
	 */
	public static void setSelectedAgents(Set<ConsoleAgent> selectedAgents) {

		ConsoleData.selectedAgents = selectedAgents;
		fireAgentSelection();
	}

	/**
	 * @return The selectedAgents of this {@link ConsoleData}.
	 */
	public static Set<ConsoleAgent> getSelectedAgents() {

		return Collections.unmodifiableSet(Collections
				.synchronizedSet(ConsoleData.selectedAgents));
	}

	/**
	 * @param execution
	 *            The execution to perform actions upon.
	 */
	public static void setExecution(ScenarioExecution execution) {

		ConsoleData.execution = execution;
		fireExecutionSelection();
	}

	/**
	 * @return The execution to perform actions upon.
	 */
	public static ScenarioExecution getExecution() {

		return ConsoleData.execution;
	}

	/**
	 * @param scenarioName
	 *            The fully classified name of the scenario that needs to be
	 *            executed.
	 */
	public static void setScenarioName(String scenarioName) {

		ConsoleData.scenarioName = scenarioName;
		fireExecutionSettings();
	}

	/**
	 * @return The fully classified name of the scenario that needs to be
	 *         executed.
	 */
	public static String getScenarioName() {

		return ConsoleData.scenarioName;
	}

	public static void fireExecutionSelection() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				synchronized (lock) {
					for (ExecutionSelectionListener listener : executionSelectionListeners)
						listener.executionSelected(execution);
				}
			}
		});
	}

	/**
	 * Make the given object listen to execution selection events.
	 */
	public static void addExecutionSelectionListener(
			ExecutionSelectionListener listener) {

		if (!executionSelectionListeners.contains(listener))
			executionSelectionListeners.add(listener);
	}

	public static void fireExecutionSettings() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				synchronized (lock) {
					for (ExecutionSettingsListener listener : executionSettingsListeners)
						listener.executionSettingsChanged();
				}
			}
		});
	}

	/**
	 * Make the given object listen to execution settings changes.
	 */
	public static void addExecutionSettingsListener(
			ExecutionSettingsListener listener) {

		if (!executionSettingsListeners.contains(listener))
			executionSettingsListeners.add(listener);
	}

	public static void fireAgentSelection() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				synchronized (lock) {
					for (AgentSelectionListener listener : agentSelectionListeners)
						listener.agentsSelected(selectedAgents);
				}
			}
		});
	}

	/**
	 * Make the given object listen to agent selection events.
	 */
	public static void addAgentSelectionListener(AgentSelectionListener listener) {

		if (!agentSelectionListeners.contains(listener))
			agentSelectionListeners.add(listener);
	}

	/**
	 * Manually fire an agent status event forcing the UI to update itself for
	 * this agent.
	 *
	 * @param agent
	 *            The agent whose status changed.
	 */
	public static void fireAgentStatus(final ConsoleAgent agent) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				synchronized (lock) {
					for (AgentStatusListener listener : agentStatusListeners)
						listener.statusChanged(agent);
				}
			}
		});
	}

	/**
	 * Make the given object listen to agent status changes.
	 */
	public static void addAgentStatusListener(
			AgentStatusListener agentStatusListener) {

		if (!agentStatusListeners.contains(agentStatusListener))
			agentStatusListeners.add(agentStatusListener);
	}
}
