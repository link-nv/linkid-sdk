/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.performance.console.jgroups.Agent;
import net.link.safeonline.performance.console.jgroups.AgentState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;

/**
 * Utility class that interfaces with JMX on the Agent AS to upload an EAR and
 * deploy it.
 * 
 * @author mbillemo
 */
public class ScenarioRemoting {

	private static Log LOG = LogFactory.getLog(ScenarioRemoting.class);

	private ObjectName agentService;

	public ScenarioRemoting() {

		try {
			this.agentService = new ObjectName(
					"safeonline.performance:name=AgentService");
		}

		catch (MalformedObjectNameException e) {
			LOG.error("The Agent Service is not available.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieve an {@link InitialContext} for the JNDI of the given agent.
	 */
	public static InitialContext getInitialContext(Address agent)
			throws NamingException {

		Hashtable<String, String> environment = new Hashtable<String, String>();

		String jndiUrl = String.format("jnp://%s:1099", agent.toString().split(
				":")[0]);
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, jndiUrl);

		return new InitialContext(environment);
	}

	/**
	 * Invoke a method on the agent service deployed at AP of the given agent.
	 */
	private Object invokeFor(Address agent, String methodName,
			Object[] arguments, String[] argumentTypes) {

		try {
			InitialContext context = getInitialContext(agent);
			return ((MBeanServerConnection) context
					.lookup("jmx/rmi/RMIAdaptor")).invoke(this.agentService,
					methodName, arguments, argumentTypes);
		}

		catch (IOException e) {
			LOG.error("Could not reach the MBean Server.", e);
			throw new RuntimeException(e);
		}

		catch (JMException e) {
			LOG.error("Failed invoking " + methodName + ".", e);
			throw new RuntimeException(e);
		}

		catch (NamingException e) {
			LOG.error("JMX's RMI Adaptor could not be found.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Upload the application that resides in the given file as EAR.<br>
	 * <br>
	 * Note: Maximum application size is: 2147483647 Bytes (2GB).
	 */
	public void upload(Address agent, File application) {

		byte[] applicationData = new byte[(int) application.length()];

		try {
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(application));
			in.read(applicationData);
			in.close();
		}

		catch (IOException e) {
			LOG.error("Could not read in the Scenario's data.", e);
			throw new RuntimeException(e);
		}

		invokeFor(agent, "upload", new Object[] { applicationData },
				new String[] { "[B" });
	}

	/**
	 * Deploy the previously uploaded application.
	 */
	public void deploy(Address agent) {

		invokeFor(agent, "deploy", new Object[] {}, new String[] {});
	}

	/**
	 * Deploy the previously uploaded application.
	 */
	public void execute(Address agent, String olasHost, Integer workers,
			Long duration) {

		invokeFor(agent, "execute",
				new Object[] { olasHost, workers, duration }, new String[] {
						String.class.getName(), Integer.class.getName(),
						Long.class.getName() });
	}

	/**
	 * @see Agent#actionCompleted(boolean)
	 */
	public void actionCompleted(Address agent, Boolean success) {

		invokeFor(agent, "actionCompleted", new Object[] { success },
				new String[] { Boolean.class.getName() });
	}

	/**
	 * @see Agent#actionRequest(AgentState)
	 */
	public boolean actionRequest(Address agent, AgentState action) {

		return (Boolean) invokeFor(agent, "actionRequest",
				new Object[] { action }, new String[] { AgentState.class
						.getName(), });
	}

	/**
	 * @see Agent#describeState()
	 */
	public String describeState(Address agent) {

		return (String) invokeFor(agent, "describeState", new Object[] {},
				new String[] {});
	}

	/**
	 * @see Agent#getAction()
	 */
	public AgentState getAction(Address agent) {

		return (AgentState) invokeFor(agent, "getAction", new Object[] {},
				new String[] {});
	}

	/**
	 * @see Agent#getCharts()
	 */
	@SuppressWarnings("unchecked")
	public List<byte[]> getCharts(Address agent) {

		return (List<byte[]>) invokeFor(agent, "getCharts", new Object[] {},
				new String[] {});
	}

	/**
	 * @see Agent#getState()
	 */
	public AgentState getState(Address agent) {

		return (AgentState) invokeFor(agent, "getState", new Object[] {},
				new String[] {});
	}

	/**
	 * @see Agent#getTransit()
	 */
	public AgentState getTransit(Address agent) {

		return (AgentState) invokeFor(agent, "getTransit", new Object[] {},
				new String[] {});
	}

	/**
	 * @see Agent#setCharts(List)
	 */
	public void setCharts(Address agent, List<byte[]> charts) {

		invokeFor(agent, "setCharts", new Object[] { charts },
				new String[] { List.class.getName() });
	}
}
