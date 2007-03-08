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

@Local
public interface ConfigGroupDAO {

	ConfigGroupEntity addConfigGroup(String name);

	void removeConfigGroup(ConfigGroupEntity configGroup);

	void saveConfigGroup(ConfigGroupEntity configGroup);

	ConfigGroupEntity findConfigGroup(String name);

	List<ConfigGroupEntity> listConfigGroups();

}
