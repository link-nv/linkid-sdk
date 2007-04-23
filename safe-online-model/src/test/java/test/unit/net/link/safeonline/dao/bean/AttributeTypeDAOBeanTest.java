package test.unit.net.link.safeonline.dao.bean;

import java.util.Map;
import java.util.UUID;

import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
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
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.SubscriptionPK;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class AttributeTypeDAOBeanTest extends TestCase {
	private EntityTestManager entityTestManager;

	private ApplicationDAOBean applicationDAO;

	private ApplicationOwnerDAOBean applicationOwnerDAO;

	private SubjectDAOBean subjectDAO;

	private AttributeDAOBean attributeDAO;

	private ApplicationIdentityDAOBean applicationIdentityDAO;

	private SubscriptionDAOBean subscriptionDAO;

	private AttributeTypeDAOBean testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(ApplicationEntity.class,
				ApplicationIdentityAttributeEntity.class,
				ApplicationIdentityAttributePK.class,
				ApplicationIdentityEntity.class, ApplicationIdentityPK.class,
				ApplicationOwnerEntity.class, SubjectEntity.class,
				AttributeEntity.class, AttributePK.class,
				AttributeTypeEntity.class,
				AttributeTypeDescriptionEntity.class,
				AttributeTypeDescriptionPK.class, SubscriptionEntity.class,
				SubscriptionOwnerType.class, SubscriptionPK.class);
		// StatisticDataPointEntity.class,
		this.testedInstance = new AttributeTypeDAOBean();
		this.applicationDAO = new ApplicationDAOBean();
		this.applicationOwnerDAO = new ApplicationOwnerDAOBean();
		this.subjectDAO = new SubjectDAOBean();
		this.attributeDAO = new AttributeDAOBean();
		this.applicationIdentityDAO = new ApplicationIdentityDAOBean();
		this.subscriptionDAO = new SubscriptionDAOBean();

		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.applicationDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.applicationOwnerDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.subjectDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.attributeDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.applicationIdentityDAO, this.entityTestManager
				.getEntityManager());
		EJBTestUtils.inject(this.subscriptionDAO, this.entityTestManager
				.getEntityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testDataMining() throws Exception {
		// setup users
		String user1name = UUID.randomUUID().toString();
		String user2name = UUID.randomUUID().toString();
		SubjectEntity user1 = this.subjectDAO.addSubject(user1name);
		SubjectEntity user2 = this.subjectDAO.addSubject(user2name);

		// setup attribute types
		String attributeName = UUID.randomUUID().toString();
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, "", false, false);
		this.testedInstance.addAttributeType(attributeType);

		// setup attribute
		String attributeValue = UUID.randomUUID().toString();
		this.attributeDAO.addAttribute(attributeType, user1, attributeValue);
		this.attributeDAO.addAttribute(attributeType, user2, attributeValue);

		// setup application owner
		String ownerName = UUID.randomUUID().toString();
		this.applicationOwnerDAO.addApplicationOwner(ownerName, user2);
		ApplicationOwnerEntity owner = this.applicationOwnerDAO
				.findApplicationOwner(ownerName);

		// setup application
		String applicationName = UUID.randomUUID().toString();
		ApplicationEntity application = this.applicationDAO.addApplication(
				applicationName, owner, null, null);

		// setup application identity
		this.applicationIdentityDAO.addApplicationIdentity(application, 1);
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, 1);
		this.applicationIdentityDAO.addApplicationIdentityAttribute(
				applicationIdentity, attributeType, true, false);

		// setup subscription
		this.subscriptionDAO.addSubscription(SubscriptionOwnerType.SUBJECT,
				user1, application);
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(user1, application);
		subscription.setConfirmedIdentityVersion(Long.valueOf(1));

		// operate
		Map<String, Long> result = this.testedInstance.categorize(application,
				attributeType);

		// verify
		for (String value : result.keySet()) {
			if (value.equals(attributeValue)) {
				assertEquals(result.get(value), Long.valueOf(1));
			}
		}

	}
}
