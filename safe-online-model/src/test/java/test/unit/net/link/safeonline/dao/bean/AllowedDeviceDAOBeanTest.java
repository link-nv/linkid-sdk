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
        this.entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

        this.testedInstance = new AllowedDeviceDAOBean();
        this.deviceDAO = new DeviceDAOBean();
        this.deviceClassDAO = new DeviceClassDAOBean();
        this.applicationDAO = new ApplicationDAOBean();
        this.applicationOwnerDAO = new ApplicationOwnerDAOBean();
        this.subjectDAO = new SubjectDAOBean();

        EJBTestUtils.inject(this.testedInstance, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.deviceDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.deviceClassDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.applicationDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.applicationOwnerDAO, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.subjectDAO, this.entityTestManager.getEntityManager());

        EJBTestUtils.init(this.deviceDAO);
        EJBTestUtils.init(this.deviceClassDAO);
        EJBTestUtils.init(this.applicationDAO);
        EJBTestUtils.init(this.applicationOwnerDAO);
        EJBTestUtils.init(this.subjectDAO);
        EJBTestUtils.init(this.testedInstance);

    }

    @Override
    protected void tearDown()
            throws Exception {

        this.entityTestManager.tearDown();
        super.tearDown();
    }

    public void testAllowedDevice() {

        this.subjectDAO.addSubject("testsubject");
        SubjectEntity subject = this.subjectDAO.findSubject("testsubject");

        this.applicationOwnerDAO.addApplicationOwner("testowner", subject);
        ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO.findApplicationOwner("testowner");
        ApplicationEntity application = this.applicationDAO.addApplication("testapp", null, applicationOwner, null, null, null, null);

        DeviceClassEntity deviceClass = this.deviceClassDAO.addDeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = this.deviceDAO.addDevice("testDevice", deviceClass, null, null, null, null, null, null, null, null, null,
                null, null, null);
        AllowedDeviceEntity allowedDevice = this.testedInstance.addAllowedDevice(application, device, 0);
        List<AllowedDeviceEntity> allowedDevices = this.testedInstance.listAllowedDevices(application);
        assertEquals(allowedDevice, allowedDevices.get(0));
    }

    public void testAllowedDeviceNoApplication() {

        DeviceClassEntity deviceClass = this.deviceClassDAO.addDeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = this.deviceDAO.addDevice("testDevice", deviceClass, null, null, null, null, null, null, null, null, null,
                null, null, null);
        try {
            this.testedInstance.addAllowedDevice(null, device, 0);
            this.entityTestManager.getEntityManager().flush();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testAllowedDeviceNoDevice() {

        this.subjectDAO.addSubject("testsubject");
        SubjectEntity subject = this.subjectDAO.findSubject("testsubject");

        this.applicationOwnerDAO.addApplicationOwner("testowner", subject);
        ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO.findApplicationOwner("testowner");
        ApplicationEntity application = this.applicationDAO.addApplication("testapp", null, applicationOwner, null, null, null, null);

        try {
            this.testedInstance.addAllowedDevice(application, null, 0);
            this.entityTestManager.getEntityManager().flush();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testDeleteDevices() {

        this.subjectDAO.addSubject("testsubject");
        SubjectEntity subject = this.subjectDAO.findSubject("testsubject");

        this.applicationOwnerDAO.addApplicationOwner("testowner", subject);
        ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO.findApplicationOwner("testowner");
        ApplicationEntity application = this.applicationDAO.addApplication("testapp", null, applicationOwner, null, null, null, null);

        DeviceClassEntity deviceClass = this.deviceClassDAO.addDeviceClass(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = this.deviceDAO.addDevice("testDevice", deviceClass, null, null, null, null, null, null, null, null, null,
                null, null, null);
        this.testedInstance.addAllowedDevice(application, device, 0);
        this.testedInstance.deleteAllowedDevices(application);
        List<AllowedDeviceEntity> allowedDevices = this.testedInstance.listAllowedDevices(application);
        assertEquals(0, allowedDevices.size());
    }

}
