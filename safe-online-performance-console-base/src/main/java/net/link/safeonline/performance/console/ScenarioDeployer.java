package net.link.safeonline.performance.console;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;

/**
 * Utility class that interfaces with JMX on the Agent AS to upload an EAR and
 * deploy it.
 * 
 * @author mbillemo
 */
public class ScenarioDeployer {

	private static Log LOG = LogFactory.getLog(ScenarioDeployer.class);

	private ObjectName agentService;

	public ScenarioDeployer() {

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

		String jndiUrl = agent.toString().split(":")[0] + ":1099";
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, jndiUrl);

		return new InitialContext(environment);
	}

	/**
	 * Retrieve the {@link MBeanServerConnection} to the AP of the given agent.
	 */
	private MBeanServerConnection getMBeanServerFor(Address agent) {

		try {
			InitialContext context = getInitialContext(agent);
			return (MBeanServerConnection) context
					.lookup("jmx/invoker/RMIAdaptor");
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

		try {
			getMBeanServerFor(agent).invoke(this.agentService, "upload",
					new Object[] { applicationData }, new String[] { "[B" });
		}

		catch (IOException e) {
			LOG.error("Could not reach the MBean Server.", e);
			throw new RuntimeException(e);
		}

		catch (JMException e) {
			LOG.error("Could not deploy the Scenario.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Deploy the previously uploaded application.
	 */
	public void deploy(Address agent) {

		try {
			getMBeanServerFor(agent).invoke(this.agentService, "deploy",
					new Object[] {}, new String[] {});
		}

		catch (IOException e) {
			LOG.error("Could not reach the MBean Server.", e);
			throw new RuntimeException(e);
		}

		catch (JMException e) {
			LOG.error("Could not deploy the Scenario.", e);
			throw new RuntimeException(e);
		}
	}
}
