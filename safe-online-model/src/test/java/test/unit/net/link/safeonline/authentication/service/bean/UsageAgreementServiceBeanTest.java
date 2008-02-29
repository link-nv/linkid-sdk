/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.bean.UsageAgreementServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class UsageAgreementServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		JmxTestUtils jmxTestUtils = new JmxTestUtils();
		jmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

		final KeyPair keyPair = PkiTestUtils.generateKeyPair();
		final X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		jmxTestUtils.registerActionHandler(
				AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE,
				"getCertificate", new MBeanActionHandler() {
					public Object invoke(@SuppressWarnings("unused")
					Object[] arguments) {
						return certificate;
					}
				});

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		systemStartable.postStart();
		this.entityTestManager.refreshEntityManager();
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testUsageAgreementUseCase() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		SubjectService subjectService = EJBTestUtils.newInstance(
				SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		String ownerId = subjectService.findSubjectFromUserName("owner")
				.getUserId();

		UsageAgreementService usageAgreementService = EJBTestUtils.newInstance(
				UsageAgreementServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, ownerId,
				SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE);

		// operate
		UsageAgreementEntity usageAgreement = usageAgreementService
				.getCurrentUsageAgreement(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

		// verify
		assertNull(usageAgreement);

		// operate
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();

		usageAgreementService.createDraftUsageAgreement(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
				UsageAgreementPK.EMPTY_USAGE_AGREEMENT_VERSION);
		usageAgreementService.createDraftUsageAgreementText(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
				Locale.ENGLISH.getLanguage(), "test-usage-agreement");
		usageAgreementService
				.updateUsageAgreement(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);
		entityManager.getTransaction().commit();
		usageAgreement = usageAgreementService
				.getCurrentUsageAgreement(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

		// verify
		assertEquals(new Long(
				UsageAgreementPK.EMPTY_USAGE_AGREEMENT_VERSION + 1),
				usageAgreement.getUsageAgreementVersion());
	}

	public void testGlobalUsageAgreementUseCase() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		SubjectService subjectService = EJBTestUtils.newInstance(
				SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		String operId = subjectService.findSubjectFromUserName("admin")
				.getUserId();

		UsageAgreementService usageAgreementService = EJBTestUtils.newInstance(
				UsageAgreementServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, operId,
				SafeOnlineRoles.OPERATOR_ROLE);

		// operate
		GlobalUsageAgreementEntity usageAgreement = usageAgreementService
				.getCurrentGlobalUsageAgreement();

		// verify
		assertNull(usageAgreement);

		// operate
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();

		usageAgreementService.createDraftGlobalUsageAgreement();
		usageAgreementService.createDraftGlobalUsageAgreementText(
				Locale.ENGLISH.getLanguage(), "test-usage-agreement");
		usageAgreementService.updateGlobalUsageAgreement();
		entityManager.getTransaction().commit();

		// verify
		usageAgreement = usageAgreementService.getCurrentGlobalUsageAgreement();

		// verify
		assertEquals(
				GlobalUsageAgreementEntity.INITIAL_GLOBAL_USAGE_AGREEMENT_VERSION,
				usageAgreement.getUsageAgreementVersion());

	}
}
