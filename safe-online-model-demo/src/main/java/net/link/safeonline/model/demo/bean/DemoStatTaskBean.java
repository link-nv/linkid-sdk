/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.demo.bean;

import java.util.Random;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Task;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = DemoStatTaskBean.JNDI_BINDING)
public class DemoStatTaskBean implements Task {

    public static final String    JNDI_BINDING = Task.JNDI_PREFIX + "DemoStatTaskBean/local";

    @EJB(mappedName = StatisticDAO.JNDI_BINDING)
    private StatisticDAO          statisticDAO;

    @EJB(mappedName = StatisticDataPointDAO.JNDI_BINDING)
    private StatisticDataPointDAO statisticDataPointDAO;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO        applicationDAO;

    private final static String   STAT_NAME    = "demo stat";

    private final static String   STAT_DOMAIN  = "demo stat domain";


    public String getName() {

        return "Demo statistic generator";
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform() {

        ResourceBundle properties = ResourceBundle.getBundle("config");
        String demoAppWebappName = properties.getString("olas.demo.app.webapp.name");
        ApplicationEntity application = applicationDAO.findApplication(demoAppWebappName);
        if (application == null)
            return;
        StatisticEntity statistic = statisticDAO.findStatisticByNameDomainAndApplication(STAT_NAME, STAT_DOMAIN, application);
        if (statistic == null) {
            statistic = statisticDAO.addStatistic(STAT_NAME, STAT_DOMAIN, application);
        }
        Random generator = new Random();
        statisticDataPointDAO.cleanStatisticDataPoints(statistic);
        statisticDataPointDAO.addStatisticDataPoint("cat A", statistic, generator.nextInt(), generator.nextInt(), generator.nextInt());
        statisticDataPointDAO.addStatisticDataPoint("cat B", statistic, generator.nextInt(), generator.nextInt(), generator.nextInt());
        statisticDataPointDAO.addStatisticDataPoint("cat C", statistic, generator.nextInt(), generator.nextInt(), generator.nextInt());
        statisticDataPointDAO.addStatisticDataPoint("cat D", statistic, generator.nextInt(), generator.nextInt(), generator.nextInt());
    }
}
