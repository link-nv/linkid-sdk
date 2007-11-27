/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.jgroups;

import java.util.Vector;

import org.jgroups.Address;

/**
 * @author mbillemo
 * 
 */
public interface AgentStateListener {

	/**
	 * The provided agent is suspected of being unreachable.
	 */
	public void agentSuspected(Address agent);

	/**
	 * The members of the channel changed to the provided list.
	 */
	public void membersChanged(Vector<Address> newMembers);

	/**
	 * The channel was closed.
	 */
	public void channelClosed();

	/**
	 * We connected to the channel.
	 */
	public void channelConnected();

	/**
	 * We disconnected from the channel.
	 */
	public void channelDisconnected();

	/**
	 * An agent reconnected to the channel.
	 */
	public void channelReconnected(Address agent);

	/**
	 * We have been shunned from the channel.
	 */
	public void channelShunned();
}
