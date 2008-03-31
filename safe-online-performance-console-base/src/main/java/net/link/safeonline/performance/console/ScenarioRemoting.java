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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanException;
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
 * <h2>{@link ScenarioRemoting}<br>
 * <sub>This class takes care of communication with the remote agent via RMI.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ScenarioRemoting {

	private static Log LOG = LogFactory.getLog(ScenarioRemoting.class);

	private static Map<Thread, MBeanServerConnection> connections = Collections
			.synchronizedMap(new HashMap<Thread, MBeanServerConnection>());

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

	public static void shutdown(Thread thread) {

		// TODO
	}

	/**
	 * Invoke a method on the agent service deployed at AP of the given agent.
	 * 
	 * @throws IllegalStateException
	 *             When the RMI adaptor is not available on the given agent.
	 */
	private Object invokeFor(Address agent, String methodName,
			Object[] arguments, String[] argumentTypes) throws MBeanException,
			IllegalStateException {

		try {
			InitialContext context = getInitialContext(agent);
			MBeanServerConnection rmi = (MBeanServerConnection) context
					.lookup("jmx/rmi/RMIAdaptor");

			connections.put(Thread.currentThread(), rmi);
			return rmi.invoke(this.agentService, methodName, arguments,
					argumentTypes);
		}

		catch (IOException e) {
			LOG.error("Could not talk to the MBean Server.", e);
			throw new RuntimeException(e);
		}

		catch (MBeanException e) {
			throw e;
		}

		catch (JMException e) {
			LOG.error("Failed invoking " + methodName + ".", e);
			throw new RuntimeException(e);
		}

		catch (NamingException e) {
			throw new IllegalStateException("RMI Adaptor not found on " + agent
					+ ".", e);
		}

		finally {
			connections.remove(Thread.currentThread());
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

		try {
			invokeFor(agent, "upload", new Object[] { applicationData },
					new String[] { "[B" });
		} catch (MBeanException e) {
			LOG.error("Server error during upload!", e);
		}
	}

	/**
	 * Deploy the previously uploaded application.
	 */
	public void deploy(Address agent) {

		try {
			invokeFor(agent, "deploy", new Object[] {}, new String[] {});
		} catch (MBeanException e) {
			LOG.error("Server error during deploy!", e);
		}
	}

	/**
	 * Deploy the previously uploaded application.
	 */
	public void execute(Address agent, String scenarioName, Integer agents,
			Integer workers, Long duration, String olasHost, Boolean useSsl,
			Date startTime) {

		try {
			invokeFor(agent, "execute", new Object[] { scenarioName, agents,
					workers, duration, olasHost, useSsl, startTime },
					new String[] { String.class.getName(),
							Integer.class.getName(), Integer.class.getName(),
							Long.class.getName(), String.class.getName(),
							Boolean.class.getName(), Date.class.getName() });
		} catch (MBeanException e) {
			LOG.error("Server error during execute!", e);
		}
	}

	/**
	 * @see Agent#getStats()
	 */
	public ScenarioExecution getCharts(Address agent, Date startTime) {

		try {
			return (ScenarioExecution) invokeFor(agent, "getCharts",
					new Object[] { startTime }, new String[] { Date.class
							.getName() });
		} catch (MBeanException e) {
			LOG.error("Server error during stats retrieval!", e);

			return null;
		}
	}

	/**
	 * @see Agent#getState()
	 */
	public AgentState getState(Address agent) {

		try {
			return (AgentState) invokeFor(agent, "getState", new Object[] {},
					new String[] {});
		} catch (MBeanException e) {
			LOG.error("Server error during state retrieval!", e);
		}

		return null;
	}

	/**
	 * @see Agent#getTransit()
	 */
	public AgentState getTransit(Address agent) {

		try {
			return (AgentState) invokeFor(agent, "getTransit", new Object[] {},
					new String[] {});
		} catch (MBeanException e) {
			LOG.error("Server error during transit retrieval!", e);
		}

		return null;
	}

	/**
	 * @see Agent#getError()
	 */
	public Throwable getError(Address agent) {

		try {
			return (Throwable) invokeFor(agent, "getError", new Object[] {},
					new String[] {});
		} catch (MBeanException e) {
			return e;
		}
	}

	/**
	 * @see Agent#getExecutions()
	 */
	@SuppressWarnings("unchecked")
	public Set<ScenarioExecution> getExecutions(Address agent)
			throws NamingException {

		try {
			return (Set<ScenarioExecution>) invokeFor(agent, "getExecutions",
					new Object[] {}, new String[] {});
		} catch (MBeanException e) {
			if (e.getCause() instanceof NamingException)
				throw (NamingException) e.getCause();

			LOG.error("Server error during execution retrieval!", e);

			return new HashSet<ScenarioExecution>();
		}
	}

	/**
	 * @see Agent#getScenarios()
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getScenarios(Address agent) {

		try {
			return (Set<String>) invokeFor(agent, "getScenarios",
					new Object[] {}, new String[] {});
		} catch (MBeanException e) {
			LOG.error("Server error during scenario retrieval!", e);

			return null;
		}
	}

	/**
	 * @see Agent#resetTransit()
	 */
	public void resetTransit(Address agent) {

		try {
			invokeFor(agent, "resetTransit", new Object[] {}, new String[] {});
		} catch (MBeanException e) {
			LOG.error("Server error during reset!", e);
		}
	}
}
