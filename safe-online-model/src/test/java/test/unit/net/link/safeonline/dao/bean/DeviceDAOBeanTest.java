/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class DeviceDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private DeviceDAOBean testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

		this.testedInstance = new DeviceDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testDevice() {
		DeviceEntity device = this.testedInstance.addDevice("testdevice");
		List<DeviceEntity> devices = this.testedInstance.listDevices();
		assertEquals(device, devices.get(0));
	}

}
