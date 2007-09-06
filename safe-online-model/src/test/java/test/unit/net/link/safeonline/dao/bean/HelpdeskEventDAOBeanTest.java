/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.entity.helpdesk.LogLevelType;
import net.link.safeonline.helpdesk.dao.bean.HelpdeskEventDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class HelpdeskEventDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private HelpdeskEventDAOBean testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

		this.testedInstance = new HelpdeskEventDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());

		EJBTestUtils.init(this.testedInstance);
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testLogs() {
		HelpdeskContextEntity context = new HelpdeskContextEntity(new Long(0));
		List<HelpdeskEventEntity> events = new Vector<HelpdeskEventEntity>();
		events.add(new HelpdeskEventEntity(context, new Date(),
				"test-message-1", "test-principal", LogLevelType.INFO));
		events.add(new HelpdeskEventEntity(context, new Date(),
				"test-message-2", "test-principal", LogLevelType.ERROR));
		this.testedInstance.persist(events);

		List<HelpdeskEventEntity> persisted_events = this.testedInstance
				.listLogs(new Long(0));
		assertEquals(persisted_events.size(), events.size());
	}

}
