/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.service.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.pkix.service.bean.PkiServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class PkiServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		JmxTestUtils jmxTestUtils = new JmxTestUtils();
		jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

		final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
		final X509Certificate authCertificate = PkiTestUtils
				.generateSelfSignedCertificate(authKeyPair, "CN=Test");
		jmxTestUtils.registerActionHandler(
				AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE,
				"getCertificate", new MBeanActionHandler() {
					public Object invoke(@SuppressWarnings("unused")
					Object[] arguments) {
						return authCertificate;
					}
				});

		jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

		final KeyPair keyPair = PkiTestUtils.generateKeyPair();
		final X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		jmxTestUtils.registerActionHandler(
				IdentityServiceClient.IDENTITY_SERVICE, "getCertificate",
				new MBeanActionHandler() {
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

	public void testAddRemoveTrustDomain() throws Exception {
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		PkiService pkiService = EJBTestUtils.newInstance(PkiServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", SafeOnlineRoles.OPERATOR_ROLE);

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
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", SafeOnlineRoles.OPERATOR_ROLE);

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
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", SafeOnlineRoles.OPERATOR_ROLE);

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
