/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.jgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

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

	private List<AgentStateListener> agentStateListeners;

	private JChannel channel;

	/**
	 * Join the Profiler's JGroup using the package name as group name.
	 */
	public AgentDiscoverer() {

		ResourceBundle properties = ResourceBundle.getBundle("console");
		String group = properties.getString("jgroups.group");
		LOG.debug("jgroups.group: " + group);

		try {
			this.agentStateListeners = new ArrayList<AgentStateListener>();

			if (null == this.channel || !this.channel.isOpen())
				this.channel = new JChannel(getClass().getResource(
						"/jgroups.xml"));

			if (!this.channel.isConnected())
				this.channel.connect(group);

			Runtime.getRuntime().addShutdownHook(
					new Thread("JGroups ShutdownHook") {

						@Override
						public void run() {

							close();
						}
					});

			this.channel.addChannelListener(this);
			this.channel.setReceiver(this);
		}

		catch (ChannelException e) {
			String msg = "Couldn't establish the JGroups channel.";
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Add an object that'll listen agent and channel events.
	 */
	public void addAgentStateListener(AgentStateListener listener) {

		this.agentStateListeners.add(listener);
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

	public void close() {

		this.channel.close();
	}

	/**
	 * @return the name of the JGroups group of agents.
	 */
	public String getGroupName() {

		return this.channel.getClusterName();
	}

	/**
	 * Retrieve the members currently part of the group.
	 */
	public Vector<Address> getMembers() {

		return this.channel.getView().getMembers();
	}

	/**
	 * Retrieve the {@link Address} that this client has in the group.
	 */
	public Address getSelf() {

		return this.channel.getLocalAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getState() {

		// We dont't care about state.
		return null;
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
	public void receive(Message m) {

		// We dont't care about messages.
	}

	public void removeAgentStateListener(AgentStateListener listener) {

		this.agentStateListeners.remove(listener);
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
}
