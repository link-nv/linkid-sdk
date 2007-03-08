/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.entity.ConfigItemEntity;

/**
 * Configuration Manager interface. Allows for components to access the
 * configuration outside of a security domain.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ConfigurationManager {

	ConfigItemEntity getConfigItem(String name);
}
