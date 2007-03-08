/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.ConfigurationProvider;
import net.link.safeonline.Startable;
import net.link.safeonline.dao.ConfigGroupDAO;
import net.link.safeonline.dao.ConfigItemDAO;
import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.model.ConfigStartable;
import net.link.safeonline.util.ee.EjbUtils;

@Stateless
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "ConfigStartableBean")
public class ConfigStartableBean implements ConfigStartable {

	private static final Log LOG = LogFactory.getLog(ConfigStartableBean.class);

	@EJB
	private ConfigGroupDAO configGroupDAO;

	@EJB
	private ConfigItemDAO configItemDAO;

	public int getPriority() {
		return Startable.PRIORITY_BOOTSTRAP;
	}

	public void postStart() {
		LOG.debug("Starting configuration discovery");

		// create a default group
		ConfigGroupEntity defaultGroup = this.configGroupDAO
				.findConfigGroup("overall");
		if (defaultGroup == null) {
			LOG.debug("Adding default config group");
			defaultGroup = this.configGroupDAO.addConfigGroup("overall");
		}

		// get list of existing configgroups and items
		List<ConfigGroupEntity> toBeRemovedGroups = this.configGroupDAO
				.listConfigGroups();
		List<ConfigItemEntity> toBeRemovedItems = this.configItemDAO
				.listConfigItems();

		// iterate over all registered configuration providers.
		Map<String, ConfigurationProvider> configNameMap = EjbUtils
				.getComponentNames(ConfigurationProvider.JNDI_PREFIX,
						ConfigurationProvider.class);
		for (Entry<String, ConfigurationProvider> configEntry : configNameMap
				.entrySet()) {
			ConfigurationProvider provider = configEntry.getValue();
			String groupName = provider.getGroupName();

			ConfigGroupEntity configGroup = null;
			if (groupName == null || groupName.equals("")) {
				// use the default group if no name was specified
				configGroup = defaultGroup;
			} else {
				// else find the group by name
				configGroup = this.configGroupDAO.findConfigGroup(groupName);
				if (configGroup == null) {
					// if the group did not exist, create it
					configGroup = this.configGroupDAO.addConfigGroup(groupName);
				}
			}
			// save the group from destruction
			toBeRemovedGroups.remove(configGroup);

			// now iterate over all configuration parameters for this provider
			for (Entry<String, String> entry : provider
					.getConfigurationParameters().entrySet()) {
				String parameterName = entry.getKey();
				String defaultValue = entry.getValue();

				// find the item if present
				ConfigItemEntity configItem = this.configItemDAO
						.findConfigItem(parameterName);
				if (configItem == null) {
					// otherwise create it
					configItem = this.configItemDAO.addConfigItem(
							parameterName, defaultValue, configGroup);
				}
				if (configItem.getValue() == null
						|| configItem.getValue().equals("")) {
					// if no value is set, set the default value
					configItem.setValue(defaultValue);
				}
				// set the config group
				configItem.setConfigGroup(configGroup);
				configGroup.getConfigItems().add(configItem);

				// save the item from destruction
				toBeRemovedItems.remove(configItem);
			}

			for (ConfigItemEntity toBeRemovedItem : toBeRemovedItems) {
				this.configItemDAO.removeConfigItem(toBeRemovedItem);
			}
			for (ConfigGroupEntity toBeRemovedGroup : toBeRemovedGroups) {
				this.configGroupDAO.removeConfigGroup(toBeRemovedGroup);
			}
		}
	}

	public void preStop() {
		// empty

	}

}
