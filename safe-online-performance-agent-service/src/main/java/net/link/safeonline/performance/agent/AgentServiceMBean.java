package net.link.safeonline.performance.agent;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.JMException;
import javax.naming.NamingException;

/**
 * The MBean interface for {@link AgentService}.
 * 
 * @author mbillemo
 */
public interface AgentServiceMBean {

	/**
	 * @return the JGroups group to join.
	 */
	public String getGroup();

	/**
	 * @param group
	 *            the JGroups group to join.
	 */
	public void setGroup(String group);

	/**
	 * Upload the given application so that it can be deployed.
	 */
	public void upload(byte[] application) throws IOException;

	/**
	 * Instruct this MBean to deploy the previously uploaded application.
	 */
	public void deploy() throws JMException, NamingException,
			MalformedURLException, IOException;

	/**
	 * Called by JBoss when this service has been fully deployed and
	 * initialised.
	 */
	public void start();

	/**
	 * Called by JBoss when cleaning up this service for undeployment.
	 */
	public void stop();

	/**
	 * @return true if the service is available.
	 */
	public boolean isStarted();
}
