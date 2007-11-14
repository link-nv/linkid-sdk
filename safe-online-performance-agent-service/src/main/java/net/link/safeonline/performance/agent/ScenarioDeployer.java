/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.agent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Deploys an application that is contained in a byte array.
 * 
 * @author mbillemo
 */
public class ScenarioDeployer {

	private static final Log LOG = LogFactory.getLog(ScenarioDeployer.class);

	private File applicationFile;

	public void upload(byte[] application) throws IOException {
		LOG.debug("upload");
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

	public void deploy() throws JMException, MalformedURLException, IOException {
		LOG.debug("deploy");
		mainDeployerInvoke("deploy", new URL[] { this.applicationFile.toURI()
				.toURL() }, new String[] { URL.class.getName() });
	}

	public void undeploy() throws JMException, MalformedURLException,
			IOException {
		LOG.debug("undeploy");
		try {
			mainDeployerInvoke("undeploy", new URL[] { this.applicationFile
					.toURI().toURL() }, new String[] { URL.class.getName() });
		} finally {
			if (null != this.applicationFile && this.applicationFile.exists())
				this.applicationFile.delete();
		}
	}

	private Object mainDeployerInvoke(String methodName, Object[] parameters,
			String[] signature) throws JMException {
		MBeanServer applicationServer = MBeanServerLocator.locateJBoss();
		ObjectName mainDeployer = new ObjectName(
				"jboss.system:service=MainDeployer");
		return applicationServer.invoke(mainDeployer, methodName, parameters,
				signature);
	}
}