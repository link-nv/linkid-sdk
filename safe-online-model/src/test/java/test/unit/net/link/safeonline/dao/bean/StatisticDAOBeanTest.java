/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import junit.framework.TestCase;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.StatisticDAOBean;
import net.link.safeonline.dao.bean.StatisticDataPointDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;


public class StatisticDAOBeanTest extends TestCase {

    private EntityTestManager         entityTestManager;

    private StatisticDAOBean          testedInstance;

    private StatisticDataPointDAOBean statisticDataPointDAO;

    private ApplicationDAOBean        applicationDAO;

    private ApplicationOwnerDAOBean   applicationOwnerDAO;

    private SubjectDAOBean            subjectDAO;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(StatisticEntity.class, StatisticDataPointEntity.class, ApplicationEntity.class,
                ApplicationOwnerEntity.class, SubjectEntity.class, ApplicationPoolEntity.class);
        // StatisticDataPointEntity.class,
        testedInstance = new StatisticDAOBean();
        statisticDataPointDAO = new StatisticDataPointDAOBean();
        applicationDAO = new ApplicationDAOBean();
        applicationOwnerDAO = new ApplicationOwnerDAOBean();
        subjectDAO = new SubjectDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());
        EJBTestUtils.inject(statisticDataPointDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(testedInstance, statisticDataPointDAO);
        EJBTestUtils.inject(applicationDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(applicationOwnerDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(subjectDAO, entityTestManager.getEntityManager());

        EJBTestUtils.init(statisticDataPointDAO);
        EJBTestUtils.init(testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testStatistic() {

        // setup
        SubjectEntity subject = subjectDAO.addSubject("testsubject");
        applicationOwnerDAO.addApplicationOwner("testowner", subject);
        ApplicationOwnerEntity owner = applicationOwnerDAO.findApplicationOwner("testowner");
        ApplicationEntity application = applicationDAO.addApplication("testapp", null, owner, null, null, null, null);

        // operate
        StatisticEntity original = testedInstance.addStatistic("test", "domain", application);
        StatisticEntity result = testedInstance.findStatisticById(original.getId());
        assertEquals(original, result);
        result = testedInstance.findStatisticByNameDomainAndApplication("test", "domain", application);
        assertEquals(original, result);

        original = testedInstance.addStatistic("test2", "domain", null);
        result = testedInstance.findStatisticByNameDomainAndApplication("test2", "domain", null);
        assertEquals(original, result);

        @SuppressWarnings("unused")
        StatisticDataPointEntity dp1 = statisticDataPointDAO.addStatisticDataPoint("test", original, 1, 2, 3);

        @SuppressWarnings("unused")
        StatisticDataPointEntity dp2 = statisticDataPointDAO.addStatisticDataPoint("test2", original, 1, 2, 3);

        testedInstance.cleanDomain("domain");
        result = testedInstance.findStatisticByNameDomainAndApplication("test2", "domain", null);
        assertNull(result);

    }
}
