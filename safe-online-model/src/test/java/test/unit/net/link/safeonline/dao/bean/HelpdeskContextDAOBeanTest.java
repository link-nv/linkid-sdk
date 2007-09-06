/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.helpdesk.dao.bean.HelpdeskContextDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class HelpdeskContextDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private HelpdeskContextDAOBean testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

		this.testedInstance = new HelpdeskContextDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());

		EJBTestUtils.init(this.testedInstance);
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testContext() {
		HelpdeskContextEntity context = this.testedInstance
				.createHelpdeskContext();
		List<HelpdeskContextEntity> contexts = this.testedInstance
				.listContexts();
		assertEquals(context.getId(), contexts.get(0).getId());
	}

}
