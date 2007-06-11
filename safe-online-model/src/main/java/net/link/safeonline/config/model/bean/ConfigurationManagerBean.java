/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.entity.config.ConfigItemEntity;

@Stateless
public class ConfigurationManagerBean implements ConfigurationManager {

	@EJB
	private ConfigItemDAO configItemDAO;

	public ConfigItemEntity findConfigItem(String name) {
		return this.configItemDAO.findConfigItem(name);
	}
}
