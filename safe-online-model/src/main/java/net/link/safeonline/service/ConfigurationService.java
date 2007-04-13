/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;

@Local
public interface ConfigurationService {

	List<ConfigGroupEntity> listConfigGroups();

	void saveConfiguration(List<ConfigGroupEntity> configGroupList);

	void saveConfigItem(ConfigItemEntity configItem);
}
