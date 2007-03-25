/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.CachedOcspResponseDAOBean;
import net.link.safeonline.dao.bean.HistoryDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.dao.bean.TrustPointDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.model.bean.ApplicationIdentityManagerBean;
import net.link.safeonline.model.bean.ApplicationOwnerManagerBean;
import net.link.safeonline.model.bean.SubjectManagerBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.PkiService;
import net.link.safeonline.service.bean.PkiServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.PkiTestUtils;

public class PkiServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private static final Class[] container = new Class[] {
			SubjectDAOBean.class, ApplicationDAOBean.class,
			SubscriptionDAOBean.class, AttributeDAOBean.class,
			TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class,
			AttributeTypeDAOBean.class, ApplicationIdentityDAOBean.class,
			SubjectManagerBean.class, HistoryDAOBean.class,
			ApplicationOwnerManagerBean.class, TrustPointDAOBean.class,
			CachedOcspResponseDAOBean.class,
			ApplicationIdentityManagerBean.class };

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SubjectEntity.class,
				ApplicationEntity.class, ApplicationOwnerEntity.class,
				AttributeEntity.class, AttributeTypeEntity.class,
				SubscriptionEntity.class, TrustDomainEntity.class,
				ApplicationIdentityEntity.class, TrustPointEntity.class);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class, container,
				entityManager);
		systemStartable.postStart();
		this.entityTestManager.refreshEntityManager();
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testAddRemoveTrustDomain() throws Exception {
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		PkiService pkiService = EJBTestUtils.newInstance(PkiServiceBean.class,
				container, entityManager, "test-operator",
				SafeOnlineRoles.OPERATOR_ROLE);

		pkiService.addTrustDomain("test-trust-domain", true);
		TrustDomainEntity trustDomain = pkiService
				.getTrustDomain("test-trust-domain");
		assertEquals("test-trust-domain", trustDomain.getName());
		pkiService.removeTrustDomain("test-trust-domain");
		try {
			pkiService.getTrustDomain("test-trust-domain");
			fail();
		} catch (TrustDomainNotFoundException e) {
			// expected
		}
	}

	public void testAddTrustPointWithFakeCertificateThrowsException()
			throws Exception {
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		PkiService pkiService = EJBTestUtils.newInstance(PkiServiceBean.class,
				container, entityManager, "test-operator",
				SafeOnlineRoles.OPERATOR_ROLE);

		pkiService.addTrustDomain("test-trust-domain", true);
		try {
			pkiService.addTrustPoint("test-trust-domain", "foobar".getBytes());
			fail();
		} catch (CertificateEncodingException e) {
			// expected
		}
	}

	public void testAddRemoveTrustPoint() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");

		PkiService pkiService = EJBTestUtils.newInstance(PkiServiceBean.class,
				container, entityManager, "test-operator",
				SafeOnlineRoles.OPERATOR_ROLE);

		pkiService.addTrustDomain("test-trust-domain", true);
		List<TrustPointEntity> result = pkiService
				.listTrustPoints("test-trust-domain");
		assertNotNull(result);
		assertTrue(result.isEmpty());
		pkiService.addTrustPoint("test-trust-domain", certificate.getEncoded());
		pkiService.listTrustPoints("test-trust-domain");
		result = pkiService.listTrustPoints("test-trust-domain");
		assertEquals(1, result.size());
		assertEquals(certificate, result.get(0).getCertificate());
	}
}
