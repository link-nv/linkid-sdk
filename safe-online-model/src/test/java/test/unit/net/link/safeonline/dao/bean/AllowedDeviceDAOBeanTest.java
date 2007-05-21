package test.unit.net.link.safeonline.dao.bean;

import java.util.List;

import net.link.safeonline.dao.bean.AllowedDeviceDAOBean;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributePK;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.SubscriptionPK;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class AllowedDeviceDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private AllowedDeviceDAOBean testedInstance;

	private ApplicationDAOBean applicationDAO;

	private DeviceDAOBean deviceDAO;

	private ApplicationOwnerDAOBean applicationOwnerDAO;

	private SubjectDAOBean subjectDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager
				.setUp(DeviceEntity.class, ApplicationEntity.class,
						ApplicationIdentityAttributeEntity.class,
						ApplicationIdentityAttributePK.class,
						ApplicationIdentityEntity.class,
						ApplicationIdentityPK.class,
						ApplicationOwnerEntity.class, SubjectEntity.class,
						AttributeEntity.class, AttributePK.class,
						AttributeTypeEntity.class,
						AttributeTypeDescriptionEntity.class,
						AttributeTypeDescriptionPK.class,
						SubscriptionEntity.class, SubscriptionOwnerType.class,
						SubscriptionPK.class, AttributeTypeEntity.class,
						AttributeTypeDescriptionEntity.class,
						AllowedDeviceEntity.class);

		this.testedInstance = new AllowedDeviceDAOBean();
		this.deviceDAO = new DeviceDAOBean();
		this.applicationDAO = new ApplicationDAOBean();
		this.applicationOwnerDAO = new ApplicationOwnerDAOBean();
		this.subjectDAO = new SubjectDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.deviceDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.applicationDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.applicationOwnerDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.subjectDAO, this.entityTestManager
				.getEntityManager());

	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testAllowedDevice() {
		this.subjectDAO.addSubject("testsubject");
		SubjectEntity subject = this.subjectDAO.findSubject("testsubject");

		this.applicationOwnerDAO.addApplicationOwner("testowner", subject);
		ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
				.findApplicationOwner("testowner");
		ApplicationEntity application = this.applicationDAO.addApplication(
				"testapp", applicationOwner, null, null);

		DeviceEntity device = this.deviceDAO.addDevice("testdevice");
		AllowedDeviceEntity allowedDevice = this.testedInstance
				.addAllowedDevice(application, device, 0);
		List<AllowedDeviceEntity> allowedDevices = this.testedInstance
				.listAllowedDevices(application);
		assertEquals(allowedDevice, allowedDevices.get(0));
	}

	public void testAllowedDeviceNoApplication() {
		DeviceEntity device = this.deviceDAO.addDevice("testdevice");
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
		ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
				.findApplicationOwner("testowner");
		ApplicationEntity application = this.applicationDAO.addApplication(
				"testapp", applicationOwner, null, null);

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
		ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
				.findApplicationOwner("testowner");
		ApplicationEntity application = this.applicationDAO.addApplication(
				"testapp", applicationOwner, null, null);

		DeviceEntity device = this.deviceDAO.addDevice("testdevice");
		this.testedInstance.addAllowedDevice(application, device, 0);
		this.testedInstance.deleteAllowedDevices(application);
		List<AllowedDeviceEntity> allowedDevices = this.testedInstance
				.listAllowedDevices(application);
		assertEquals(0, allowedDevices.size());
	}

}
