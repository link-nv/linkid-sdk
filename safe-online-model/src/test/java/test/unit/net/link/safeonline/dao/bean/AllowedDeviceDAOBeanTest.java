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
import net.link.safeonline.dao.bean.AllowedDeviceDAOBean;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.DeviceClassDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class AllowedDeviceDAOBeanTest extends TestCase {

    private EntityTestManager       entityTestManager;

    private AllowedDeviceDAOBean    testedInstance;

    private ApplicationDAOBean      applicationDAO;

    private DeviceDAOBean           deviceDAO;

    private DeviceClassDAOBean      deviceClassDAO;

    private ApplicationOwnerDAOBean applicationOwnerDAO;

    private SubjectDAOBean          subjectDAO;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(SafeOnlineTestContainer.entities);

        testedInstance = new AllowedDeviceDAOBean();
        deviceDAO = new DeviceDAOBean();
        deviceClassDAO = new DeviceClassDAOBean();
        applicationDAO = new ApplicationDAOBean();
        applicationOwnerDAO = new ApplicationOwnerDAOBean();
        subjectDAO = new SubjectDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());
        EJBTestUtils.inject(deviceDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(deviceClassDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(applicationDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(applicationOwnerDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(subjectDAO, entityTestManager.getEntityManager());

        EJBTestUtils.init(deviceDAO);
        EJBTestUtils.init(deviceClassDAO);
        EJBTestUtils.init(applicationDAO);
        EJBTestUtils.init(applicationOwnerDAO);
        EJBTestUtils.init(subjectDAO);
        EJBTestUtils.init(testedInstance);

    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testAllowedDevice() {

        subjectDAO.addSubject("testsubject");
        SubjectEntity subject = subjectDAO.findSubject("testsubject");

        applicationOwnerDAO.addApplicationOwner("testowner", subject);
        ApplicationOwnerEntity applicationOwner = applicationOwnerDAO.findApplicationOwner("testowner");
        ApplicationEntity application = applicationDAO.addApplication("testapp", null, applicationOwner, null, null, null, null);

        DeviceClassEntity deviceClass = deviceClassDAO.addDeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = deviceDAO.addDevice("testDevice", deviceClass, null, null, null, null, null, null, null, null, null, null,
                null);
        AllowedDeviceEntity allowedDevice = testedInstance.addAllowedDevice(application, device, 0);
        List<AllowedDeviceEntity> allowedDevices = testedInstance.listAllowedDevices(application);
        assertEquals(allowedDevice, allowedDevices.get(0));
    }

    public void testAllowedDeviceNoApplication() {

        DeviceClassEntity deviceClass = deviceClassDAO.addDeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = deviceDAO.addDevice("testDevice", deviceClass, null, null, null, null, null, null, null, null, null, null,
                null);
        try {
            testedInstance.addAllowedDevice(null, device, 0);
            entityTestManager.getEntityManager().flush();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testAllowedDeviceNoDevice() {

        subjectDAO.addSubject("testsubject");
        SubjectEntity subject = subjectDAO.findSubject("testsubject");

        applicationOwnerDAO.addApplicationOwner("testowner", subject);
        ApplicationOwnerEntity applicationOwner = applicationOwnerDAO.findApplicationOwner("testowner");
        ApplicationEntity application = applicationDAO.addApplication("testapp", null, applicationOwner, null, null, null, null);

        try {
            testedInstance.addAllowedDevice(application, null, 0);
            entityTestManager.getEntityManager().flush();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testDeleteDevices() {

        subjectDAO.addSubject("testsubject");
        SubjectEntity subject = subjectDAO.findSubject("testsubject");

        applicationOwnerDAO.addApplicationOwner("testowner", subject);
        ApplicationOwnerEntity applicationOwner = applicationOwnerDAO.findApplicationOwner("testowner");
        ApplicationEntity application = applicationDAO.addApplication("testapp", null, applicationOwner, null, null, null, null);

        DeviceClassEntity deviceClass = deviceClassDAO.addDeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = deviceDAO.addDevice("testDevice", deviceClass, null, null, null, null, null, null, null, null, null, null,
                null);
        testedInstance.addAllowedDevice(application, device, 0);
        testedInstance.deleteAllowedDevices(application);
        List<AllowedDeviceEntity> allowedDevices = testedInstance.listAllowedDevices(application);
        assertEquals(0, allowedDevices.size());
    }

}
