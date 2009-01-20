/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.osgi.OSGIConfigurationService;
import net.link.safeonline.osgi.OlasConfigurationService;
import net.link.safeonline.osgi.configuration.OlasConfigurationServiceImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link OSGIConfigurationServiceBean}<br>
 * <sub>OLAS Configuration Service used by OSGi bundles.</sub></h2>
 * 
 * <p>
 * This EJB is used by OSGi bundles for initializing and retrieving OLAS configuration items. It is called from
 * {@link OlasConfigurationServiceImpl} which is an implementation of the OLAS configuration service ( {@link OlasConfigurationService} that
 * external bundles can use.
 * </p>
 * 
 * <p>
 * <i>Dec 10, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Stateless
@LocalBinding(jndiBinding = OSGIConfigurationService.JNDI_BINDING)
public class OSGIConfigurationServiceBean implements OSGIConfigurationService {

    private static final Log     LOG = LogFactory.getLog(OSGIConfigurationServiceBean.class);

    @EJB(mappedName = ConfigurationManager.JNDI_BINDING)
    private ConfigurationManager configurationManager;


    /**
     * {@inheritDoc}
     */
    public Object getConfigurationValue(String group, String name) {

        return configurationManager.getConfigurationValue(group, name);
    }

    /**
     * {@inheritDoc}
     */
    public void initConfigurationValue(String group, String name, Object value) {

        LOG.debug("init configuration value: group=" + group + " name=" + name + " value=" + value.toString());

        if (null == getConfigurationValue(group, name)) {
            configurationManager.addConfigurationValue(group, name, false, value);
        }

    }
}
