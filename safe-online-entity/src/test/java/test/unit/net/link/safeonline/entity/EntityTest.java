package test.unit.net.link.safeonline.entity;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import junit.framework.TestCase;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
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
		this.entityTestManager.setUp(SubjectEntity.class,
				ApplicationEntity.class, SubscriptionEntity.class,
				HistoryEntity.class);
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
		SubjectEntity subject = new SubjectEntity("test-login",
				"test-password", null);

		// operate: add entity
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);

		// verify: locate the added entity
		entityManager = this.entityTestManager.refreshEntityManager();
		SubjectEntity resultSubject = entityManager.find(SubjectEntity.class,
				"test-login");
		assertNotNull(resultSubject);
		assertEquals(subject, resultSubject);
		LOG.debug("result entity: " + resultSubject);

		// operate: remove entity
		entityManager.remove(resultSubject);

		// verify
		assertNull(entityManager.find(SubjectEntity.class, "test-login"));
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
		SubjectEntity subject = new SubjectEntity("test-login",
				"test-password", null);
		ApplicationEntity application = new ApplicationEntity(
				"test-application");
		SubscriptionEntity subscription = new SubscriptionEntity(
				SubscriptionOwnerType.SUBJECT, subject, application);

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
		SubjectEntity subject = new SubjectEntity("test-login",
				"test-password", null);
		ApplicationEntity application = new ApplicationEntity(
				"test-application");
		SubscriptionEntity subscription = new SubscriptionEntity(
				SubscriptionOwnerType.SUBJECT, subject, application);

		// operate: add subscription
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
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
				entityManager, subject, application);
		resultSubscription = (SubscriptionEntity) query.getSingleResult();

		// operate: remove subscription
		entityManager.remove(resultSubscription);

		// verify
		assertNull(entityManager.find(SubscriptionEntity.class, resultId));
		assertNotNull(entityManager.find(SubjectEntity.class, "test-login"));
		assertNotNull(entityManager.find(ApplicationEntity.class,
				"test-application"));
	}

	public void testAddHistory() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login",
				"test-password", null);
		Date when = new Date();
		HistoryEntity history = new HistoryEntity(when, subject, "test-event");

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
		entityManager.persist(history);

		// verify
		LOG.debug("history id: " + history.getId());
		entityManager = this.entityTestManager.refreshEntityManager();

		HistoryEntity resultHistory = entityManager.find(HistoryEntity.class,
				history.getId());
		assertNotNull(resultHistory);
	}
}
