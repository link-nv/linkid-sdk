package test.unit.net.link.safeonline.dao.bean;

import java.util.List;

import net.link.safeonline.dao.bean.DeviceDAOBean;
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
						AttributeTypeDescriptionEntity.class);

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
