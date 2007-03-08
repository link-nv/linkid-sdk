/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline;

import java.util.Map;

/**
 * Components implementing this interface register configurable options
 * 
 * @author dhouthoo
 * 
 */
public interface ConfigurationProvider {

	public static final String JNDI_PREFIX = "SafeOnline/config";

	/**
	 * Returns a map of configuration parameters with a name and defaultvalue
	 * 
	 * @return
	 */
	Map<String, String> getConfigurationParameters();

	/**
	 * Returns the groupname to which the configuration parameters may be
	 * registered
	 * 
	 * @return
	 */
	String getGroupName();

}
