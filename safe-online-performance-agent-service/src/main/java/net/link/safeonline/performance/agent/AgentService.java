/**
 * 
 */
package net.link.safeonline.performance.agent;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.JMException;
import javax.naming.NamingException;

/**
 * @author mbillemo
 * 
 */
public class AgentService implements AgentServiceMBean {

	private AgentBroadcaster broadcaster;
	private ScenarioDeployer deployer;

	public AgentService() {

		this.broadcaster = new AgentBroadcaster();
		this.deployer = new ScenarioDeployer();
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {

		this.broadcaster.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {

		this.broadcaster.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isStarted() {

		return this.broadcaster.isConnected();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGroup() {

		return this.broadcaster.getGroup();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setGroup(String group) {

		this.broadcaster.setGroup(group);
	}

	/**
	 * {@inheritDoc}
	 */
	public void upload(byte[] application) throws IOException {

		this.deployer.upload(application);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deploy() throws JMException, NamingException,
			MalformedURLException, IOException {

		this.deployer.deploy();
	}
}