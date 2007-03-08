/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.demo;

import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.ConfigurationProvider;

@Stateless
@Local(ConfigurationProvider.class)
@LocalBinding(jndiBinding = ConfigurationProvider.JNDI_PREFIX + "/"
		+ "DemoTaskBean")
public class DemoConfigurationProvider implements ConfigurationProvider {

	private Map<String, String> config = null;

	public DemoConfigurationProvider() {
		// a Treemap to keep the entries sorted
		this.config = new TreeMap<String, String>();
		this.config.put("To be or not to be?", "yes");
		this.config.put("This is not true", "erm ...");
	}

	public String getGroupName() {
		return "Demo configuration provider";
	}

	public Map<String, String> getConfigurationParameters() {
		return this.config;
	}

}
