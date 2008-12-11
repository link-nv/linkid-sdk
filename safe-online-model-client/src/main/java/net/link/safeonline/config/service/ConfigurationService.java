/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;


@Local
public interface ConfigurationService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ConfigurationServiceBean/local";


    List<ConfigGroupEntity> listConfigGroups();

    void saveConfiguration(List<ConfigGroupEntity> configGroupList);

    void saveConfigItem(ConfigItemEntity configItem);

    List<ConfigItemValueEntity> listConfigItemValues(ConfigItemEntity configItem);
}
