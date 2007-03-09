/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.ConfigurationProvider;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(ConfigurationProvider.class)
@LocalBinding(jndiBinding = ConfigurationProvider.JNDI_PREFIX + "/"
		+ "HistoryCleanerConfigurationProviderBean")
public class HistoryCleanerConfigurationProviderBean implements
		ConfigurationProvider {

	private Map<String, String> config = null;

	public static final String configAgeInMillis = "Subject history age limit (ms)";

	public static final String configAgeInMillisDefault = "600000";

	private static final String groupName = "Subject history cleaner";

	public HistoryCleanerConfigurationProviderBean() {
		this.config = new TreeMap<String, String>();
		this.config.put(configAgeInMillis, configAgeInMillisDefault);
	}

	public Map<String, String> getConfigurationParameters() {
		return config;
	}

	public String getGroupName() {
		return groupName;
	}
}
