/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = StatisticDataPointDAO.JNDI_BINDING)
public class StatisticDataPointDAOBean implements StatisticDataPointDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                           entityManager;

    private StatisticDataPointEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, StatisticDataPointEntity.QueryInterface.class);
    }

    public StatisticDataPointEntity addStatisticDataPoint(String name, StatisticEntity statistic, long x, long y, long z) {

        StatisticDataPointEntity statisticDataPoint = new StatisticDataPointEntity(name, statistic, new Date(), x, y, z);
        entityManager.persist(statisticDataPoint);
        statistic.getStatisticDataPoints().add(statisticDataPoint);
        statistic.setCreationTime(new Date());
        return statisticDataPoint;
    }

    public List<StatisticDataPointEntity> listStatisticDataPoints(String name, StatisticEntity statistic) {

        return queryObject.listStatisticDataPoints(name, statistic);
    }

    public StatisticDataPointEntity findOrAddStatisticDataPoint(String name, StatisticEntity statistic) {

        StatisticDataPointEntity dp = null;
        List<StatisticDataPointEntity> dps = listStatisticDataPoints(name, statistic);
        if (dps.size() > 0) {
            dp = dps.get(0);
        } else {
            dp = addStatisticDataPoint(name, statistic, 0, 0, 0);
        }
        return dp;
    }

    public void cleanStatisticDataPoints(StatisticEntity statistic) {

        queryObject.deleteWhereStatistic(statistic);
    }

    public void cleanStatisticDataPoints(StatisticEntity statistic, Date ageLimit) {

        queryObject.deleteWhereStatisticExpired(statistic, ageLimit);
    }

}
