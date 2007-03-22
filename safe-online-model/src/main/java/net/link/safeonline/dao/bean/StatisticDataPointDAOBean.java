/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;

@Stateless
public class StatisticDataPointDAOBean implements StatisticDataPointDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public StatisticDataPointEntity addStatisticDataPoint(String name,
			StatisticEntity statistic, long x, long y, long z) {
		StatisticDataPointEntity statisticDataPoint = new StatisticDataPointEntity(
				name, statistic, new Date(), x, y, z);
		this.entityManager.persist(statisticDataPoint);
		statistic.getStatisticDataPoints().add(statisticDataPoint);
		return statisticDataPoint;
	}

	public void cleanStatisticDataPoints(StatisticEntity statistic) {
		Query query = StatisticDataPointEntity.createQueryDeleteWhereStatistic(
				this.entityManager, statistic);
		query.executeUpdate();
	}

}
