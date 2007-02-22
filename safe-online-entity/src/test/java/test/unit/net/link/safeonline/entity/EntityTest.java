/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.entity;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import junit.framework.TestCase;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CachedOcspResponseEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;
import net.link.safeonline.entity.SubjectIdentifierPK;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.SubscriptionPK;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.entity.TrustPointPK;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

public class EntityTest extends TestCase {

	private EntityTestManager entityTestManager;

	private static final Log LOG = LogFactory.getLog(EntityTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(SubjectEntity.class,
				ApplicationEntity.class, SubscriptionEntity.class,
				HistoryEntity.class, ApplicationOwnerEntity.class,
				AttributeTypeEntity.class, AttributeEntity.class,
				TrustDomainEntity.class, TrustPointEntity.class,
				SubjectIdentifierEntity.class, CachedOcspResponseEntity.class);
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
		SubjectEntity subject = new SubjectEntity("test-login");

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
		SubjectEntity admin = new SubjectEntity("test-admin");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", admin);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", applicationOwner);

		// operate: add application
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(admin);
		entityManager.persist(applicationOwner);
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
		SubjectEntity subject = new SubjectEntity("test-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", subject);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", applicationOwner);
		SubscriptionEntity subscription = new SubscriptionEntity(
				SubscriptionOwnerType.SUBJECT, subject, application);

