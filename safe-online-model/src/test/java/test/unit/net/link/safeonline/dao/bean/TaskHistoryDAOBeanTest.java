/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.Date;

import net.link.safeonline.dao.bean.TaskDAOBean;
import net.link.safeonline.dao.bean.TaskHistoryDAOBean;
import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.entity.TaskHistoryEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class TaskHistoryDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private TaskHistoryDAOBean testedInstance;

	private TaskDAOBean taskDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(TaskEntity.class, TaskHistoryEntity.class,
				SchedulingEntity.class);

		this.taskDAO = new TaskDAOBean();
		this.testedInstance = new TaskHistoryDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.taskDAO, this.entityTestManager
				.getEntityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testTaskHistoryList() {
		// Insert first task and history entry
		TaskEntity task1 = this.taskDAO.addTaskEntity("jndi", "name", null);
		Date startDate = new Date(System.currentTimeMillis());
		Date endDate = new Date(System.currentTimeMillis() + 1000);
		TaskHistoryEntity taskHistoryEntity1 = this.testedInstance
				.addTaskHistoryEntity(task1, "", true, startDate, endDate);

		// try to fetch history list from a detached task
		TaskEntity task2 = new TaskEntity("jndi", "name", null);
		TaskHistoryEntity taskHistoryEntity2 = this.testedInstance
				.getTaskHistory(task2).get(0);
		assertEquals(taskHistoryEntity1, taskHistoryEntity2);
	}
}
