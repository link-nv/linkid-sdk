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
 * <h2>{@link ScenarioDeployer}<br>
 * <sub>Deploys an application (EAR) that is contained in a byte array.</sub></h2>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ScenarioDeployer {

    private static final Log LOG = LogFactory.getLog(ScenarioDeployer.class);

    private File             applicationFile;

    private boolean          uploading;
    private boolean          deploying;


    public void upload(byte[] application) throws IOException {

        try {
            this.uploading = true;

            // Undeploy any existing scenario first.
            if (null != this.applicationFile && this.applicationFile.exists()) {
                try {
                    undeploy();
                } catch (Exception e) {
                    LOG.error("Couldn't undeploy existing scenario: " + this.applicationFile, e);
                }
            }

            // Create a temporary file to write the scenario into.
            this.applicationFile = File.createTempFile("scenario", ".ear");
            this.applicationFile.deleteOnExit();

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.applicationFile));
            out.write(application);
            out.close();
        }

        finally {
            this.uploading = false;
        }
    }

    public void deploy() throws JMException, MalformedURLException, IOException {

        invokeDeployer("deploy", new URL[] { this.applicationFile.toURI().toURL() },
                new String[] { URL.class.getName() });
    }

    public void undeploy() throws JMException, MalformedURLException, IOException {

        try {
            invokeDeployer("undeploy", new URL[] { this.applicationFile.toURI().toURL() }, new String[] { URL.class
                    .getName() });
        } finally {
            if (null != this.applicationFile && this.applicationFile.exists()) {
                this.applicationFile.delete();
            }
        }
    }

    public boolean isUploaded() {

        return this.applicationFile != null && this.applicationFile.canRead();
    }

    public boolean isUploading() {

        return this.uploading;
    }

    public boolean isDeploying() {

        return this.deploying;
    }

    private Object invokeDeployer(String methodName, Object[] parameters, String[] signature) throws JMException {

        try {
            this.deploying = true;

            MBeanServer applicationServer = MBeanServerLocator.locateJBoss();
            ObjectName mainDeployer = new ObjectName("jboss.system:service=MainDeployer");
            return applicationServer.invoke(mainDeployer, methodName, parameters, signature);
        }

        finally {
            this.deploying = false;
        }
    }
}
