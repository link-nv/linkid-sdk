/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.Date;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.tasks.dao.TaskDAO;
import net.link.safeonline.tasks.dao.TaskHistoryDAO;
import net.link.safeonline.tasks.dao.bean.TaskDAOBean;
import net.link.safeonline.tasks.dao.bean.TaskHistoryDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class TaskHistoryDAOBeanTest extends TestCase {

    private EntityTestManager entityTestManager;

    private TaskHistoryDAO    testedInstance;

    private TaskDAO           taskDAO;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(TaskEntity.class, TaskHistoryEntity.class, SchedulingEntity.class);

        EntityManager entityManager = entityTestManager.getEntityManager();
        taskDAO = EJBTestUtils.newInstance(TaskDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
        testedInstance = EJBTestUtils.newInstance(TaskHistoryDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testTaskHistoryList() {

        // Insert first task and history entry
        TaskEntity task1 = taskDAO.addTaskEntity("jndi", "name", null);
        Date startDate = new Date(System.currentTimeMillis());
        Date endDate = new Date(System.currentTimeMillis() + 1000);
        TaskHistoryEntity taskHistoryEntity1 = testedInstance.addTaskHistoryEntity(task1, "", true, startDate, endDate);

        // try to fetch history list from a detached task
        TaskEntity task2 = new TaskEntity("jndi", "name", null);
        TaskHistoryEntity taskHistoryEntity2 = testedInstance.listTaskHistory(task2).get(0);
        assertEquals(taskHistoryEntity1, taskHistoryEntity2);
    }

    public void testTaskHistoryClearing() {

        // setup
        TaskEntity task = taskDAO.addTaskEntity("jndi", "name", null);
        Date startDate = new Date(System.currentTimeMillis());
        Date endDate = new Date(System.currentTimeMillis() + 1000);
        TaskHistoryEntity taskHistoryEntity = testedInstance.addTaskHistoryEntity(task, "", true, startDate, endDate);

        EntityManager entityManager = entityTestManager.refreshEntityManager();
        testedInstance = EJBTestUtils.newInstance(TaskHistoryDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityTestManager.getEntityManager());
        taskDAO = EJBTestUtils.newInstance(TaskDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        // operate
        testedInstance.clearAllTasksHistory(System.currentTimeMillis());

        entityManager = entityTestManager.refreshEntityManager();
        testedInstance = EJBTestUtils.newInstance(TaskHistoryDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityTestManager.getEntityManager());
        taskDAO = EJBTestUtils.newInstance(TaskDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        // verify
        TaskEntity resultTask = taskDAO.findTaskEntity("jndi");
        TaskHistoryEntity result = testedInstance.listTaskHistory(resultTask).get(0);
        assertEquals(result, taskHistoryEntity);

        // operate
        testedInstance.clearAllTasksHistory(0);

        entityManager = entityTestManager.refreshEntityManager();
        testedInstance = EJBTestUtils.newInstance(TaskHistoryDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityTestManager.getEntityManager());
        taskDAO = EJBTestUtils.newInstance(TaskDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);

        // verify
        resultTask = taskDAO.findTaskEntity("jndi");
        int size = testedInstance.listTaskHistory(resultTask).size();
        assertEquals(0, size);
    }
}
