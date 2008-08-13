/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;


@Local
public interface ConfigItemDAO {

    ConfigItemEntity addConfigItem(String name, String value, String valueType, ConfigGroupEntity configGroup);

    void removeConfigItem(ConfigItemEntity configItem);

    void saveConfigItem(ConfigItemEntity configItem);

    ConfigItemEntity findConfigItem(String name);

    List<ConfigItemEntity> listConfigItems();
}
