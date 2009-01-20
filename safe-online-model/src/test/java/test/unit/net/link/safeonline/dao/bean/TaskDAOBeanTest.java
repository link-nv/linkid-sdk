/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.tasks.dao.bean.TaskDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;


public class TaskDAOBeanTest extends TestCase {

    private EntityTestManager entityTestManager;

    private TaskDAOBean       testedInstance;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(TaskEntity.class, SchedulingEntity.class, TaskHistoryEntity.class);

        testedInstance = new TaskDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());
        EJBTestUtils.init(testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testTaskDAO() {

        TaskEntity taskEntity = testedInstance.addTaskEntity("testTask", "Test Task", null);
        assertNotNull(taskEntity);
        TaskEntity resultEntity = testedInstance.findTaskEntity("testTask");
        assertEquals(taskEntity, resultEntity);
        List<TaskEntity> taskEntities = testedInstance.listTaskEntities();
        assertEquals(taskEntity, taskEntities.get(0));
        testedInstance.removeTaskEntity(taskEntity);
        taskEntity = testedInstance.findTaskEntity("testTask");
        assertNull(taskEntity);
    }

}
