/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.Task;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Configurable(group = "User Statistic Generation")
@Interceptors(ConfigurationInterceptor.class)
@Local(Task.class)
@LocalBinding(jndiBinding = UsageStatisticTaskBean.JNDI_BINDING)
public class UsageStatisticTaskBean implements Task {

    public static final String    JNDI_BINDING        = Task.JNDI_PREFIX + "UsageStatisticTaskBean/local";

    public static final String    name                = "Usage statistic task";

    public static final String    statisticName       = "Usage statistic";

    public static final String    statisticDomain     = "Usage statistic domain";

    public static final String    loginCounter        = "Login counter";

    @Configurable(name = "Active user limit (ms)")
    private Integer               activeLimitInMillis = 10 * 60 * 1000;

    @Configurable(name = "Keep stats for (ms)")
    private Integer               ageInMillis         = 100 * 60 * 1000;

    @EJB(mappedName = StatisticDAO.JNDI_BINDING)
    private StatisticDAO          statisticDAO;

    @EJB(mappedName = StatisticDataPointDAO.JNDI_BINDING)
    private StatisticDataPointDAO statisticDataPointDAO;

    @EJB(mappedName = SubscriptionDAO.JNDI_BINDING)
    private SubscriptionDAO       subscriptionDAO;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO        applicationDAO;


    public String getName() {

        return UsageStatisticTaskBean.name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform()
            throws Exception {

        List<ApplicationEntity> applicationList = applicationDAO.listApplications();
        Date activeLimit = new Date(System.currentTimeMillis() - activeLimitInMillis);
        Date ageLimit = new Date(System.currentTimeMillis() - ageInMillis);

        for (ApplicationEntity application : applicationList) {
            long totalSubscriptions = subscriptionDAO.getNumberOfSubscriptions(application);
            long activeSubscriptions = subscriptionDAO.getActiveNumberOfSubscriptions(application, activeLimit);

            StatisticEntity statistic = statisticDAO.findOrAddStatisticByNameDomainAndApplication(statisticName, statisticDomain,
                    application);

            StatisticDataPointEntity loginCounterDP = statisticDataPointDAO.findOrAddStatisticDataPoint(loginCounter, statistic);

            statisticDataPointDAO.addStatisticDataPoint(statisticName, statistic, totalSubscriptions, activeSubscriptions,
                    loginCounterDP.getX());
            statisticDataPointDAO.cleanStatisticDataPoints(statistic, ageLimit);
            loginCounterDP.setX(0);

        }
    }
}
