/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.log;

import net.link.safeonline.osgi.configuration.OlasConfigurationServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;


/**
 * <h2>{@link OlasLogServiceFactory}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 18, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class OlasLogServiceFactory implements ServiceFactory {

    private static final Log LOG          = LogFactory.getLog(OlasConfigurationServiceFactory.class);

    private int              usageCounter = 0;


    public Object getService(Bundle bundle, ServiceRegistration registration) {

        LOG.debug("Create object of OlasLogService for " + bundle.getSymbolicName());
        usageCounter++;
        LOG.debug("Number of bundles using service " + usageCounter);
        return new OlasLogServiceImpl(bundle.getSymbolicName());
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {

        LOG.debug("Release object of OlasLogService for " + bundle.getSymbolicName());
        usageCounter--;
        LOG.debug("Number of bundles using service " + usageCounter);
    }

}
