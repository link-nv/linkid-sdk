/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;


/**
 * Configuration Manager interface. Allows for components to access the configuration outside of a security domain.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ConfigurationManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ConfigurationManagerBean/local";


    void addConfigurationValue(String group, String name, boolean multipleChoice, Object value);

    void removeConfigurationValue(String group, String name, Object value);

    void configure(Object object);
}
