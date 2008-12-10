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
import net.link.safeonline.config.dao.ConfigItemValueDAO;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = ConfigItemValueDAO.JNDI_BINDING)
public class ConfigItemValueDAOBean implements ConfigItemValueDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                        entityManager;

    private ConfigItemValueEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, ConfigItemValueEntity.QueryInterface.class);
    }

    public ConfigItemValueEntity addConfigItemValue(ConfigItemEntity configItem, String value) {

        ConfigItemValueEntity configItemValue = new ConfigItemValueEntity(configItem, value);
        if (configItem != null) {
            configItem.getValues().add(configItemValue);
        }
        this.entityManager.persist(configItemValue);
        return configItemValue;
    }

    public void removeConfigItemValue(ConfigItemValueEntity configItemValue) {

        /*
         * Manage relationships.
         */
        configItemValue.getConfigItem().setValues(listConfigItemValues(configItemValue.getConfigItem()));
        configItemValue.getConfigItem().getValues().remove(configItemValue);
        configItemValue.getConfigItem().setValueIndex(0);

        /*
         * Remove from database.
         */
        this.entityManager.remove(configItemValue);
    }

    public void saveConfigItemValue(ConfigItemValueEntity configItemValue) {

        this.entityManager.merge(configItemValue);
    }

    public List<ConfigItemValueEntity> listConfigItemValues(ConfigItemEntity configItem) {

        List<ConfigItemValueEntity> result = this.queryObject.listConfigItemValues(configItem);
        return result;
    }

    public void removeConfigItemValues(ConfigItemEntity configItem) {

        List<ConfigItemValueEntity> configItemValues = listConfigItemValues(configItem);
        for (ConfigItemValueEntity configItemValue : configItemValues) {
            removeConfigItemValue(configItemValue);
        }
    }
}