		// operate & verify
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subscription);
		try {
			entityManager.flush();
			fail();
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testAddSubscription() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", subject);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", applicationOwner);
		SubscriptionEntity subscription = new SubscriptionEntity(
				SubscriptionOwnerType.SUBJECT, subject, application);

		// operate: add subscription
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
		entityManager.persist(applicationOwner);
		entityManager.persist(application);
		entityManager.persist(subscription);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		SubscriptionEntity resultSubscription = entityManager.find(
				SubscriptionEntity.class, new SubscriptionPK(subject,
						application));
		assertNotNull(resultSubscription);
		assertEquals(subscription, resultSubscription);

		resultSubscription = entityManager.find(SubscriptionEntity.class,
				new SubscriptionPK(subject, application));

		Query countQuery = SubscriptionEntity.createQueryCountWhereApplication(
				entityManager, application);
		Long count = (Long) countQuery.getSingleResult();
		LOG.debug("count: " + count);
		assertEquals(1, (long) count);

		// operate: remove subscription
		entityManager.remove(resultSubscription);

		// verify
		assertNull(entityManager.find(SubscriptionEntity.class,
				new SubscriptionPK(subject, application)));
		assertNotNull(entityManager.find(SubjectEntity.class, "test-login"));
		assertNotNull(entityManager.find(ApplicationEntity.class,
				"test-application"));
	}

	public void testAddHistory() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login");
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

	public void testAddAttribute() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login");
		AttributeTypeEntity attributeType = new AttributeTypeEntity("password",
				"string", false, false);
		AttributeEntity attribute = new AttributeEntity("password",
				"test-login", "test-password");

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
		entityManager.persist(attributeType);
		entityManager.persist(attribute);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		AttributeEntity resultAttribute = entityManager.find(
				AttributeEntity.class,
				new AttributePK("password", "test-login"));
		assertNotNull(resultAttribute);
		assertEquals("test-password", resultAttribute.getStringValue());
		assertEquals(subject, resultAttribute.getSubject());
		assertEquals(attributeType, resultAttribute.getAttributeType());
	}

	public void testTrustDomain() throws Exception {
		// setup
		String trustDomainName = "test-trust-domain-" + getName();
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(trustDomain);
		LOG.debug("trust domain id: " + trustDomain.getId());

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		Query query = TrustDomainEntity.createQueryWhereName(entityManager,
				trustDomainName);
		TrustDomainEntity resultTrustDomain = (TrustDomainEntity) query
				.getSingleResult();
		LOG.debug("result trust domain: " + resultTrustDomain);
		assertEquals(trustDomain, resultTrustDomain);

		// operate & verify: unique constraint
		TrustDomainEntity secondTrustDomain = new TrustDomainEntity(
				trustDomainName, true);
		entityManager = this.entityTestManager.refreshEntityManager();
		entityManager.persist(secondTrustDomain);
		try {
			entityManager.flush();
			fail();
		} catch (EntityExistsException e) {
			// expected
		}
	}

	@SuppressWarnings("unchecked")
	public void testTrustPoint() throws Exception {
		// setup
		String trustDomainName = "test-trust-domain-" + getName();
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		String dn = "CN=Test";
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, dn);
		String keyId = new String(Hex
				.encodeHex(new SubjectKeyIdentifierStructure(certificate
						.getExtensionValue(X509Extensions.SubjectKeyIdentifier
								.getId())).getKeyIdentifier()));

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(trustDomain);
		TrustPointEntity trustPoint = new TrustPointEntity(trustDomain,
				certificate);
		entityManager.persist(trustPoint);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		TrustPointEntity resultTrustPoint = entityManager.find(
				TrustPointEntity.class,
				new TrustPointPK(trustDomain, dn, keyId));
		assertNotNull(resultTrustPoint);
		assertEquals(trustPoint, resultTrustPoint);

		// operate: query test
		LOG.debug("trust domain Id: " + trustDomain.getId());
		Query query = TrustPointEntity.createQueryWhereDomain(entityManager,
				trustDomain);
		List<TrustPointEntity> resultTrustPoints = query.getResultList();

		// verify
		assertEquals(1, resultTrustPoints.size());
		assertEquals(resultTrustPoint, resultTrustPoints.get(0));
	}

	public void testSubjectIdentifier() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-subject");
		SubjectIdentifierEntity subjectIdentifier = new SubjectIdentifierEntity(
				"test-domain", "test-identifier", subject);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
		entityManager.persist(subjectIdentifier);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		SubjectIdentifierPK pk = new SubjectIdentifierPK("test-domain",
				"test-identifier");
		SubjectIdentifierEntity resultSubjectIdentifier = entityManager.find(
				SubjectIdentifierEntity.class, pk);
		assertNotNull(resultSubjectIdentifier);
		assertEquals(subject, resultSubjectIdentifier.getSubject());
	}

	public void testTrustPointEntityHashCode() throws Exception {
		// setup
		TrustDomainEntity trustDomain = new TrustDomainEntity(
				"trust-domain-name", true);
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		TrustPointEntity trustPoint1 = new TrustPointEntity(trustDomain,
				certificate);
		TrustPointEntity trustPoint2 = new TrustPointEntity(trustDomain,
				certificate);

		// operate & verify
		assertEquals(trustPoint1, trustPoint2);
		assertEquals(trustPoint1.getPk().hashCode(), trustPoint2.getPk()
				.hashCode());
		assertEquals(trustPoint1.hashCode(), trustPoint2.hashCode());
	}

	public void testCachedOcspResponse() throws Exception {
		// setup
		String trustDomainName = "test-trust-domain-" + getName();
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		String key = "1234";
		CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(
				key, true, trustDomain);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(trustDomain);
		LOG.debug("trust domain id: " + trustDomain.getId());
		entityManager.persist(cachedOcspResponse);
		LOG.debug("ocsp response id: " + cachedOcspResponse.getId());

		// verify store and find
		entityManager = this.entityTestManager.refreshEntityManager();
		Query query = CachedOcspResponseEntity.createQueryWhereKey(
				entityManager, key);
		CachedOcspResponseEntity resultCachedOcspResponse = (CachedOcspResponseEntity) query
				.getResultList().get(0);
		assertEquals(cachedOcspResponse, resultCachedOcspResponse);

		// operate
		entityManager = this.entityTestManager.refreshEntityManager();
		entityManager.persist(new CachedOcspResponseEntity(key, true,
				trustDomain));
		// verify unique constraint
		try {
			entityManager.flush();
			fail();
		} catch (EntityExistsException e) {
			// expected
		}

		// operate + verify cache purge
		entityManager = this.entityTestManager.refreshEntityManager();
		query = CachedOcspResponseEntity.createQueryDeleteAll(entityManager);
		int result = query.executeUpdate();
		assertEquals(result, 1);
	}

}
