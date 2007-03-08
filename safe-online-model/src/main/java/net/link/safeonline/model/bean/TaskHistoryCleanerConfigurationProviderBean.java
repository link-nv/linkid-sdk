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

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.ConfigurationProvider;

@Stateless
@Local(ConfigurationProvider.class)
@LocalBinding(jndiBinding = ConfigurationProvider.JNDI_PREFIX + "/"
		+ "TaskHistoryCleanerConfigurationProviderBean")
public class TaskHistoryCleanerConfigurationProviderBean implements
		ConfigurationProvider {

	private Map<String, String> config = null;

	public static final String configAgeInMillis = "Task history age limit (ms)";

	public static final String configAgeInMillisDefault = "600000";

	private static final String groupName = "Task history cleaner";

	public TaskHistoryCleanerConfigurationProviderBean() {
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
