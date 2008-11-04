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
@LocalBinding(jndiBinding = UsageStatisticTaskBean.JNDI_BINDING)
@Configurable(group = "User Statistic Generation")
@Interceptors(ConfigurationInterceptor.class)
public class UsageStatisticTaskBean implements Task {

    public static final String    JNDI_BINDING        = Task.JNDI_PREFIX + "/UsageStatisticTaskBean/local";

    public static final String    name                = "Usage statistic task";

    public static final String    statisticName       = "Usage statistic";

    public static final String    statisticDomain     = "Usage statistic domain";

    public static final String    loginCounter        = "Login counter";

    @Configurable(name = "Active user limit (ms)")
    private Integer               activeLimitInMillis = 10 * 60 * 1000;

    @Configurable(name = "Keep stats for (ms)")
    private Integer               ageInMillis         = 100 * 60 * 1000;

    @EJB
    private StatisticDAO          statisticDAO;

    @EJB
    private StatisticDataPointDAO statisticDataPointDAO;

    @EJB
    private SubscriptionDAO       subscriptionDAO;

    @EJB
    private ApplicationDAO        applicationDAO;


    public String getName() {

        return UsageStatisticTaskBean.name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform()
            throws Exception {

        List<ApplicationEntity> applicationList = this.applicationDAO.listApplications();
        Date activeLimit = new Date(System.currentTimeMillis() - this.activeLimitInMillis);
        Date ageLimit = new Date(System.currentTimeMillis() - this.ageInMillis);

        for (ApplicationEntity application : applicationList) {
            long totalSubscriptions = this.subscriptionDAO.getNumberOfSubscriptions(application);
            long activeSubscriptions = this.subscriptionDAO.getActiveNumberOfSubscriptions(application, activeLimit);

            StatisticEntity statistic = this.statisticDAO.findOrAddStatisticByNameDomainAndApplication(statisticName, statisticDomain,
                    application);

            StatisticDataPointEntity loginCounterDP = this.statisticDataPointDAO.findOrAddStatisticDataPoint(loginCounter, statistic);

            this.statisticDataPointDAO.addStatisticDataPoint(statisticName, statistic, totalSubscriptions, activeSubscriptions,
                    loginCounterDP.getX());
            this.statisticDataPointDAO.cleanStatisticDataPoints(statistic, ageLimit);
            loginCounterDP.setX(0);

        }
    }
}
