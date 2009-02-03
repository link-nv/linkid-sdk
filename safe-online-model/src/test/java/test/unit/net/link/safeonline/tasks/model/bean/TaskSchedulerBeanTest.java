/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.tasks.model.bean;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.Startable;
import net.link.safeonline.Task;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.tasks.dao.SchedulingDAO;
import net.link.safeonline.tasks.dao.bean.SchedulingDAOBean;
import net.link.safeonline.tasks.model.bean.TaskSchedulerBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class TaskSchedulerBeanTest {

    private static final Log  LOG = LogFactory.getLog(TaskSchedulerBeanTest.class);

    private EntityTestManager entityTestManager;

    private TaskSchedulerBean testedInstance;

    private TimerService      mockTimerService;

    private Timer             mockTimer;

    private JndiTestUtils     jndiTestUtils;

    private KeyService        mockKeyService;


    @Before
    public void setUp()
            throws Exception {

        mockTimerService = createMock(TimerService.class);
        mockTimer = createMock(Timer.class);
        testedInstance = new TaskSchedulerBean();
        EJBTestUtils.inject(testedInstance, mockTimerService);

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        final KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineKeyStore.class)).andReturn(
                new PrivateKeyEntry(olasKeyPair.getPrivate(), new Certificate[] { olasCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);

        systemStartable.postStart();

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
    }

    @After
    public void tearDown()
            throws Exception {

        if (entityTestManager != null) {
            entityTestManager.tearDown();
        }
        if (jndiTestUtils != null) {
            jndiTestUtils.tearDown();
        }
    }

    @Test
    public void testSetTimer()
            throws Exception {

        // setup
        SchedulingEntity scheduling = new SchedulingEntity("test", "0 0/5 * * * ?", null);

        expect(mockTimerService.createTimer((Date) anyObject(), (String) anyObject())).andReturn(mockTimer);
        expect(mockTimer.getHandle()).andReturn(null);
        expect(mockTimerService.createTimer((Date) anyObject(), (String) anyObject())).andReturn(mockTimer);
        expect(mockTimer.getHandle()).andReturn(null);
        replay(mockTimerService);
        replay(mockTimer);

        // operate
        testedInstance.setTimer(scheduling);
        Date firstDate = scheduling.getFireDate();
        testedInstance.setTimer(scheduling);
        Date nextDate = scheduling.getFireDate();
        assertFalse(firstDate.equals(nextDate));
    }

    @Test
    public void testPostStart()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        testedInstance = EJBTestUtils.newInstance(TaskSchedulerBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        Task testTaskComponent = new TestTask();
        jndiTestUtils.bindComponent(TestTask.JNDI_BINDING, testTaskComponent);

        // operate
        LOG.debug("------------------ FIRST POST START -------------------------");
        testedInstance.postStart();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();

        SchedulingDAO schedulingDAO = EJBTestUtils
                                                  .newInstance(SchedulingDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
        SchedulingEntity defaultScheduling = schedulingDAO.findSchedulingByName("default");
        assertNotNull(defaultScheduling);
        List<TaskEntity> defaultTasks = defaultScheduling.getTasks();
        assertNotNull(defaultTasks);
        assertEquals(1, defaultTasks.size());
        assertEquals(TestTask.JNDI_BINDING, defaultTasks.get(0).getJndiName());
        assertEquals("test-task", defaultTasks.get(0).getName());
        assertEquals(defaultScheduling, defaultTasks.get(0).getScheduling());

        LOG.debug("------------------ SECOND POST START ----------------------------");
        /*
         * We run postStart twice since the task scheduler bean must be capable of rebooting using a non-volatile database.
         */
        testedInstance.postStart();
    }


    static class TestTask implements Task {

        public static final String JNDI_BINDING = Task.JNDI_PREFIX + "TestTaskComponent/local";

        private static final Log   taskLOG      = LogFactory.getLog(TestTask.class);


        public String getName() {

            return "test-task";
        }

        public void perform() {

            taskLOG.debug("perform");
        }
    }
}
