/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.link.safeonline.performance.console.jgroups.AgentStateListener;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;
import net.link.safeonline.performance.console.swing.model.AgentSelectionListener;

import org.jgroups.Address;

/**
 * A list that visualises agent status.
 *
 * @author mbillemo
 */
public class AgentsList extends JList implements AgentStateListener,
		ListSelectionListener, AgentStatusListener {

	private static final long serialVersionUID = 1L;

	private List<AgentSelectionListener> agentSelectionListeners;
	private DefaultListModel model;

	public AgentsList() {

		addListSelectionListener(this);
		setModel(this.model = new DefaultListModel());
		setCellRenderer(new AgentRenderer());
		setBackground(null);
		setOpaque(false);

		this.agentSelectionListeners = new ArrayList<AgentSelectionListener>();
		ConsoleData.getAgentDiscoverer().addAgentStateListener(
				this);
	}

	/**
	 * Make the given object listen to agent selection events.
	 */
	public void addAgentSelectionListener(AgentSelectionListener listener) {

		this.agentSelectionListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void membersChanged(List<Address> addresses) {

		// Check whether all agents are in the list and add them if need be.
		for (Address address : addresses)
			if (!address.equals(ConsoleData.getSelf())) {
				ConsoleAgent agent = ConsoleData
						.getAgent(address);
				if (agent != null && !this.model.contains(agent)) {
					this.model.addElement(agent);
					agent.addAgentStatusListener(this);

					int endIndex = getModel().getSize() - 1;
					addSelectionInterval(endIndex, endIndex);
				}
			}

		// Remove stale agents from the list.
		for (ConsoleAgent agent : ConsoleData.removeStaleAgents())
			this.model.removeElement(agent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentSuspected(Address agentAddress) {

		ConsoleAgent agent = ConsoleData.getAgent(agentAddress);
		if (null != agent)
			agent.setHealthy(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelClosed() {

	}

	/**
	 * {@inheritDoc}
	 */
	public void channelConnected() {

		membersChanged(ConsoleData.getAgentDiscoverer()
				.getMembers());
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelDisconnected() {

	}

	/**
	 * {@inheritDoc}
	 */
	public void channelReconnected(Address arg0) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void channelShunned() {

	}

	/**
	 * {@inheritDoc}
	 */
	public void valueChanged(ListSelectionEvent e) {

		Set<ConsoleAgent> selectedAgents = new HashSet<ConsoleAgent>();
		for (Object value : getSelectedValues())
			if (value instanceof ConsoleAgent)
				selectedAgents.add((ConsoleAgent) value);

		ConsoleData.setSelectedAgents(selectedAgents);
		notifyAgentSelectionListeners();
	}

	private void notifyAgentSelectionListeners() {

		Set<ConsoleAgent> selectedAgents = ConsoleData.getSelectedAgents();
		for (AgentSelectionListener listener : this.agentSelectionListeners)
			listener.agentsSelected(selectedAgents);
	}

	/**
	 * {@inheritDoc}
	 */
	public void statusChanged(ConsoleAgent agent) {

		repaint();

		if (ConsoleData.getSelectedAgents().contains(agent))
			notifyAgentSelectionListeners();
	}
}
