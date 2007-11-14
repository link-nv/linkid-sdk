/**
 * 
 */
package net.link.safeonline.performance.console.swing.ui;

import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.link.safeonline.performance.console.jgroups.AgentStateListener;
import net.link.safeonline.performance.console.swing.data.Agent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;

import org.jgroups.Address;

/**
 * A list that visualises agent status.
 * 
 * @author mbillemo
 */
public class AgentsList extends JList implements AgentStateListener,
		ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private DefaultListModel model;

	private ConsoleData consoleData;

	public AgentsList(ConsoleData consoleData) {

		addListSelectionListener(this);
		setModel(this.model = new DefaultListModel());
		setCellRenderer(new AgentRenderer());
		setBackground(null);
		setOpaque(false);

		this.consoleData = consoleData;
		consoleData.getAgentDiscoverer().addAgentStateListener(this);
		membersChanged(consoleData.getAgentDiscoverer().getMembers());
	}

	/**
	 * @return <code>true</code> if the given agent is selected in this
	 *         {@link AgentsList} component.
	 */
	public boolean isSelected(Agent agent) {

		for (Object o : getSelectedValues())
			if (o.equals(agent))
				return true;

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void membersChanged(Vector<Address> addresses) {

		// Check whether all agents are in the list and add them if need be.
		for (Address address : addresses)
			if (!address.equals(this.consoleData.getSelf())) {
				Agent agent = this.consoleData.getAgent(address);
				if (!this.model.contains(agent))
					this.model.addElement(agent);
				setSelectedValue(agent, false);
			}

		// Remove stale agents from the list.
		for (Agent agent : this.consoleData.removeStaleAgents())
			this.model.removeElement(agent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentSuspected(Address agentAddress) {

		this.consoleData.getAgent(agentAddress).setHealthy(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelClosed() {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public void channelConnected() {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public void channelDisconnected() {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public void channelReconnected(Address arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public void channelShunned() {
		// TODO Auto-generated method stub

	}

	/**
	 * @{inheritDoc}
	 */
	public void valueChanged(ListSelectionEvent e) {

		for (int i = e.getFirstIndex(); i <= e.getLastIndex(); ++i) {
			if (i >= this.model.getSize())
				break;

			Object value = this.model.getElementAt(i);
			if (value instanceof Agent) {
				Agent agent = (Agent) value;
				agent.setSelected(isSelected(agent));
			}
		}
	}
}