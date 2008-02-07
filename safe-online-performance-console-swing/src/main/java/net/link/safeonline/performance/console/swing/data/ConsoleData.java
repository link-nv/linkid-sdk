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
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.AgentRemoting;

import org.jgroups.Address;

/**
 * Keeps a list of settings from the UI. This includes the location of OLAS and
 * the known mappings from agent addresses to agent objects. It is the
 * responsibility of the UI to call the appropriate methods (
 * {@link #getAgent(Address)} and {@link #removeStaleAgents()} ) whenever agent
 * addresses join or leave the group so that the mappings kept by this object
 * can be kept up-to-date.
 *
 * @author mbillemo
 *
 */
public class ConsoleData {

	private static Map<Address, ConsoleAgent> agents = new HashMap<Address, ConsoleAgent>();
	private static AgentRemoting agentDiscoverer = new AgentRemoting();
	private static ScenarioRemoting remoting = new ScenarioRemoting();
	private static String hostname = "localhost";
	private static int port = 8443, workers = 5;
	private static long duration = 60000;
	private static Set<ConsoleAgent> selectedAgents;
	private static ScenarioExecution execution;
	private static String scenarioName;

	public static Address getSelf() {

		return ConsoleData.agentDiscoverer.getSelf();
	}

	/**
	 * @return an unmodifiable view of the currently known agents for read-only
	 *         access.
	 */
	public static synchronized Map<Address, ConsoleAgent> getAgents() {

		return Collections.unmodifiableMap(new HashMap<Address, ConsoleAgent>(
				ConsoleData.agents));
	}

	/**
	 * Retrieve the {@link ConsoleAgent} object for a given address. If there is
	 * no such object yet, and the {@link Address} is part of the group; create
	 * an {@link ConsoleAgent} object for it.
	 */
	public static synchronized ConsoleAgent getAgent(Address agentAddress) {

		ConsoleAgent agent = ConsoleData.agents.get(agentAddress);
		if (null == agent && ConsoleData.agentDiscoverer.hasMember(agentAddress))
			ConsoleData.agents.put(agentAddress,
					agent = new ConsoleAgent(agentAddress));

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
			if (!ConsoleData.agentDiscoverer.hasMember(agentAddress))
				staleAgents.add(ConsoleData.agents.remove(agentAddress));

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
	}

	/**
	 * @return The selectedAgents of this {@link ConsoleData}.
	 */
	public static Set<ConsoleAgent> getSelectedAgents() {

		return Collections.unmodifiableSet(ConsoleData.selectedAgents);
	}

	/**
	 * @param execution
	 *            The execution to perform actions upon.
	 */
	public static void setExecution(ScenarioExecution execution) {

		ConsoleData.execution = execution;
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
	}

	/**
	 * @return The fully classified name of the scenario that needs to be
	 *         executed.
	 */
	public static String getScenarioName() {

		return ConsoleData.scenarioName;
	}
}
