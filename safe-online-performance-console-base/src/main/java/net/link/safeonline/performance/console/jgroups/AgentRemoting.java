/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.jgroups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

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
public class AgentRemoting implements Receiver, ChannelListener {

	static final Log LOG = LogFactory.getLog(AgentRemoting.class);

	private List<AgentStateListener> agentStateListeners;

	JChannel channel;
	String group;

	/**
	 * Join the Profiler's JGroup using the package name as group name.
	 */
	public AgentRemoting() {

		ResourceBundle properties = ResourceBundle.getBundle("console");
		this.group = properties.getString("jgroups.group");
		LOG.debug("jgroups.group: " + this.group);

		this.agentStateListeners = new ArrayList<AgentStateListener>();

		try {
			if (null == this.channel || !this.channel.isOpen())
				this.channel = new JChannel(getClass().getResource(
						"/jgroups.xml"));
		}

		catch (ChannelException e) {
			String msg = "Couldn't establish the JGroups channel.";
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}

		this.channel.addChannelListener(AgentRemoting.this);
		this.channel.setReceiver(AgentRemoting.this);

		Runtime.getRuntime().addShutdownHook(
				new Thread("JGroups ShutdownHook") {

					@Override
					public void run() {

						close();
					}
				});

		Thread thread = new Thread() {
			@Override
			public void run() {

				try {
					if (!AgentRemoting.this.channel.isConnected())
						AgentRemoting.this.channel
								.connect(AgentRemoting.this.group);
				}

				catch (ChannelException e) {
					String msg = "Couldn't establish the JGroups channel.";
					LOG.error(msg, e);
					throw new RuntimeException(msg, e);
				}
			}
		};

		thread.setDaemon(true);
		thread.start();
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
	 * @return The members in the current view.
	 */
	public List<Address> getMembers() {

		if (this.channel.getView() == null)
			return null;

		return Collections
				.unmodifiableList(this.channel.getView().getMembers());
	}

	/**
	 * @return <code>true</code> if the current view contains the given
	 *         member.
	 */
	public boolean hasMember(Address member) {

		if (this.channel.getView() == null)
			return false;

		return this.channel.getView().containsMember(member);
	}

	/**
	 * @return the name of the JGroups group of agents.
	 */
	public String getGroupName() {

		if (this.channel.getClusterName() == null)
			return "[" + this.group + "]";

		return this.channel.getClusterName();
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
	 * Check whether the {@link AgentRemoting} is still connected to the group.
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
