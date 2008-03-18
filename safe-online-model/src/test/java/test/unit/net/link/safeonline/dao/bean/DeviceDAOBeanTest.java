/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.bean.DeviceClassDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class DeviceDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private DeviceDAOBean testedInstance;

	private DeviceClassDAOBean deviceClassDAO;

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
		this.deviceClassDAO = new DeviceClassDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.deviceClassDAO, this.entityTestManager
				.getEntityManager());

		EJBTestUtils.init(this.testedInstance);
		EJBTestUtils.init(this.deviceClassDAO);
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testDevice() {
		DeviceClassEntity deviceClass = this.deviceClassDAO.addDeviceClass(
				SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
				SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
		DeviceEntity device = this.testedInstance.addDevice("testDevice",
				deviceClass, null, null, null, null, null, null, null, null);
		List<DeviceEntity> devices = this.testedInstance.listDevices();
		assertEquals(device, devices.get(0));
	}

}
