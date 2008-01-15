/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributePK;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeProviderPK;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;
import net.link.safeonline.entity.SubjectIdentifierPK;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.SubscriptionPK;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.entity.UsageAgreementTextPK;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.OperationStateType;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.entity.pkix.TrustPointPK;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.jpa.QueryObjectFactory;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EntityTest {

	private EntityTestManager entityTestManager;

	private static final Log LOG = LogFactory.getLog(EntityTest.class);

	@Before
	public void setUp() {
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		try {
			this.entityTestManager.setUp(SubjectEntity.class,
					ApplicationEntity.class, SubscriptionEntity.class,
					HistoryEntity.class, ApplicationOwnerEntity.class,
					AttributeTypeEntity.class, AttributeEntity.class,
					TrustDomainEntity.class, TrustPointEntity.class,
					SubjectIdentifierEntity.class,
					CachedOcspResponseEntity.class, TaskEntity.class,
					SchedulingEntity.class, TaskHistoryEntity.class,
					ConfigItemEntity.class, ConfigGroupEntity.class,
					StatisticEntity.class, StatisticDataPointEntity.class,
					ApplicationIdentityEntity.class,
					ApplicationIdentityAttributeEntity.class,
					AttributeTypeDescriptionEntity.class,
					AttributeProviderEntity.class, AuditContextEntity.class,
					AuditAuditEntity.class, HelpdeskContextEntity.class,
					CompoundedAttributeTypeMemberEntity.class,
					AccessAuditEntity.class, UsageAgreementEntity.class,
					UsageAgreementTextEntity.class,
					GlobalUsageAgreementEntity.class);
		} catch (Exception e) {
			LOG.fatal("JPA annotations incorrect: " + e.getMessage(), e);
			throw new RuntimeException("JPA annotations incorrect: "
					+ e.getMessage(), e);
		}
	}

	@After
	public void tearDown() throws Exception {
		this.entityTestManager.tearDown();
	}

	@Test
	public void annotationCorrectness() throws Exception {
		LOG.debug("annotation correctness test");
		assertNotNull("JPA annotations incorrect?", this.entityTestManager
				.getEntityManager());
	}

	@Test
	public void addRemoveSubject() throws Exception {
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

	@Test
	public void testAddRemoveApplication() throws Exception {
		// setup
		SubjectEntity admin = new SubjectEntity("test-admin");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", admin);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", null, applicationOwner, null, null, null,
				null, null);

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

	@Test
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

	@Test
	public void testAddSubscriptionRequiresExistingEntityAndApplication()
			throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", subject);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", null, applicationOwner, null, null, null,
				null, null);
		SubscriptionEntity subscription = new SubscriptionEntity(
				SubscriptionOwnerType.SUBJECT, subject, "0", application);

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

	@Test
	public void testAddSubscription() throws Exception {
		// setup
		String userApplicationId = UUID.randomUUID().toString();
		SubjectEntity subject = new SubjectEntity("test-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", subject);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", null, applicationOwner, null, null, null,
				null, null);
		SubscriptionEntity subscription = new SubscriptionEntity(
				SubscriptionOwnerType.SUBJECT, subject, userApplicationId,
				application);

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

		SubscriptionEntity.QueryInterface subscriptionQueryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						SubscriptionEntity.QueryInterface.class);
		Long count = subscriptionQueryObject
				.getNumberOfSubscriptions(application);
		LOG.debug("count: " + count);
		assertEquals(1, (long) count);

		resultSubscription = subscriptionQueryObject
				.getSubscription(userApplicationId);
		assertNotNull(resultSubscription);

		// operate: remove subscription
		entityManager.remove(resultSubscription);

		// verify
		assertNull(entityManager.find(SubscriptionEntity.class,
				new SubscriptionPK(subject, application)));
		assertNotNull(entityManager.find(SubjectEntity.class, "test-login"));
		assertNotNull(entityManager.find(ApplicationEntity.class,
				"test-application"));
	}

	@Test
	public void testAddGlobalUsageAgreement() throws Exception {
		// setup
		GlobalUsageAgreementEntity globalUsageAgreementv1 = new GlobalUsageAgreementEntity(
				1L);
		GlobalUsageAgreementEntity globalUsageAgreementv2 = new GlobalUsageAgreementEntity(
				2L);
		UsageAgreementTextEntity usageAgreementTextv1En = new UsageAgreementTextEntity(
				globalUsageAgreementv1, "This is a usage agreement.",
				Locale.ENGLISH.getLanguage());
		UsageAgreementTextEntity usageAgreementTextv1Fr = new UsageAgreementTextEntity(
				globalUsageAgreementv1, "Dit is een gebruikers overeenkomst.",
				"nl");

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(globalUsageAgreementv1);
		entityManager.persist(globalUsageAgreementv2);
		entityManager.persist(usageAgreementTextv1En);
		entityManager.persist(usageAgreementTextv1Fr);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		GlobalUsageAgreementEntity resultAgreementv1 = entityManager.find(
				GlobalUsageAgreementEntity.class, 1L);
		assertNotNull(resultAgreementv1);
		GlobalUsageAgreementEntity resultAgreementv2 = entityManager.find(
				GlobalUsageAgreementEntity.class, 2L);
		assertNotNull(resultAgreementv2);
		assertFalse(resultAgreementv1.equals(resultAgreementv2));

		UsageAgreementTextEntity resultAgreementTextv1En = entityManager.find(
				UsageAgreementTextEntity.class, new UsageAgreementTextPK(
						GlobalUsageAgreementEntity.GLOBAL_USAGE_AGREEMENT,
						globalUsageAgreementv1.getUsageAgreementVersion(),
						Locale.ENGLISH.getLanguage()));
		assertNotNull(resultAgreementTextv1En);
		UsageAgreementTextEntity resultAgreementTextv1Nl = entityManager
				.find(
						UsageAgreementTextEntity.class,
						new UsageAgreementTextPK(
								GlobalUsageAgreementEntity.GLOBAL_USAGE_AGREEMENT,
								globalUsageAgreementv1
										.getUsageAgreementVersion(), "nl"));
		assertNotNull(resultAgreementTextv1Nl);

	}

	@Test
	public void testAddUsageAgreement() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", subject);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", null, applicationOwner, null, null, null,
				null, null);
		UsageAgreementEntity usageAgreementv1 = new UsageAgreementEntity(
				application, 1L);
		UsageAgreementEntity usageAgreementv2 = new UsageAgreementEntity(
				application, 2L);
		UsageAgreementTextEntity usageAgreementTextv1En = new UsageAgreementTextEntity(
				usageAgreementv1, "This is a usage agreement.", Locale.ENGLISH
						.getLanguage());
		UsageAgreementTextEntity usageAgreementTextv1Fr = new UsageAgreementTextEntity(
				usageAgreementv1, "Dit is een gebruikers overeenkomst.", "nl");

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
		entityManager.persist(applicationOwner);
		entityManager.persist(application);
		entityManager.persist(usageAgreementv1);
		entityManager.persist(usageAgreementv2);
		entityManager.persist(usageAgreementTextv1En);
		entityManager.persist(usageAgreementTextv1Fr);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		UsageAgreementEntity resultAgreementv1 = entityManager.find(
				UsageAgreementEntity.class, new UsageAgreementPK(application
						.getName(), 1L));
		assertNotNull(resultAgreementv1);
		UsageAgreementEntity resultAgreementv2 = entityManager.find(
				UsageAgreementEntity.class, new UsageAgreementPK(application
						.getName(), 2L));
		assertNotNull(resultAgreementv2);
		assertFalse(resultAgreementv1.equals(resultAgreementv2));

		UsageAgreementTextEntity resultAgreementTextv1En = entityManager.find(
				UsageAgreementTextEntity.class, new UsageAgreementTextPK(
						usageAgreementv1.getApplication().getName(),
						usageAgreementv1.getUsageAgreementVersion(),
						Locale.ENGLISH.getLanguage()));
		assertNotNull(resultAgreementTextv1En);
		UsageAgreementTextEntity resultAgreementTextv1Nl = entityManager.find(
				UsageAgreementTextEntity.class, new UsageAgreementTextPK(
						usageAgreementv1.getApplication().getName(),
						usageAgreementv1.getUsageAgreementVersion(), "nl"));
		assertNotNull(resultAgreementTextv1Nl);
	}

	@Test
	public void testAddHistory() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login");
		Date when = new Date();
		HistoryEntity history = new HistoryEntity(when, subject,
				HistoryEventType.LOGIN_SUCCESS, "test-app", "username-password");

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

	@Test
	public void testAddAttribute() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-login");
		AttributeTypeEntity attributeType = new AttributeTypeEntity("password",
				DatatypeType.STRING, false, false);
		AttributeEntity attribute = new AttributeEntity(attributeType, subject,
				"test-password");

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

	@Test
	public void testMultiValuedAttribute() throws Exception {
		// setup
		String login = "test-login";
		SubjectEntity subject = new SubjectEntity(login);
		String attributeName = "attribute-name";
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, DatatypeType.STRING, false, false);
		AttributeEntity attribute1 = new AttributeEntity(attributeType,
				subject, "value1");
		AttributeEntity attribute2 = new AttributeEntity(attributeType,
				subject, 1);
		attribute2.setStringValue("value2");

		AttributePK pk1 = new AttributePK(attributeName, login, 0);
		AttributePK pk2 = new AttributePK(attributeName, login, 0);
		assertEquals(pk1, pk2);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
		entityManager.persist(attributeType);
		entityManager.persist(attribute1);
		entityManager.persist(attribute2);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		AttributeEntity resultAttribute = entityManager.find(
				AttributeEntity.class, new AttributePK(attributeName, login));
		assertNotNull(resultAttribute);
		assertEquals("value1", resultAttribute.getStringValue());
		assertEquals(subject, resultAttribute.getSubject());
		assertEquals(attributeType, resultAttribute.getAttributeType());
		LOG.debug("attribute1 PK: " + attribute1.getPk());
		LOG.debug("result PK: " + resultAttribute.getPk());
		assertEquals(resultAttribute.getPk(), attribute1.getPk());
		assertEquals(resultAttribute, attribute1);

		// operate: remove attribute1
		entityManager.remove(resultAttribute);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		resultAttribute = entityManager.find(AttributeEntity.class,
				new AttributePK(attributeName, login));
		assertNull(resultAttribute);

		resultAttribute = entityManager.find(AttributeEntity.class,
				new AttributePK(attributeType, subject, 1));
		LOG.debug("result attribute: " + resultAttribute);
		assertEquals(attribute2, resultAttribute);
	}

	@Test
	public void testTrustDomain() throws Exception {
		// setup
		String trustDomainName = "test-trust-domain-"
				+ UUID.randomUUID().toString();
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(trustDomain);
		LOG.debug("trust domain id: " + trustDomain.getId());

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		TrustDomainEntity.QueryInterface trustDomainQueryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						TrustDomainEntity.QueryInterface.class);
		TrustDomainEntity resultTrustDomain = trustDomainQueryObject
				.findTrustDomain(trustDomainName);
		LOG.debug("result trust domain: " + resultTrustDomain);
		assertEquals(trustDomain, resultTrustDomain);

		// operate & verify: unique constraint
		TrustDomainEntity secondTrustDomain = new TrustDomainEntity(
				trustDomainName, true);
		entityManager = this.entityTestManager.refreshEntityManager();
		try {
			entityManager.persist(secondTrustDomain);
			entityManager.flush();
			fail();
		} catch (EntityExistsException e) {
			// expected
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTrustPoint() throws Exception {
		// setup
		String trustDomainName = "test-trust-domain-"
				+ UUID.randomUUID().toString();
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
		TrustPointEntity.QueryInterface queryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						TrustPointEntity.QueryInterface.class);
		List<TrustPointEntity> resultTrustPoints = queryObject
				.listTrustPoints(trustDomain);

		// verify
		assertEquals(1, resultTrustPoints.size());
		assertEquals(resultTrustPoint, resultTrustPoints.get(0));
	}

	@Test
	public void testTrustPointWithEmptyKeyId() throws Exception {
		// setup
		String trustDomainName = "test-trust-domain-"
				+ UUID.randomUUID().toString();
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		String dn = "CN=Test";
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, dn);
		String keyId = dn;

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(trustDomain);
		TrustPointEntity trustPoint = new TrustPointEntity(trustDomain,
				certificate);
		trustPoint.getPk().setKeyId(keyId);
		entityManager.persist(trustPoint);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		TrustPointEntity resultTrustPoint = entityManager.find(
				TrustPointEntity.class, new TrustPointPK(trustDomain, dn, dn));
		assertNotNull(resultTrustPoint);
		assertEquals(trustPoint, resultTrustPoint);

		// operate: query test
		LOG.debug("trust domain Id: " + trustDomain.getId());
		TrustPointEntity.QueryInterface queryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						TrustPointEntity.QueryInterface.class);
		List<TrustPointEntity> resultTrustPoints = queryObject
				.listTrustPoints(trustDomain);

		// verify
		assertEquals(1, resultTrustPoints.size());
		assertEquals(resultTrustPoint, resultTrustPoints.get(0));
	}

	@Test
	public void testSubjectIdentifier() throws Exception {
		// setup
		SubjectEntity subject = new SubjectEntity("test-subject");
		String domain = "test-domain";
		String identifier = "test-identifier";
		SubjectIdentifierEntity subjectIdentifier = new SubjectIdentifierEntity(
				domain, identifier, subject);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(subject);
		entityManager.persist(subjectIdentifier);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		SubjectIdentifierPK pk = new SubjectIdentifierPK(domain, identifier);
		SubjectIdentifierEntity resultSubjectIdentifier = entityManager.find(
				SubjectIdentifierEntity.class, pk);
		assertNotNull(resultSubjectIdentifier);
		assertEquals(subject, resultSubjectIdentifier.getSubject());

		/*
		 * Check that the delete query is not deleting our new identifier.
		 */
		SubjectIdentifierEntity.QueryInterface subjectIdentifierQueryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						SubjectIdentifierEntity.QueryInterface.class);
		subjectIdentifierQueryObject.deleteWhereOtherIdentifiers(domain,
				identifier, subject);
		resultSubjectIdentifier = entityManager.find(
				SubjectIdentifierEntity.class, pk);
		assertNotNull(resultSubjectIdentifier);

		/*
		 * Check whether the delete query can delete other identifiers.
		 */
		String anotherIdentifier = identifier + "-something-else";
		subjectIdentifierQueryObject.deleteWhereOtherIdentifiers(domain,
				anotherIdentifier, subject);

		/*
		 * We have to clear the entity cache before being able to make useful
		 * queries.
		 */
		entityManager.clear();

		resultSubjectIdentifier = entityManager.find(
				SubjectIdentifierEntity.class, pk);
		assertNull(resultSubjectIdentifier);
	}

	@Test
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

	@Test
	public void testCachedOcspResponse() throws Exception {
		// setup
		String trustDomainName = "test-trust-domain-"
				+ UUID.randomUUID().toString();
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
		CachedOcspResponseEntity.QueryInterface queryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						CachedOcspResponseEntity.QueryInterface.class);
		CachedOcspResponseEntity resultCachedOcspResponse = queryObject
				.findCachedOcspResponse(key);
		assertEquals(cachedOcspResponse, resultCachedOcspResponse);

		// operate
		entityManager = this.entityTestManager.refreshEntityManager();
		// verify unique constraint
		try {
			entityManager.persist(new CachedOcspResponseEntity(key, true,
					trustDomain));
			entityManager.flush();
			fail();
		} catch (EntityExistsException e) {
			// expected
		}

		// operate + verify cache purge
		entityManager = this.entityTestManager.refreshEntityManager();
		queryObject = QueryObjectFactory.createQueryObject(entityManager,
				CachedOcspResponseEntity.QueryInterface.class);
		int result = queryObject.deleteAll();
		assertEquals(result, 1);
	}

	@Test
	public void testTaskScheduling() {
		// setup
		SchedulingEntity schedulingEntity = new SchedulingEntity("default",
				"0 0 3 * * ?", null);
		TaskEntity taskEntity = new TaskEntity("id", "name", schedulingEntity);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(schedulingEntity);
		entityManager.persist(taskEntity);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		TaskEntity resultTask = entityManager.find(TaskEntity.class, "id");
		SchedulingEntity resultScheduling = resultTask.getScheduling();
		assertNotNull(resultTask);
		assertNotNull(resultScheduling);
		assertEquals(taskEntity, resultTask);
		assertEquals(schedulingEntity, resultScheduling);

		entityManager = this.entityTestManager.refreshEntityManager();
		resultScheduling = entityManager
				.find(SchedulingEntity.class, "default");
		Collection<?> collection = resultScheduling.getTasks();
		LOG.debug("schedulings returned: " + collection.size());
		resultTask = (TaskEntity) resultScheduling.getTasks().toArray()[0];
		assertNotNull(resultTask);
		assertNotNull(resultScheduling);
		assertEquals(taskEntity, resultTask);
		assertEquals(schedulingEntity, resultScheduling);
	}

	@Test
	public void testAddRemoveTaskScheduling() {
		// setup
		SchedulingEntity schedulingEntity = new SchedulingEntity("default",
				"0 0 3 * * ?", null);
		TaskEntity taskEntity = new TaskEntity("id", "name", schedulingEntity);
		TaskEntity taskEntity2 = new TaskEntity("id2", "name2",
				schedulingEntity);

		// operate + verify
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(schedulingEntity);
		entityManager.persist(taskEntity);

		entityManager.remove(taskEntity2);
		entityManager.flush();

		entityManager.remove(schedulingEntity);
		try {
			entityManager.flush();
			fail();
		} catch (Exception e) {
			// empty
		}
		taskEntity.setScheduling(null);
		entityManager.flush();
		entityManager.remove(taskEntity);
	}

	@Test
	public void testAddRemoveTaskHistory() {
		// setup
		TaskEntity taskEntity = new TaskEntity("id", "name", null);
		long time = System.currentTimeMillis();
		TaskHistoryEntity history = new TaskHistoryEntity(taskEntity,
				"message", true, new Date(time), new Date(time + 1000));
		taskEntity.addTaskHistoryEntity(history);
		TaskHistoryEntity history2 = new TaskHistoryEntity(taskEntity,
				"message", true, new Date(time), new Date(time + 1000));
		taskEntity.addTaskHistoryEntity(history2);

		// operate + verify
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(taskEntity);
		entityManager.persist(history);
		entityManager.persist(history2);

		// try to remove a history entity
		entityManager.remove(history);
		entityManager.find(TaskEntity.class, "id").getTaskHistory().remove(
				history);
		entityManager.flush();

		// can the history entity be found?
		TaskEntity taskEntity2 = entityManager.find(TaskEntity.class, "id");
		assertEquals(taskEntity2.getTaskHistory().get(0), history2);

		// try to remove the task without cleaning all history entities
		entityManager.remove(taskEntity);
		try {
			entityManager.flush();
			fail();
		} catch (Exception e) {
			// empty
		}

		// clean the history entities and try to remove the task
		entityManager.remove(history2);
		entityManager.flush();
	}

	@Test
	public void testTaskHistoryClearing() {
		// setup
		TaskEntity taskEntity = new TaskEntity("id", "name", null);
		long time = System.currentTimeMillis();
		TaskHistoryEntity history = new TaskHistoryEntity(taskEntity,
				"message", true, new Date(time), new Date(time + 1000));
		taskEntity.addTaskHistoryEntity(history);

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(taskEntity);
		entityManager.persist(history);
		entityManager.flush();

		TaskHistoryEntity.QueryInterface queryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						TaskHistoryEntity.QueryInterface.class);
		queryObject.clearTaskHistory(taskEntity);
		entityManager = this.entityTestManager.refreshEntityManager();

		TaskEntity task = entityManager.find(TaskEntity.class, "id");
		assertEquals(0, task.getTaskHistory().size());
	}

	@Test
	public void testAddApplicationIdentity() throws Exception {
		// setup
		SubjectEntity admin = new SubjectEntity("test-admin");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", admin);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", null, applicationOwner, null, null, null,
				null, null);
		AttributeTypeEntity attributeType1 = new AttributeTypeEntity(
				"test-attribute-type-1", DatatypeType.STRING, true, true);
		AttributeTypeEntity attributeType2 = new AttributeTypeEntity(
				"test-attribute-type-2", DatatypeType.STRING, true, true);
		long identityVersion = 10;
		ApplicationIdentityEntity applicationIdentity = new ApplicationIdentityEntity(
				application, identityVersion);
		ApplicationIdentityAttributeEntity applicationIdentityAttribute1 = new ApplicationIdentityAttributeEntity(
				applicationIdentity, attributeType1, true, false);
		ApplicationIdentityAttributeEntity applicationIdentityAttribute2 = new ApplicationIdentityAttributeEntity(
				applicationIdentity, attributeType2, true, false);

		// operate: add entities
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(admin);
		entityManager.persist(applicationOwner);
		entityManager.persist(application);
		entityManager.persist(attributeType1);
		entityManager.persist(attributeType2);
		entityManager.persist(applicationIdentity);
		entityManager.persist(applicationIdentityAttribute1);
		entityManager.persist(applicationIdentityAttribute2);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		ApplicationIdentityEntity resultApplicationIdentity = entityManager
				.find(ApplicationIdentityEntity.class,
						new ApplicationIdentityPK(application.getName(),
								identityVersion));
		assertNotNull(resultApplicationIdentity);
		Set<ApplicationIdentityAttributeEntity> resultIdentityAttributes = resultApplicationIdentity
				.getAttributes();
		assertNotNull(resultIdentityAttributes);
		assertEquals(2, resultIdentityAttributes.size());

		boolean hasType1 = false, hasType2 = false;

		for (ApplicationIdentityAttributeEntity resultIdentityAttribute : resultIdentityAttributes) {
			AttributeTypeEntity resultAttributeType = resultIdentityAttribute
					.getAttributeType();
			if (attributeType1.equals(resultAttributeType))
				hasType1 = true;
			else if (attributeType2.equals(resultAttributeType))
				hasType2 = true;
			assertEquals(application.getName(), resultIdentityAttribute
					.getApplicationName());
		}

		assertTrue(hasType1);
		assertTrue(hasType2);
	}

	@Test
	public void testMultipleApplicationIdentities() throws Exception {
		SubjectEntity admin = new SubjectEntity("test-admin");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", admin);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", null, applicationOwner, null, null, null,
				null, null);
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				"test-attribute-type", DatatypeType.STRING, true, true);
		ApplicationIdentityEntity applicationIdentity1 = new ApplicationIdentityEntity(
				application, 1);
		ApplicationIdentityEntity applicationIdentity2 = new ApplicationIdentityEntity(
				application, 2);

		// operate: add entities
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(admin);
		entityManager.persist(applicationOwner);
		entityManager.persist(application);
		entityManager.persist(attributeType);
		entityManager.persist(applicationIdentity1);
		entityManager.persist(applicationIdentity2);
		entityManager.getTransaction().commit();
	}

	@Test
	public void testAddRemoveConfig() {
		// setup
		ConfigGroupEntity group = new ConfigGroupEntity("group 1");
		ConfigItemEntity item = new ConfigItemEntity("name 1", "value 1",
				"string", group);

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(group);
		entityManager.persist(item);
		entityManager.flush();
	}

	@Test
	public void testAddRemoveStatistic() {
		// setup
		StatisticEntity stat = new StatisticEntity("stat 1", "domain", null,
				new Date(System.currentTimeMillis()));
		StatisticDataPointEntity data = new StatisticDataPointEntity("point 1",
				stat, new Date(System.currentTimeMillis()), 1, 2, 3);
		stat.getStatisticDataPoints().add(data);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(stat);
		entityManager.persist(data);
		entityManager.flush();
		entityManager.remove(stat);
		entityManager.remove(data); // cascading gone in statistic data model
		entityManager.flush();

		StatisticDataPointEntity result = entityManager.find(
				StatisticDataPointEntity.class, data.getId());
		assertNull(result);
	}

	@Test
	public void testAddRemoveAttributeProvider() throws Exception {
		// setup
		SubjectEntity admin = new SubjectEntity("test-admin");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"owner", admin);
		ApplicationEntity application = new ApplicationEntity(
				"test-application", null, applicationOwner, null, null, null,
				null, null);
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				"test-attribute-type", DatatypeType.STRING, false, false);
		AttributeProviderEntity attributeProvider = new AttributeProviderEntity(
				application, attributeType);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(admin);
		entityManager.persist(applicationOwner);
		entityManager.persist(application);
		entityManager.persist(attributeType);
		entityManager.persist(attributeProvider);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		AttributeProviderEntity resultAttributeProvider = entityManager.find(
				AttributeProviderEntity.class, new AttributeProviderPK(
						application, attributeType));
		assertNotNull(resultAttributeProvider);
		assertEquals(resultAttributeProvider, attributeProvider);
	}

	@Test
	public void testCreateAuditContext() throws Exception {
		// setup
		AuditContextEntity contextEntity = new AuditContextEntity();
		AuditContextEntity contextEntity2 = new AuditContextEntity();

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(contextEntity);
		entityManager.persist(contextEntity2);

		// verify
		assertTrue(contextEntity.getId() < contextEntity2.getId());
	}

	@Test
	public void testAuditAudit() throws Exception {
		// setup
		AuditContextEntity auditContext = new AuditContextEntity();
		AuditAuditEntity auditAudit = new AuditAuditEntity(auditContext,
				"test message");
		AuditAuditEntity auditAudit2 = new AuditAuditEntity("test message 2");

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(auditContext);
		entityManager.persist(auditAudit);
		entityManager.persist(auditAudit2);

		// verify
		assertNull(auditAudit2.getAuditContext());
	}

	@Test
	public void testCreateHelpdeskContext() throws Exception {
		// setup
		HelpdeskContextEntity contextEntity = new HelpdeskContextEntity();
		HelpdeskContextEntity contextEntity2 = new HelpdeskContextEntity();

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(contextEntity);
		entityManager.persist(contextEntity2);

		// verify
		assertTrue(contextEntity.getId() < contextEntity2.getId());
	}

	@Test
	public void compoundedAttributeType() throws Exception {
		// setup
		String testParentName = "parent-attribute-type-"
				+ UUID.randomUUID().toString();
		String testMemberName = "member-attribute-type-"
				+ UUID.randomUUID().toString();
		DatatypeType testType = DatatypeType.STRING;

		AttributeTypeEntity parentAttributeType = new AttributeTypeEntity(
				testParentName, testType, true, true);
		AttributeTypeEntity memberAttributeType = new AttributeTypeEntity(
				testMemberName, testType, true, true);
		CompoundedAttributeTypeMemberEntity member = new CompoundedAttributeTypeMemberEntity(
				parentAttributeType, memberAttributeType, 0, true);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(parentAttributeType);
		entityManager.persist(memberAttributeType);
		entityManager.persist(member);

		// verify
		assertTrue(parentAttributeType.getMembers().isEmpty());
		entityManager = this.entityTestManager.refreshEntityManager();
		AttributeTypeEntity resultParent = entityManager.find(
				AttributeTypeEntity.class, testParentName);
		assertNotNull(resultParent);
		assertEquals(1, resultParent.getMembers().size());
		assertEquals(member, resultParent.getMembers().toArray()[0]);
	}

	@Test
	public void compoundedAttributeTypePersistCascading() throws Exception {
		// setup
		String testParentName = "parent-attribute-type-"
				+ UUID.randomUUID().toString();
		String testMemberName = "member-attribute-type-"
				+ UUID.randomUUID().toString();
		DatatypeType testType = DatatypeType.STRING;

		AttributeTypeEntity parentAttributeType = new AttributeTypeEntity(
				testParentName, testType, true, true);
		AttributeTypeEntity memberAttributeType = new AttributeTypeEntity(
				testMemberName, testType, true, true);
		CompoundedAttributeTypeMemberEntity member = new CompoundedAttributeTypeMemberEntity(
				parentAttributeType, memberAttributeType, 0, false);
		parentAttributeType.getMembers().add(member);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(memberAttributeType);
		entityManager.persist(parentAttributeType);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		AttributeTypeEntity resultParent = entityManager.find(
				AttributeTypeEntity.class, testParentName);
		assertNotNull(resultParent);
		assertEquals(1, resultParent.getMembers().size());
		assertEquals(member, resultParent.getMembers().toArray()[0]);
	}

	@Test
	public void compoundedAttributeTypeEagerLoading() throws Exception {
		// setup
		String testParentName = "parent-attribute-type-"
				+ UUID.randomUUID().toString();
		String testMemberName = "member-attribute-type-"
				+ UUID.randomUUID().toString();
		DatatypeType testType = DatatypeType.STRING;

		AttributeTypeEntity parentAttributeType = new AttributeTypeEntity(
				testParentName, testType, true, true);
		AttributeTypeEntity memberAttributeType = new AttributeTypeEntity(
				testMemberName, testType, true, true);
		CompoundedAttributeTypeMemberEntity member = new CompoundedAttributeTypeMemberEntity(
				parentAttributeType, memberAttributeType, 0, false);
		parentAttributeType.getMembers().add(member);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(memberAttributeType);
		entityManager.persist(parentAttributeType);

		// verify
		entityManager = this.entityTestManager.refreshEntityManager();
		AttributeTypeEntity resultParent = entityManager.find(
				AttributeTypeEntity.class, testParentName);
		assertNotNull(resultParent);

		// operate & verify: eager loading of members
		entityManager.clear(); // detaches the resultParent
		assertEquals(1, resultParent.getMembers().size());
		assertTrue(resultParent.isCompounded());
	}

	@Test
	public void testAccessAuditErrorQuery() throws Exception {
		// setup
		AuditContextEntity auditContext = new AuditContextEntity();
		AccessAuditEntity accessAudit = new AccessAuditEntity(auditContext,
				"test-operation", OperationStateType.BEGIN, null);

		// operate
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(auditContext);
		entityManager.persist(accessAudit);

		AccessAuditEntity.QueryInterface queryObject = QueryObjectFactory
				.createQueryObject(entityManager,
						AccessAuditEntity.QueryInterface.class);
		long count = queryObject.countErrorRecords(auditContext.getId());

		// verify
		LOG.debug("audit context Id: " + auditContext.getId());
		LOG.debug("count: " + count);
		assertEquals(0, count);

		accessAudit = new AccessAuditEntity(auditContext, "test-operation",
				OperationStateType.BUSINESS_EXCEPTION_END, null);
		entityManager.persist(accessAudit);

		assertEquals(2, queryObject.listRecords(auditContext.getId()).size());

		count = queryObject.countErrorRecords(auditContext.getId());
		assertEquals(1, count);
	}
}
