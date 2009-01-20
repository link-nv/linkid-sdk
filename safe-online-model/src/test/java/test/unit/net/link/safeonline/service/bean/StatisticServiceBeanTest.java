/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.bean.StatisticDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.StatisticService;
import net.link.safeonline.service.bean.StatisticServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class StatisticServiceBeanTest extends TestCase {

    private EntityTestManager entityTestManager;


    @Override
    public void setUp()
            throws Exception {

        super.setUp();

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return authCertificate;
            }
        });

        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return certificate;
            }
        });

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        systemStartable.postStart();
        entityTestManager.refreshEntityManager();
    }

    @Override
    public void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testChartGeneration()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();

        String testChartName = "test-chart-name-" + UUID.randomUUID().toString();
        String testDomain = "test-domain-" + UUID.randomUUID().toString();
        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", "operator");

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testAdminLogin);
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null, null, false,
                false, false, null);
        ApplicationEntity application = applicationService.getApplication(testApplicationName);
        StatisticDAO statisticDAO = EJBTestUtils.newInstance(StatisticDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        StatisticEntity statistic = statisticDAO.addStatistic(testChartName, testDomain, application);

        Random generator = new Random();
        StatisticDataPointEntity dp = new StatisticDataPointEntity("Cat A", statistic, new Date(), generator.nextInt(),
                generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);
        dp = new StatisticDataPointEntity("Cat B", statistic, new Date(), generator.nextInt(), generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);
        dp = new StatisticDataPointEntity("Cat C", statistic, new Date(), generator.nextInt(), generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);
        dp = new StatisticDataPointEntity("Cat D", statistic, new Date(), generator.nextInt(), generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);

        // operate
        StatisticService statisticService = EJBTestUtils.newInstance(StatisticServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, "test-operator", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE, SafeOnlineRoles.OPERATOR_ROLE);

        JFreeChart chart = statisticService.getChart(testChartName, testDomain, testApplicationName);

        // verify
        File file = File.createTempFile("tempchart-", ".png");
        FileOutputStream out = new FileOutputStream(file);
        out.write(ChartUtilities.encodeAsPNG(chart.createBufferedImage(800, 600)));
    }

    public void testExport()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();

        String testChartName = "test-chart-name";
        String testDomain = "test-domain-" + UUID.randomUUID().toString();
        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", "operator");

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testAdminLogin);
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null, null, false,
                false, false, null);
        ApplicationEntity application = applicationService.getApplication(testApplicationName);
        StatisticDAO statisticDAO = EJBTestUtils.newInstance(StatisticDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        StatisticEntity statistic = statisticDAO.addStatistic(testChartName, testDomain, application);

        Random generator = new Random();
        StatisticDataPointEntity dp = new StatisticDataPointEntity("Cat A", statistic, new Date(), generator.nextInt(),
                generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);
        dp = new StatisticDataPointEntity("Cat B", statistic, new Date(), generator.nextInt(), generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);
        dp = new StatisticDataPointEntity("Cat C", statistic, new Date(), generator.nextInt(), generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);
        dp = new StatisticDataPointEntity("Cat D", statistic, new Date(), generator.nextInt(), generator.nextInt(), generator.nextInt());
        statistic.getStatisticDataPoints().add(dp);

        // operate
        StatisticService statisticService = EJBTestUtils.newInstance(StatisticServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, "test-operator", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE, SafeOnlineRoles.OPERATOR_ROLE);

        HSSFWorkbook workbook = statisticService.exportStatistic(testChartName, testDomain, testApplicationName);

        // verify
        assertNotNull(workbook);
    }
}
