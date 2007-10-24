/**
 * 
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

	private static final String PROFILER_JGROUPS_GROUP = "net.lin-k.safe-online.performance";

	private JChannel channel;

	/**
	 * Join the Profiler's JGroup using the package name as group name.
	 */
	public AgentBroadcaster() {

		try {
			if (null == this.channel || !this.channel.isOpen()) {
				LOG.debug("Opening a JGroups Channel.");
				this.channel = new JChannel();
			}

			if (!this.channel.isConnected()) {
				LOG.debug("Joining the Performance Agents group.");
				this.channel.connect(PROFILER_JGROUPS_GROUP);
			}
		}

		catch (ChannelException e) {
			LOG.error("Couldn't establish the JGroups channel.", e);
		}
	}

	/**
	 * Check whether this {@link AgentBroadcaster} is still connected to the
	 * rest of the group.
	 */
	private boolean isConnected() {

		return this.channel.isConnected();
	}

	public static void main(String[] args) {

		AgentBroadcaster broadcaster = new AgentBroadcaster();

		while (broadcaster.isConnected())
			try {
				Thread.sleep(100);
			}

			catch (InterruptedException e) {
				System.exit(0);
			}
	}
}