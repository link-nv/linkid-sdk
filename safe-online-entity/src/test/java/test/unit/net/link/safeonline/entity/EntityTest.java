package test.unit.net.link.safeonline.entity;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import junit.framework.TestCase;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EntityTest extends TestCase {

	private EntityTestManager entityTestManager;

	private static final Log LOG = LogFactory.getLog(EntityTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(EntityEntity.class,
				ApplicationEntity.class, SubscriptionEntity.class);
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testAnnotationCorrectness() throws Exception {
		// empty
	}

	public void testAddRemoveEntity() throws Exception {
		// setup
		EntityEntity entity = new EntityEntity("test-login",
				"test-password", null);

		// operate: add entity
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(entity);

		// verify: locate the added entity
		entityManager = this.entityTestManager.refreshEntityManager();
		EntityEntity resultEntity = entityManager.find(EntityEntity.class,
				"test-login");
		assertNotNull(resultEntity);
		assertEquals(entity, resultEntity);
		LOG.debug("result entity: " + resultEntity);

		// operate: remove entity
		entityManager.remove(resultEntity);

		// verify
		assertNull(entityManager.find(EntityEntity.class, "test-login"));
	}

	public void testAddRemoveApplication() throws Exception {
		// setup
		ApplicationEntity application = new ApplicationEntity(
				"test-application");

		// operate: add application
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(application);

		// verify: locate the added application
		entityManager = this.entityTestManager.refreshEntityManager();
		ApplicationEntity resultApplication = entityManager.find(
				ApplicationEntity.class, "test-application");
		assertNotNull(resultApplication);
		assertEquals(application, resultApplication);
		LOG.debug("result application:" + resultApplication);

		// operate: remove application
		entityManager.remove(resultApplication);

		// verify
		assertNull(entityManager.find(ApplicationEntity.class,
				"test-application"));
	}

	public void testAddSubscriptionRequiresEntityAndApplication()
			throws Exception {
		// setup
		SubscriptionEntity subscription = new SubscriptionEntity();

		// operate & verify: add subscription without entity or application
		// fails
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		try {
			EntityTransaction entityTransaction = entityManager
					.getTransaction();
			assertFalse(entityTransaction.getRollbackOnly());
			entityManager.persist(subscription);
			assertTrue(entityTransaction.getRollbackOnly());
			entityTransaction.rollback();
			entityTransaction.begin();
			fail();
		} catch (PersistenceException e) {
			// expected
			LOG.debug("expected exception: " + e.getMessage());
		}
	}

	public void testAddSubscriptionRequiresExistingEntityAndApplication()
			throws Exception {
		// setup
		EntityEntity entity = new EntityEntity("test-login",
				"test-password", null);
		ApplicationEntity application = new ApplicationEntity(
				"test-application");
		SubscriptionEntity subscription = new SubscriptionEntity(entity,
				application);

		// operate & verify: add subscription requires existing entity and
		// application
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		try {
			entityManager.persist(subscription);
			fail();
		} catch (PersistenceException e) {
			// expected
		}
	}

	public void testAddSubscription() throws Exception {
		// setup
		EntityEntity entity = new EntityEntity("test-login",
				"test-password", null);
		ApplicationEntity application = new ApplicationEntity(
				"test-application");
		SubscriptionEntity subscription = new SubscriptionEntity(entity,
				application);

		// operate: add subscription
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(entity);
		entityManager.persist(application);
		entityManager.persist(subscription);

		// verify
		long resultId = subscription.getId();
		LOG.debug("subscription id: " + resultId);
		entityManager = this.entityTestManager.refreshEntityManager();
		SubscriptionEntity resultSubscription = entityManager.find(
				SubscriptionEntity.class, resultId);
		assertNotNull(resultSubscription);
		assertEquals(subscription, resultSubscription);

		Query query = SubscriptionEntity.createQueryWhereEntityAndApplication(
				entityManager, entity, application);
		resultSubscription = (SubscriptionEntity) query.getSingleResult();

		// operate: remove subscription
		entityManager.remove(resultSubscription);

		// verify
		assertNull(entityManager.find(SubscriptionEntity.class, resultId));
		assertNotNull(entityManager.find(EntityEntity.class, "test-login"));
		assertNotNull(entityManager.find(ApplicationEntity.class,
				"test-application"));
	}
}
