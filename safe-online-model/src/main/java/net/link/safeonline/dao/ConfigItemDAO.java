/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;

@Local
public interface ConfigItemDAO {

	ConfigItemEntity addConfigItem(String name, String value,
			ConfigGroupEntity configGroup);

	void removeConfigItem(ConfigItemEntity configItem);

	void saveConfigItem(ConfigItemEntity configItem);

	ConfigItemEntity findConfigItem(String name);

	List<ConfigItemEntity> listConfigItems();

}
