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

import net.link.safeonline.performance.console.jgroups.AgentDiscoverer;

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

	private Map<Address, Agent> agents;
	private AgentDiscoverer agentDiscoverer;
	private String hostname = "localhost";
	private int port = 8443, workers = 5;

	public ConsoleData() {

		this.agents = new HashMap<Address, Agent>();
		this.agentDiscoverer = new AgentDiscoverer();
	}

	public Address getSelf() {

		return this.agentDiscoverer.getSelf();
	}

	/**
	 * @return an unmodifiable view of the currently known agents for read-only
	 *         access.
	 */
	public synchronized Map<Address, Agent> getAgents() {

		return Collections.unmodifiableMap(new HashMap<Address, Agent>(
				this.agents));
	}

	/**
	 * Retrieve the {@link Agent} object for a given address. If there is no
	 * such object yet, and the {@link Address} is part of the group; create an
	 * {@link Agent} object for it.
	 */
	public synchronized Agent getAgent(Address agentAddress) {

		Agent agent = this.agents.get(agentAddress);
		if (null == agent
				&& this.agentDiscoverer.getMembers().contains(agentAddress))
			this.agents.put(agentAddress, agent = new Agent(agentAddress));

		return agent;
	}

	/**
	 * Remove {@link Agent} objects for agents that disappeared from the group.
	 * 
	 * @return All agents that were removed.
	 */
	public synchronized List<Agent> removeStaleAgents() {

		List<Agent> staleAgents = new ArrayList<Agent>();
		for (Address agentAddress : getAgents().keySet())
			if (!this.agentDiscoverer.getMembers().contains(agentAddress))
				staleAgents.add(this.agents.remove(agentAddress));

		return staleAgents;
	}

	/**
	 * @return the agentDiscoverer
	 */
	public AgentDiscoverer getAgentDiscoverer() {

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
}
