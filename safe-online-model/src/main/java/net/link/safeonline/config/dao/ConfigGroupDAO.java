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
import net.link.safeonline.entity.config.ConfigGroupEntity;


@Local
public interface ConfigGroupDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ConfigGroupDAOBean/local";


    ConfigGroupEntity addConfigGroup(String name);

    void removeConfigGroup(ConfigGroupEntity configGroup);

    void saveConfigGroup(ConfigGroupEntity configGroup);

    ConfigGroupEntity findConfigGroup(String name);

    List<ConfigGroupEntity> listConfigGroups();

}
