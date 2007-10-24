/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import net.link.safeonline.performance.console.jgroups.AgentDiscoverer;
import net.link.safeonline.performance.console.jgroups.AgentStateListener;
import net.link.safeonline.performance.console.swing.Agent.State;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;

/**
 * A panel that visualises agent status.
 * 
 * @author mbillemo
 */
public class AgentsGrid extends JPanel implements AgentStateListener {

	private static final Log LOG = LogFactory.getLog(AgentsGrid.class);

	private static final long serialVersionUID = 1L;

	private AgentDiscoverer agentDiscoverer;

	private Map<Address, Agent> agents;

	public AgentsGrid(AgentDiscoverer agentDiscoverer) {

		super(new GridLayout(1, 0));

		this.agents = new HashMap<Address, Agent>();
		this.agentDiscoverer = agentDiscoverer;
		agentDiscoverer.addAgentStateListener(this);

		updateMembers();
	}

	/**
	 * Remove all known agents from the view.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void removeAll() {

		this.agents.clear();
		super.removeAll();
	}

	private void add(Address agent) {

		LOG.info("Adding agent: " + agent);

		Agent agentComponent = new Agent(agent);
		this.agents.put(agent, agentComponent);
		add(agentComponent);

		update(agent);
	}

	private void update(Address agent) {

		LOG.info("Updating agent: " + agent);
		// TODO
	}

	private void remove(Address agent) {

		LOG.info("Removing agent: " + agent);
		remove(this.agents.remove(agent));
	}

	private void updateMembers() {

		if (null != this.agentDiscoverer && this.agentDiscoverer.isConnected())
			membersChanged(this.agentDiscoverer.getMembers());
	}

	/**
	 * {@inheritDoc}
	 */
	public void membersChanged(List<Address> members) {

		for (Address agent : members) {
			if (!this.agents.containsKey(agent))
				add(agent);

			update(agent);
		}

		for (Address agent : this.agents.keySet())
			if (!members.contains(agent))
				remove(agent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void agentSuspected(Address agent) {

		if (!this.agents.containsKey(agent))
			updateMembers();

		this.agents.get(agent).setState(State.UNRESPONSIVE);
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
