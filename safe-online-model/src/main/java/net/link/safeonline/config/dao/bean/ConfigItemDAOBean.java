/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemPK;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = ConfigItemDAO.JNDI_BINDING)
public class ConfigItemDAOBean implements ConfigItemDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                   entityManager;

    private ConfigItemEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, ConfigItemEntity.QueryInterface.class);
    }

    public ConfigItemEntity addConfigItem(String name, String valueType, boolean multipleChoice, ConfigGroupEntity configGroup) {

        ConfigItemEntity configItem = new ConfigItemEntity(name, valueType, multipleChoice, configGroup);
        if (configGroup != null) {
            configGroup.getConfigItems().add(configItem);
        }
        this.entityManager.persist(configItem);
        return configItem;
    }

    public void removeConfigItem(ConfigItemEntity configItem) {

        this.entityManager.remove(configItem);
    }

    public void saveConfigItem(ConfigItemEntity configItem) {

        this.entityManager.merge(configItem);
    }

    public ConfigItemEntity findConfigItem(String groupName, String name) {

        return this.entityManager.find(ConfigItemEntity.class, new ConfigItemPK(groupName, name));
    }

    public List<ConfigItemEntity> listConfigItems() {

        List<ConfigItemEntity> result = this.queryObject.listConfigItems();
        return result;
    }
}
