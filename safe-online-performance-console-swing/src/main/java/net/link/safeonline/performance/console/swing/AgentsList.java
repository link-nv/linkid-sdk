/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import net.link.safeonline.performance.console.jgroups.AgentDiscoverer;
import net.link.safeonline.performance.console.jgroups.AgentStateListener;

import org.jgroups.Address;

/**
 * A list that visualises agent status.
 * 
 * @author mbillemo
 */
public class AgentsList extends JList implements AgentStateListener {

	private static final long serialVersionUID = 1L;

	private Map<Address, Agent> agents;

	private DefaultListModel model;

	public AgentsList(AgentDiscoverer agentDiscoverer) {

		setModel(this.model = new DefaultListModel());
		setCellRenderer(new AgentRenderer());
		setBackground(null);
		setOpaque(false);

		this.agents = new HashMap<Address, Agent>();
		agentDiscoverer.addAgentStateListener(this);

		membersChanged(agentDiscoverer.getMembers());
	}

	/**
	 * @return
	 */
	public Map<Address, Agent> getAgents() {

		return this.agents;
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

		for (Address address : addresses)
			if (!this.agents.containsKey(address)) {
				Agent agent = new Agent(address);
				this.agents.put(address, agent);
				this.model.addElement(agent);
			}

		for (Address address : this.agents.keySet())
			if (!addresses.contains(address))
				this.model.removeElement(this.agents.remove(address));
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentSuspected(Address agent) {

		this.agents.get(agent).setHealthy(false);
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
}
