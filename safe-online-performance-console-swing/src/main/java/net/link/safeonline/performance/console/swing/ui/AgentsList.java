/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import net.link.safeonline.performance.console.jgroups.AgentStateListener;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;

import org.jgroups.Address;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <h2>{@link AgentsList}<br>
 * <sub>A list that visualises agent status.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class AgentsList extends JPanel implements AgentStateListener {

	private static final long serialVersionUID = 1L;
	private Set<AgentPanel> agentPanels;
	private DefaultFormBuilder builder;

	public AgentsList() {

		FormLayout formLayout = new FormLayout("f:p:g");
		this.builder = new DefaultFormBuilder(formLayout, this);
		setBackground(Color.white);

		this.agentPanels = new HashSet<AgentPanel>();

		ConsoleData.getAgentDiscoverer().addAgentStateListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void membersChanged(List<Address> addresses) {

		// Check whether all agents are in the list and add them if need be.
		for (Address address : addresses)
			if (!address.equals(ConsoleData.getSelf())) {
				ConsoleAgent agent = ConsoleData.getAgent(address);
				if (agent != null && findPanel(agent) == null)
					addAgent(agent);
			}

		// Remove stale agents from the list.
		for (ConsoleAgent agent : ConsoleData.removeStaleAgents())
			removeAgent(agent);
	}

	private void addAgent(ConsoleAgent agent) {

		AgentPanel panel = new AgentPanel(this, agent);

		this.agentPanels.add(panel);
		this.builder.appendRow("p");
		this.builder.append(panel);
		validate();
	}

	private void removeAgent(ConsoleAgent agent) {

		AgentPanel panel = findPanel(agent);

		System.err.println("Removing " + agent);
		if (panel == null) {
			System.err.println(" -> not there.");
			return;
		}

		this.agentPanels.remove(panel);
		remove(panel);
		validate();

		System.err.println(" -> done.");
	}

	private AgentPanel findPanel(ConsoleAgent agent) {

		for (AgentPanel panel : this.agentPanels)
			if (panel.getAgent().equals(agent))
				return panel;

		return null;
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

		membersChanged(ConsoleData.getAgentDiscoverer().getMembers());
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
	 * Updates the selected agents in {@link ConsoleData}. Fired by
	 * {@link AgentPanel} when its selection status changes.
	 */
	public void fireListSelectionChanged() {

		Set<ConsoleAgent> selectedAgents = new HashSet<ConsoleAgent>();
		for (AgentPanel panel : this.agentPanels)
			if (panel.isSelected())
				selectedAgents.add(panel.getAgent());

		ConsoleData.setSelectedAgents(selectedAgents);
	}
}
