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
import net.link.safeonline.tasks.dao.bean.SchedulingDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;


public class SchedulingDAOBeanTest extends TestCase {

    private EntityTestManager entityTestManager;

    private SchedulingDAOBean testedInstance;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(TaskEntity.class, SchedulingEntity.class, TaskHistoryEntity.class);

        testedInstance = new SchedulingDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());

        EJBTestUtils.init(testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testSchedulingDAO() {

        SchedulingEntity scheduling = testedInstance.addScheduling("test scheduling", "0 0 3 * * ?");
        SchedulingEntity resultScheduling = testedInstance.findSchedulingByName("test scheduling");
        assertEquals(scheduling, resultScheduling);
        List<SchedulingEntity> schedulingEntities = testedInstance.listSchedulings();
        assertEquals(scheduling, schedulingEntities.get(0));
        testedInstance.removeScheduling("test scheduling");
        resultScheduling = testedInstance.findSchedulingByName("test scheduling");
        assertNull(resultScheduling);
    }

}
