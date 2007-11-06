/**
 * 
 */
package net.link.safeonline.performance.console.swing.data;

import net.link.safeonline.performance.console.swing.ui.AgentStatusListener;

import org.jgroups.Address;

/**
 * This object keeps the status of communication between the console and the
 * agent it represents. It also features locking such that only one operation
 * would be executed upon it at once.
 * 
 * @author mbillemo
 * 
 */
public class Agent {

	private static final long serialVersionUID = 1L;

	private Address agentAddress;
	private Exception error;
	private boolean healthy;
	private boolean uploading;
	private boolean deploying;
	private boolean executing;
	private boolean selected;

	private AgentStatusListener agentStatusListener;

	/**
	 * Create a new {@link Agent} component based off the agent at the given
	 * {@link Address}.
	 */
	public Agent(Address agentAddress) {

		this.agentAddress = agentAddress;
		setHealthy(true);
	}

	/**
	 * @param error
	 *            An error that occurred while interacting with this client.
	 */
	public void setError(Exception error) {

		this.error = error;
	}

	/**
	 * @return An error that occurred while interacting with this client.
	 */
	public Exception getError() {

		return this.error;
	}

	/**
	 * @return false if JGroups suspects this agent of being unavailable.
	 */
	public boolean isHealthy() {

		return this.healthy;
	}

	/**
	 * Set the JGroups health status of this agent.
	 */
	public void setHealthy(boolean healthy) {

		this.healthy = healthy;

		if (null != this.agentStatusListener)
			this.agentStatusListener.statusChanged(this);
	}

	private boolean isLocked() {

		return isUploading() || isDeploying() || isExecuting();
	}

	/**
	 * @param uploading
	 *            Set this to true to make this agent indicate that a file is
	 *            being uploaded to it.
	 * 
	 * @return <code>true</code> if agent is available for this action.
	 */
	public boolean setUploading(boolean uploading) {

		if (uploading && isLocked())
			return false;

		this.uploading = uploading;

		if (null != this.agentStatusListener)
			this.agentStatusListener.statusChanged(this);

		return true;
	}

	/**
	 * @return <code>true</code> if a file is being uploaded to this agent.
	 */
	public boolean isUploading() {

		return this.uploading;
	}

	/**
	 * @param deploying
	 *            Set this to <code>true</code> to make this agent indicate
	 *            that an application is being deployed on it.
	 * 
	 * @return <code>true</code> if agent is available for this action.
	 */
	public boolean setDeploying(boolean deploying) {

		if (deploying && isLocked())
			return false;

		this.deploying = deploying;

		if (null != this.agentStatusListener)
			this.agentStatusListener.statusChanged(this);

		return true;
	}

	/**
	 * @return <code>true</code> if this agent is deploying an application.
	 */
	public boolean isDeploying() {

		return this.deploying;
	}

	/**
	 * @param executing
	 *            <code>true</code> to make this agent indicate that it is
	 *            executing a scenario.
	 * 
	 * @return <code>true</code> if agent is available for this action.
	 */
	public boolean setExecuting(boolean executing) {

		if (executing && isLocked())
			return false;

		this.executing = executing;

		if (null != this.agentStatusListener)
			this.agentStatusListener.statusChanged(this);

		return true;
	}

	/**
	 * @return <code>true</code> to make this agent indicate that it is
	 *         executing a scenario.
	 */
	public boolean isExecuting() {

		return this.executing;
	}

	/**
	 * @return <code>true</code> if this agent is to participate in actions.
	 */
	public boolean isSelected() {

		return this.selected;
	}

	/**
	 * @param selected
	 *            <code>true</code> if this agent is selected for actions.
	 */
	public void setSelected(boolean selected) {

		this.selected = selected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		String action = "Idle";
		if (this.executing)
			action = "Executing";
		else if (this.deploying)
			action = "Deploying";
		else if (this.uploading)
			action = "Receiving";

		String health = "Unavailable";
		if (this.healthy)
			health = "Healthy";

		return String.format("[%s:%s] %s", health, action, this.agentAddress);
	}

	/**
	 * Define the object that should be notified when this agent changes. This
	 * should be an object that can fire the appropriate events in the UI
	 * required to render the change in this {@link Agent}'s status.
	 */
	public void setAgentStatusListener(AgentStatusListener agentStatusListener) {

		this.agentStatusListener = agentStatusListener;
	}
}
