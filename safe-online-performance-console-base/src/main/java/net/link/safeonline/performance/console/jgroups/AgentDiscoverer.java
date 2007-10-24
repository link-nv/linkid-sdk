/**
 * 
 */
package net.link.safeonline.performance.console.jgroups;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

/**
 * Utility class to locate performance testing agents using JGroups and monitor
 * any changes in their availability.
 * 
 * @author mbillemo
 */
public class AgentDiscoverer implements Receiver, ChannelListener {

	private static final Log LOG = LogFactory.getLog(AgentDiscoverer.class);

	private static final String PROFILER_JGROUPS_GROUP = "net.lin-k.safe-online.performance";

	private JChannel channel;

	private List<AgentStateListener> agentStateListeners;

	/**
	 * Join the Profiler's JGroup using the package name as group name.
	 */
	public AgentDiscoverer() {

		try {
			this.agentStateListeners = new ArrayList<AgentStateListener>();

			if (null == this.channel || !this.channel.isOpen())
				this.channel = new JChannel();

			if (!this.channel.isConnected())
				this.channel.connect(PROFILER_JGROUPS_GROUP);
		}

		catch (ChannelException e) {
			LOG.error("Couldn't establish the JGroups channel.", e);
		}
	}

	/**
	 * Add an object that'll listen agent and channel events.
	 */
	public void addAgentStateListener(AgentStateListener listener) {

		this.agentStateListeners.add(listener);
	}

	public void removeAgentStateListener(AgentStateListener listener) {

		this.agentStateListeners.remove(listener);
	}

	/**
	 * Retrieve the members currently part of the group.
	 */
	public List<Address> getMembers() {

		return this.channel.getView().getMembers();
	}

	/**
	 * Check whether the {@link AgentDiscoverer} is still connected to the
	 * group.
	 */
	public boolean isConnected() {

		return this.channel.isConnected();
	}

	/**
	 * {@inheritDoc}
	 */
	public void block() {

		this.channel.blockOk();
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspect(Address suspected_mbr) {

		for (AgentStateListener listener : this.agentStateListeners)
			listener.agentSuspected(suspected_mbr);
	}

	/**
	 * {@inheritDoc}
	 */
	public void viewAccepted(View new_view) {

		for (AgentStateListener listener : this.agentStateListeners)
			listener.membersChanged(new_view.getMembers());
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getState() {

		// We dont't care about state.
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void receive(Message m) {

		// We dont't care about messages.
	}

	/**
	 * {@inheritDoc}
	 */
	public void setState(byte[] s) {

		// We dont't care about state.
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelClosed(Channel c) {

		if (c.equals(this.channel))
			for (AgentStateListener listener : this.agentStateListeners)
				listener.channelClosed();
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelConnected(Channel c) {

		if (c.equals(this.channel))
			for (AgentStateListener listener : this.agentStateListeners)
				listener.channelConnected();
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelDisconnected(Channel c) {

		if (c.equals(this.channel))
			for (AgentStateListener listener : this.agentStateListeners)
				listener.channelDisconnected();
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelReconnected(Address agent) {

		for (AgentStateListener listener : this.agentStateListeners)
			listener.channelReconnected(agent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void channelShunned() {

		for (AgentStateListener listener : this.agentStateListeners)
			listener.channelShunned();
	}
}
