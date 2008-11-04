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
import net.link.safeonline.config.dao.ConfigGroupDAO;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = ConfigGroupDAO.JNDI_BINDING)
public class ConfigGroupDAOBean implements ConfigGroupDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                    entityManager;

    private ConfigGroupEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager, ConfigGroupEntity.QueryInterface.class);
    }

    public ConfigGroupEntity addConfigGroup(String name) {

        ConfigGroupEntity configGroup = new ConfigGroupEntity(name);
        this.entityManager.persist(configGroup);
        return configGroup;
    }

    public void removeConfigGroup(ConfigGroupEntity configGroup) {

        this.entityManager.remove(configGroup);
    }

    public void saveConfigGroup(ConfigGroupEntity configGroup) {

        this.entityManager.merge(configGroup);
    }

    public ConfigGroupEntity findConfigGroup(String name) {

        return this.entityManager.find(ConfigGroupEntity.class, name);
    }

    public List<ConfigGroupEntity> listConfigGroups() {

        List<ConfigGroupEntity> result = this.queryObject.listConfigGroups();
        return result;
    }

}
