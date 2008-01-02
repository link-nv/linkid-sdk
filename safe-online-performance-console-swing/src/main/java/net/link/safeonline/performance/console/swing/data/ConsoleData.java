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

	private static ConsoleData instance;
	private Map<Address, ConsoleAgent> agents;
	private AgentRemoting agentDiscoverer;
	private ScenarioRemoting remoting;
	private String hostname = "localhost";
	private int port = 8443, workers = 5;
	private long duration = 60000;

	private ConsoleData() {

		this.agents = new HashMap<Address, ConsoleAgent>();
		this.agentDiscoverer = new AgentRemoting();
		this.remoting = new ScenarioRemoting();
	}

	public static ConsoleData getInstance() {

		if (null == instance)
			instance = new ConsoleData();

		return instance;
	}

	public Address getSelf() {

		return this.agentDiscoverer.getSelf();
	}

	/**
	 * @return an unmodifiable view of the currently known agents for read-only
	 *         access.
	 */
	public synchronized Map<Address, ConsoleAgent> getAgents() {

		return Collections.unmodifiableMap(new HashMap<Address, ConsoleAgent>(
				this.agents));
	}

	/**
	 * Retrieve the {@link ConsoleAgent} object for a given address. If there is
	 * no such object yet, and the {@link Address} is part of the group; create
	 * an {@link ConsoleAgent} object for it.
	 */
	public synchronized ConsoleAgent getAgent(Address agentAddress) {

		ConsoleAgent agent = this.agents.get(agentAddress);
		if (null == agent && this.agentDiscoverer.hasMember(agentAddress))
			this.agents.put(agentAddress,
					agent = new ConsoleAgent(agentAddress));

		return agent;
	}

	/**
	 * Remove {@link ConsoleAgent} objects for agents that disappeared from the
	 * group.
	 * 
	 * @return All agents that were removed.
	 */
	public synchronized List<ConsoleAgent> removeStaleAgents() {

		List<ConsoleAgent> staleAgents = new ArrayList<ConsoleAgent>();
		for (Address agentAddress : getAgents().keySet())
			if (!this.agentDiscoverer.hasMember(agentAddress))
				staleAgents.add(this.agents.remove(agentAddress));

		return staleAgents;
	}

	/**
	 * @return the agentDiscoverer
	 */
	public AgentRemoting getAgentDiscoverer() {

		return this.agentDiscoverer;
	}

	/**
	 * @param hostname
	 *            the hostname of the OLAS application.
	 */
	public synchronized void setHostname(String hostname) {

		this.hostname = hostname;
	}

	/**
	 * @return the hostname of the OLAS application.
	 */
	public synchronized String getHostname() {

		return this.hostname;
	}

	/**
	 * @param port
	 *            the port of the OLAS application.
	 */
	public synchronized void setPort(int port) {

		this.port = port;
	}

	/**
	 * @return the port of the OLAS application.
	 */
	public synchronized int getPort() {

		return this.port;
	}

	/**
	 * @return the amount of simultaneous threads that execute a scenario.
	 */
	public synchronized int getWorkers() {

		return this.workers;
	}

	/**
	 * @param workers
	 *            The amount of simultaneous threads that execute a scenario.
	 */
	public synchronized void setWorkers(int workers) {

		this.workers = workers;
	}

	/**
	 * @return The amount of time to keep the scenario running (in
	 *         milliseconds).
	 */
	public long getDuration() {

		return this.duration;
	}

	/**
	 * @param duration
	 *            The amount of time to keep the scenario running (in
	 *            milliseconds).
	 */
	public void setDuration(long duration) {

		this.duration = duration;
	}

	/**
	 * @return The instance that supplies remoting to the agent service.
	 */
	public ScenarioRemoting getRemoting() {

		return this.remoting;
	}
}
