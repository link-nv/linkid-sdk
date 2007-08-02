/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model;

import javax.ejb.Local;

import net.link.safeonline.entity.config.ConfigItemEntity;

/**
 * Configuration Manager interface. Allows for components to access the
 * configuration outside of a security domain. Also manages some configuration
 * parameters that do not belong to a particular model bean.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ConfigurationManager {

	ConfigItemEntity findConfigItem(String name);

	long getMaximumWsSecurityTimestampOffset();
}
