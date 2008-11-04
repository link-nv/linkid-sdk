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
        this.entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        this.entityTestManager.setUp(TaskEntity.class, SchedulingEntity.class, TaskHistoryEntity.class);

        this.testedInstance = new TaskDAOBean();

        EJBTestUtils.inject(this.testedInstance, this.entityTestManager.getEntityManager());
        EJBTestUtils.init(this.testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        this.entityTestManager.tearDown();
        super.tearDown();
    }

    public void testTaskDAO() {

        TaskEntity taskEntity = this.testedInstance.addTaskEntity("testTask", "Test Task", null);
        assertNotNull(taskEntity);
        TaskEntity resultEntity = this.testedInstance.findTaskEntity("testTask");
        assertEquals(taskEntity, resultEntity);
        List<TaskEntity> taskEntities = this.testedInstance.listTaskEntities();
        assertEquals(taskEntity, taskEntities.get(0));
        this.testedInstance.removeTaskEntity(taskEntity);
        taskEntity = this.testedInstance.findTaskEntity("testTask");
        assertNull(taskEntity);
    }

}
