/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import javax.management.JMException;
import javax.naming.NamingException;

import net.link.safeonline.performance.console.jgroups.Agent;

/**
 * <h2>{@link AgentServiceMBean}<br>
 * <sub>The MBean interface for {@link AgentService}.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public interface AgentServiceMBean extends Agent {

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
	 * Execute the scenario and collect the charts.
	 */
	public void execute(String scenarioName, Integer agents, Integer workers,
			Long duration, String hostname, Date startTime);

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
