/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;

@Stateless
public class StatisticDAOBean implements StatisticDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public StatisticEntity addStatistic(String name,
			ApplicationEntity application) {
		StatisticEntity statistic = new StatisticEntity(name, application,
				new Date());
		this.entityManager.persist(statistic);
		return statistic;
	}

	public StatisticEntity findStatisticById(long statisticId) {
		StatisticEntity result = this.entityManager.find(StatisticEntity.class,
				statisticId);
		return result;
	}

	public StatisticEntity findStatisticByNameAndApplication(String name,
			ApplicationEntity application) {
		Query query = StatisticEntity.createQueryWhereNameAndApplication(
				this.entityManager, name, application);
		StatisticEntity result = null;
		try {
			result = (StatisticEntity) query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<StatisticEntity> listStatistics(ApplicationEntity application) {
		Query query = StatisticEntity.createQueryWhereApplication(
				this.entityManager, application);
		List<StatisticEntity> result = query.getResultList();
		return result;
	}

}
