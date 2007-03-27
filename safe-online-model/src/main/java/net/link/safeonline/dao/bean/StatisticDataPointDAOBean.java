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
		statistic.setCreationTime(new Date());
		return statisticDataPoint;
	}

	@SuppressWarnings("unchecked")
	public List<StatisticDataPointEntity> listStatisticDataPoints(String name,
			StatisticEntity statistic) {
		Query query = StatisticDataPointEntity
				.createQueryWhereNameAndStatistic(this.entityManager, name,
						statistic);
		return query.getResultList();

	}

	public StatisticDataPointEntity findOrAddStatisticDataPoint(String name,
			StatisticEntity statistic) {
		StatisticDataPointEntity dp = null;
		List<StatisticDataPointEntity> dps = this.listStatisticDataPoints(name,
				statistic);
		if (dps.size() > 0) {
			dp = dps.get(0);
		} else {
			dp = this.addStatisticDataPoint(name, statistic, 0, 0, 0);
		}
		return dp;
	}

	public void cleanStatisticDataPoints(StatisticEntity statistic) {
		Query query = StatisticDataPointEntity.createQueryDeleteWhereStatistic(
				this.entityManager, statistic);
		query.executeUpdate();
	}

	public void cleanStatisticDataPoints(StatisticEntity statistic,
			long ageInMillis) {
		Query query = StatisticDataPointEntity
				.createQueryDeleteWhereStatisticExpired(this.entityManager,
						statistic, ageInMillis);
		query.executeUpdate();
	}

}
