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
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = StatisticDAO.JNDI_BINDING)
public class StatisticDAOBean implements StatisticDAO {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                  entityManager;

    @EJB(mappedName = StatisticDataPointDAO.JNDI_BINDING)
    StatisticDataPointDAO                  statisticDataPointDAO;

    private StatisticEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, StatisticEntity.QueryInterface.class);
    }

    public StatisticEntity addStatistic(String name, String domain, ApplicationEntity application) {

        StatisticEntity statistic = new StatisticEntity(name, domain, application, new Date());
        entityManager.persist(statistic);
        return statistic;
    }

    public StatisticEntity findStatisticById(long statisticId) {

        StatisticEntity result = entityManager.find(StatisticEntity.class, statisticId);
        return result;
    }

    public StatisticEntity findStatisticByNameDomainAndApplication(String name, String domain, ApplicationEntity application) {

        try {
            if (null == application)
                return queryObject.findStatisticWhereNameAndDomain(name, domain);
            return queryObject.findStatisticWhereNameDomainAndApplication(name, domain, application);
        } catch (Exception e) {
            return null;
        }
    }

    public StatisticEntity findOrAddStatisticByNameDomainAndApplication(String name, String domain, ApplicationEntity application) {

        StatisticEntity statistic = findStatisticByNameDomainAndApplication(name, domain, application);
        if (statistic == null) {
            statistic = addStatistic(name, domain, application);
        }
        return statistic;
    }

    public List<StatisticEntity> listStatistics(ApplicationEntity application) {

        if (null == application)
            return queryObject.listStatistics();
        return queryObject.listStatistics(application);
    }

    public void removeStatistics(ApplicationEntity application) {

        List<StatisticEntity> statistics = listStatistics(application);
        for (StatisticEntity statistic : statistics) {
            statisticDataPointDAO.cleanStatisticDataPoints(statistic);
            entityManager.remove(statistic);
        }
    }

    public void cleanDomain(String domain) {

        List<StatisticEntity> statistics = queryObject.listStatistics(domain);
        for (StatisticEntity statisticEntity : statistics) {
            statisticDataPointDAO.cleanStatisticDataPoints(statisticEntity);
        }
        queryObject.deleteWhereDomain(domain);
    }
}
