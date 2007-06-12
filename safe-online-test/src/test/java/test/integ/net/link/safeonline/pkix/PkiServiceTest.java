/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.pkix;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import javax.naming.InitialContext;

import junit.framework.TestCase;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.integ.net.link.safeonline.IntegrationTestUtils;

/**
 * PKIX service integration tests.
 * 
 * @author fcorneli
 * 
 */
public class PkiServiceTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(PkiServiceTest.class);

	private PkiService pkiService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();
		this.pkiService = getPkiService(initialContext);
	}

	public void testTrustDomain() throws Exception {
		// setup
		String trustDomainName = UUID.randomUUID().toString();

		// operate
		IntegrationTestUtils.login("admin", "admin");
		List<TrustDomainEntity> trustDomains = this.pkiService
				.listTrustDomains();
		int origSize = trustDomains.size();
		LOG.debug("number of trust domains: " + origSize);
		this.pkiService.addTrustDomain(trustDomainName, true);
		trustDomains = this.pkiService.listTrustDomains();
		assertEquals(origSize + 1, trustDomains.size());
		boolean containsAddedTrustDomain = false;
		for (TrustDomainEntity trustDomain : trustDomains) {
			LOG.debug(trustDomain.toString());
			if (trustDomainName.equals(trustDomain.getName())) {
				containsAddedTrustDomain = true;
			}
		}
		assertTrue(containsAddedTrustDomain);

		// operate: adding twice does not work
		try {
			this.pkiService.addTrustDomain(trustDomainName, true);
			fail();
		} catch (Exception e) {
			// expected
			LOG.debug("expected exception type: " + e.getClass().getName());
		}

		// operate: remove trust domain
		this.pkiService.removeTrustDomain(trustDomainName);

		// verify
		trustDomains = this.pkiService.listTrustDomains();
		containsAddedTrustDomain = false;
		for (TrustDomainEntity trustDomain : trustDomains) {
			LOG.debug(trustDomain.toString());
			if (trustDomainName.equals(trustDomain.getName())) {
				containsAddedTrustDomain = true;
			}
		}
		assertFalse(containsAddedTrustDomain);

		// operate & verify: removing twice does not work
		try {
			this.pkiService.removeTrustDomain(trustDomainName);
			fail();
		} catch (TrustDomainNotFoundException e) {
			// expected
		}
	}

	public void testTrustPoint() throws Exception {
		// setup
		String trustDomainName = "domain-" + UUID.randomUUID().toString();
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		String dn = "CN=Test";
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, dn);

		// operate: add trust domain
		IntegrationTestUtils.login("admin", "admin");
		this.pkiService.addTrustDomain(trustDomainName, true);

		// operate: add trust point
		this.pkiService
				.addTrustPoint(trustDomainName, certificate.getEncoded());

		// operate: get trust points
		List<TrustPointEntity> trustPoints = this.pkiService
				.listTrustPoints(trustDomainName);

		// verify
		assertEquals(1, trustPoints.size());
		assertEquals(certificate, trustPoints.get(0).getCertificate());

		// operate: remove trust point
		this.pkiService.removeTrustPoint(trustPoints.get(0));

		// operate: remove trust domain
		this.pkiService.removeTrustDomain(trustDomainName);
	}

	private PkiService getPkiService(InitialContext initialContext) {
		PkiService pkiService = EjbUtils.getEJB(initialContext,
				"SafeOnline/PkiServiceBean/remote", PkiService.class);
		return pkiService;
	}
}