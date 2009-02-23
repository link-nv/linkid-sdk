/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.configuration;

import net.link.safeonline.osgi.OSGIConfigurationService;
import net.link.safeonline.osgi.OlasConfigurationService;
import net.link.safeonline.util.ee.EjbUtils;


/**
 * <h2>{@link OlasConfigurationServiceImpl}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 10, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class OlasConfigurationServiceImpl implements OlasConfigurationService {

    /**
     * {@inheritDoc}
     */
    public Object getConfigurationValue(String group, String name, Object defaultValue) {

        OSGIConfigurationService configurationService = EjbUtils.getEJB(OSGIConfigurationService.JNDI_BINDING,
                OSGIConfigurationService.class);
        Object value = configurationService.getConfigurationValue(group, name);
        if (null == value) {
            initConfigurationValue(group, name, defaultValue);
            value = defaultValue;
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void initConfigurationValue(String group, String name, Object value) {

        OSGIConfigurationService configurationService = EjbUtils.getEJB(OSGIConfigurationService.JNDI_BINDING,
                OSGIConfigurationService.class);
        configurationService.initConfigurationValue(group, name, value);

    }

}
