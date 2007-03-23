/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class SystemInitializationStartableBeanTest extends TestCase {

	public void testPostStart() throws Exception {
		// setup
		EntityTestManager entityTestManager = new EntityTestManager();
		entityTestManager.setUp(SafeOnlineTestContainer.entities);
		EntityManager entityManager = entityTestManager.getEntityManager();

		Startable testedInstance = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);

		// operate
		testedInstance.postStart();
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();
		/*
		 * We run postStart twice since the system must be capable of rebooting
		 * using an persistent database.
		 */
		testedInstance.postStart();
		entityTestManager.tearDown();
	}
}
