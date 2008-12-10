/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;


@Local
public interface ConfigItemValueDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ConfigItemValueDAOBean/local";


    ConfigItemValueEntity addConfigItemValue(ConfigItemEntity configItem, String value);

    void removeConfigItemValue(ConfigItemValueEntity configItemValue);

    void saveConfigItemValue(ConfigItemValueEntity configItemValue);

    List<ConfigItemValueEntity> listConfigItemValues(ConfigItemEntity configItem);

    void removeConfigItemValues(ConfigItemEntity configItem);
}
