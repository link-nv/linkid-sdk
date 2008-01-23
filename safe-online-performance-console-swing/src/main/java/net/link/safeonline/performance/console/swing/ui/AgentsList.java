/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.jgroups.AgentStateListener;
import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;

import org.jgroups.Address;

/**
 * A list that visualises agent status.
 *
 * @author mbillemo
 */
public class AgentsList extends JList implements AgentStateListener,
		ListSelectionListener, AgentStatusListener {

	private static final long serialVersionUID = 1L;

	private DefaultListModel model;
	private ScenarioChooser scenarioChooser;

	public AgentsList(ScenarioChooser scenarioChooser) {

		addListSelectionListener(this);
		setModel(this.model = new DefaultListModel());
		setCellRenderer(new AgentRenderer());
		setBackground(null);
		setOpaque(false);

		setToolTipText("The list of discovered agents available for performing test on OLAS.");

		this.scenarioChooser = scenarioChooser;
		ConsoleData.getInstance().getAgentDiscoverer().addAgentStateListener(
				this);
	}

	/**
	 * @return <code>true</code> if the given agent is selected in this
	 *         {@link AgentsList} component.
	 */
	public boolean isSelected(ConsoleAgent agent) {

		for (Object o : getSelectedValues())
			if (o.equals(agent))
				return true;

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void membersChanged(List<Address> addresses) {

		// Check whether all agents are in the list and add them if need be.
		for (Address address : addresses)
			if (!address.equals(ConsoleData.getInstance().getSelf())) {
				ConsoleAgent agent = ConsoleData.getInstance()
						.getAgent(address);
				if (agent != null && !this.model.contains(agent)) {
					this.model.addElement(agent);
					agent.addAgentStatusListener(this);

					int endIndex = getModel().getSize() - 1;
					addSelectionInterval(endIndex, endIndex);
				}
			}

		// Remove stale agents from the list.
		for (ConsoleAgent agent : ConsoleData.getInstance().removeStaleAgents())
			this.model.removeElement(agent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentSuspected(Address agentAddress) {

		ConsoleAgent agent = ConsoleData.getInstance().getAgent(agentAddress);
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

		membersChanged(ConsoleData.getInstance().getAgentDiscoverer()
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
	 * @{inheritDoc}
	 */
	public void valueChanged(ListSelectionEvent e) {

		for (int i = e.getFirstIndex(); i <= e.getLastIndex(); ++i) {
			if (i >= this.model.getSize())
				break;

			Object value = this.model.getElementAt(i);
			if (value instanceof ConsoleAgent) {
				ConsoleAgent agent = (ConsoleAgent) value;
				agent.setSelected(isSelected(agent));
			}
		}

		updateButtons();
	}

	/**
	 * @{inheritDoc}
	 */
	public void statusChanged(ConsoleAgent agent) {

		repaint();

		if (isSelected(agent))
			updateButtons();
	}

	private void updateButtons() {

		boolean enabled = true;
		AgentState state = null;
		List<AgentState> transit = new ArrayList<AgentState>();

		for (Object o : getSelectedValues())
			if (o instanceof ConsoleAgent) {
				ConsoleAgent agent = (ConsoleAgent) o;

				if (state == null)
					state = agent.getState();
				if (agent.getTransit() != null
						&& !agent.getTransit().equals(AgentState.RESET))
					transit.add(agent.getTransit());

				enabled &= agent.getTransit() == null;
				enabled &= state.equals(agent.getState());
			}

		this.scenarioChooser.enableButtonsFor(enabled ? state : null, transit
				.toArray(new AgentState[transit.size()]));
	}
}
