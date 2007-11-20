/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;

/**
 * @author mbillemo
 * 
 */
public class AgentBroadcaster {

	private static final Log LOG = LogFactory.getLog(AgentBroadcaster.class);

	private JChannel channel;
	private String group;

	/**
	 * Connect to the JGroups channel and join the performance agents group.
	 */
	public void start() {

		try {
			this.channel = new JChannel(getClass().getResource("/jgroups.xml"));
			this.channel.connect(getGroup());
		}

		catch (ChannelException e) {
			LOG.error("Couldn't establish the JGroups channel.", e);
		}
	}

	/**
	 * Disconnect from the JGroups group and close the channel.
	 */
	public void stop() {

		this.channel.close();
	}

	/**
	 * @return the JGroups group to join.
	 */
	public String getGroup() {

		return this.group;
	}

	/**
	 * @param group
	 *            the JGroups group to join.
	 */
	public void setGroup(String group) {

		this.group = group;
	}

	/**
	 * @return true if we are connected to the JGroups group.
	 */
	public boolean isConnected() {

		return this.channel.isConnected();
	}
}
