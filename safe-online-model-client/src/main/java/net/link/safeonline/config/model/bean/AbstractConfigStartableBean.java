/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.config.model.bean;

import javax.ejb.EJB;
import javax.ejb.EJBException;

import net.link.safeonline.Startable;
import net.link.safeonline.config.model.ConfigurationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link AbstractConfigStartableBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 28, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public abstract class AbstractConfigStartableBean implements Startable {

    protected final Log          LOG = LogFactory.getLog(getClass());

    @EJB(mappedName = ConfigurationManager.JNDI_BINDING)
    private ConfigurationManager configurationManager;

    protected Class<?>[]         configurationBeans;


    public AbstractConfigStartableBean() {

        // empty
    }

    public abstract int getPriority();

    public void postStart() {

        if (null != configurationBeans) {
            for (Class<?> configurationBean : configurationBeans) {
                try {
                    Object target = configurationBean.newInstance();
                    configurationManager.configure(target);
                } catch (Exception e) {
                    throw new EJBException("Failed to execute @Configurable", e);
                }
            }
        }

        /*
         * ConfigurationDeploymentStrategy configurationDeploymentStrategy = new ConfigurationDeploymentStrategy();
         * configurationDeploymentStrategy.scan();
         * 
         * for (Class<?> classObject : configurationDeploymentStrategy.getScannedConfigurationClasses()) {
         * this.LOG.debug("found configurable class: " + classObject.getName()); configure(classObject); }
         */
    }

    public void preStop() {

        // empty
    }
}
