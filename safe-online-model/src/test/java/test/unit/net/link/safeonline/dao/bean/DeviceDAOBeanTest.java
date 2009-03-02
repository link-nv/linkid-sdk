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

    private EntityTestManager  entityTestManager;

    private DeviceDAOBean      testedInstance;

    private DeviceClassDAOBean deviceClassDAO;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(SafeOnlineTestContainer.entities);

        testedInstance = new DeviceDAOBean();
        deviceClassDAO = new DeviceClassDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());
        EJBTestUtils.inject(deviceClassDAO, entityTestManager.getEntityManager());

        EJBTestUtils.init(testedInstance);
        EJBTestUtils.init(deviceClassDAO);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testDevice() {

        DeviceClassEntity deviceClass = deviceClassDAO.addDeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = testedInstance.addDevice("testDevice", deviceClass, null, null, null, null, null, null, null, null, null,
                null, null);
        List<DeviceEntity> devices = testedInstance.listDevices();
        assertEquals(device, devices.get(0));
    }

}
