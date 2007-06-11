/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.config.dao.ConfigGroupDAO;
import net.link.safeonline.entity.config.ConfigGroupEntity;

@Stateless
public class ConfigGroupDAOBean implements ConfigGroupDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

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

	@SuppressWarnings("unchecked")
	public List<ConfigGroupEntity> listConfigGroups() {
		Query query = ConfigGroupEntity.createQueryListAll(this.entityManager);
		List<ConfigGroupEntity> result = query.getResultList();
		return result;
	}

}
