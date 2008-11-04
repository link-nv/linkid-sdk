/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.tasks.model.bean;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
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
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.tasks.dao.SchedulingDAO;
import net.link.safeonline.tasks.dao.bean.SchedulingDAOBean;
import net.link.safeonline.tasks.model.bean.TaskSchedulerBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

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


    @Before
    public void setUp()
            throws Exception {

        this.mockTimerService = createMock(TimerService.class);
        this.mockTimer = createMock(Timer.class);
        this.testedInstance = new TaskSchedulerBean();
        EJBTestUtils.inject(this.testedInstance, this.mockTimerService);

        this.entityTestManager = new EntityTestManager();
        this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = this.entityTestManager.getEntityManager();

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
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

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
    }

    @After
    public void tearDown()
            throws Exception {

        this.entityTestManager.tearDown();
        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testSetTimer()
            throws Exception {

        // setup
        SchedulingEntity scheduling = new SchedulingEntity("test", "0 0/5 * * * ?", null);

        expect(this.mockTimerService.createTimer((Date) anyObject(), (String) anyObject())).andReturn(this.mockTimer);
        expect(this.mockTimer.getHandle()).andReturn(null);
        expect(this.mockTimerService.createTimer((Date) anyObject(), (String) anyObject())).andReturn(this.mockTimer);
        expect(this.mockTimer.getHandle()).andReturn(null);
        replay(this.mockTimerService);
        replay(this.mockTimer);

        // operate
        this.testedInstance.setTimer(scheduling);
        Date firstDate = scheduling.getFireDate();
        this.testedInstance.setTimer(scheduling);
        Date nextDate = scheduling.getFireDate();
        assertFalse(firstDate.equals(nextDate));
    }

    @Test
    public void testPostStart()
            throws Exception {

        // setup
        EntityManager entityManager = this.entityTestManager.getEntityManager();
        this.testedInstance = EJBTestUtils.newInstance(TaskSchedulerBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        Task testTaskComponent = new TestTask();
        String testTaskJndiName = "SafeOnline/task/TestTaskComponent";
        this.jndiTestUtils.bindComponent(testTaskJndiName, testTaskComponent);

        // operate
        LOG.debug("------------------ FIRST POST START -------------------------");
        this.testedInstance.postStart();
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
        assertEquals(testTaskJndiName, defaultTasks.get(0).getJndiName());
        assertEquals("test-task", defaultTasks.get(0).getName());
        assertEquals(defaultScheduling, defaultTasks.get(0).getScheduling());

        LOG.debug("------------------ SECOND POST START ----------------------------");
        /*
         * We run postStart twice since the task scheduler bean must be capable of rebooting using a non-volatile database.
         */
        this.testedInstance.postStart();
    }


    static class TestTask implements Task {

        private static final Log taskLOG = LogFactory.getLog(TestTask.class);


        public String getName() {

            return "test-task";
        }

        public void perform() {

            taskLOG.debug("perform");
        }
    }
}
