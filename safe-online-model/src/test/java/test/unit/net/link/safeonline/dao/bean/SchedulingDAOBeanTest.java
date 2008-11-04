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
        this.entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        this.entityTestManager.setUp(TaskEntity.class, SchedulingEntity.class, TaskHistoryEntity.class);

        this.testedInstance = new SchedulingDAOBean();

        EJBTestUtils.inject(this.testedInstance, this.entityTestManager.getEntityManager());

        EJBTestUtils.init(this.testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        this.entityTestManager.tearDown();
        super.tearDown();
    }

    public void testSchedulingDAO() {

        SchedulingEntity scheduling = this.testedInstance.addScheduling("test scheduling", "0 0 3 * * ?");
        SchedulingEntity resultScheduling = this.testedInstance.findSchedulingByName("test scheduling");
        assertEquals(scheduling, resultScheduling);
        List<SchedulingEntity> schedulingEntities = this.testedInstance.listSchedulings();
        assertEquals(scheduling, schedulingEntities.get(0));
        this.testedInstance.removeScheduling("test scheduling");
        resultScheduling = this.testedInstance.findSchedulingByName("test scheduling");
        assertNull(resultScheduling);
    }

}
