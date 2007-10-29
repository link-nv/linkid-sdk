/**
 * 
 */
package net.link.safeonline.performance.console.swing;

import org.jgroups.Address;

/**
 * @author mbillemo
 * 
 */
public class Agent {

	private static final long serialVersionUID = 1L;

	private Address agentAddress;
	private boolean healthy;
	private boolean uploading;

	private boolean deploying;

	private Exception error;

	/**
	 * Create a new {@link Agent} component based off the agent at the given
	 * {@link Address}.
	 */
	public Agent(Address agentAddress) {

		this.agentAddress = agentAddress;
		setHealthy(true);
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
	 * @return <code>true</code> if a file is being uploaded to this agent.
	 */
	public boolean isUploading() {

		return this.uploading;
	}

	/**
	 * Set this to true to make this agent indicate that a file is being
	 * uploaded to it.
	 */
	public void setUploading(boolean uploading) {

		this.uploading = uploading;
	}

	/**
	 * @return <code>true</code> if this agent is deploying an application.
	 */
	public boolean isDeploying() {

		return this.deploying;
	}

	/**
	 * @Set this to <code>true</code> to make this agent indicate that an
	 *      application is being deployed on it.
	 */
	public void setDeploying(boolean deploying) {

		this.deploying = deploying;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		return String.format("[%s:%s] %s", this.healthy ? "Healthy"
				: "Unavailable", this.deploying ? "Deploying"
				: this.uploading ? "Receiving" : "Idle", this.agentAddress);
	}
}
