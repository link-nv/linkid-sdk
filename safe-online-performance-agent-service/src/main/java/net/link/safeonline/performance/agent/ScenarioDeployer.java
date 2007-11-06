/**
 * 
 */
package net.link.safeonline.performance.agent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Deploys an application that is contained in a byte array.
 * 
 * @author mbillemo
 */
public class ScenarioDeployer {

	private static final Log LOG = LogFactory.getLog(ScenarioDeployer.class);

	private File applicationFile;

	public void upload(byte[] application) throws IOException {

		// Undeploy any existing scenario first.
		if (null != this.applicationFile && this.applicationFile.exists())
			try {
				undeploy();
			} catch (Exception e) {
				LOG.error("Couldn't undeploy existing scenario: "
						+ this.applicationFile, e);
			}

		// Create a temporary file to write the scenario into.
		this.applicationFile = File.createTempFile("scenario", ".ear");
		this.applicationFile.deleteOnExit();

		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(this.applicationFile));
		out.write(application);
		out.close();
	}

	public void deploy() throws JMException, NamingException,
			MalformedURLException, IOException {

		invoke("deploy", new URL[] { this.applicationFile.toURI().toURL() },
				new String[] { URL.class.getName() });
	}

	public void undeploy() throws JMException, NamingException,
			MalformedURLException, IOException {

		try {
			invoke("undeploy",
					new URL[] { this.applicationFile.toURI().toURL() },
					new String[] { URL.class.getName() });
		}

		finally {
			if (null != this.applicationFile && this.applicationFile.exists())
				this.applicationFile.delete();
		}
	}

	private Object invoke(String methodName, Object[] parameters,
			String[] signature) throws JMException, NamingException,
			MalformedURLException, IOException {

		InitialContext context = getInitialContext();
		MBeanServerConnection applicationServer = (MBeanServerConnection) context
				.lookup("jmx/invoker/RMIAdaptor");

		ObjectName mainDeployer = new ObjectName(
				"jboss.system:service=MainDeployer");

		return applicationServer.invoke(mainDeployer, methodName, parameters,
				signature);
	}

	private static InitialContext getInitialContext() throws NamingException {

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "localhost:1099");

		return new InitialContext(environment);
	}
}
