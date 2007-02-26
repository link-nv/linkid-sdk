/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.service.bean.PkiServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

public class PkiServiceBeanTest extends TestCase {

	private PkiServiceBean testedInstance;

	private TrustDomainDAO mockTrustDomainDAO;

	private TrustPointDAO mockTrustPointDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new PkiServiceBean();

		this.mockTrustDomainDAO = createMock(TrustDomainDAO.class);
		this.mockTrustPointDAO = createMock(TrustPointDAO.class);

		EJBTestUtils.inject(this.testedInstance, this.mockTrustDomainDAO);
		EJBTestUtils.inject(this.testedInstance, this.mockTrustPointDAO);
		EJBTestUtils.init(this.testedInstance);
	}

	public void testAddTrustPoint() throws Exception {
		// setup
		KeyPair testKeyPair = PkiTestUtils.generateKeyPair();
		String dn = "CN=Test";
		X509Certificate testCertificate = PkiTestUtils
				.generateSelfSignedCertificate(testKeyPair, dn);
		String testTrustDomainName = "test-trust-domain-name-" + getName();
		byte[] encodedTestCertificate = testCertificate.getEncoded();

		// stubs
		TrustDomainEntity testTrustDomain = new TrustDomainEntity(
				testTrustDomainName, true);
		expect(this.mockTrustDomainDAO.getTrustDomain(testTrustDomainName))
				.andStubReturn(testTrustDomain);
		expect(
				this.mockTrustPointDAO.findTrustPoint(testTrustDomain,
						testCertificate)).andStubReturn(null);

		// expectations
		this.mockTrustPointDAO.addTrustPoint(testTrustDomain, testCertificate);

		// prepare
		replay(this.mockTrustDomainDAO, this.mockTrustPointDAO);

		// operate
		this.testedInstance.addTrustPoint(testTrustDomainName,
				encodedTestCertificate);

		// verify
		verify(this.mockTrustDomainDAO, this.mockTrustPointDAO);
	}

	public void testAddTrustPointWithFaceCertificateThrowsException()
			throws Exception {
		// setup
		String testTrustDomainName = "test-trust-domain-name-" + getName();
		byte[] encodedTestCertificate = "foobar".getBytes();

		// stubs
		TrustDomainEntity testTrustDomain = new TrustDomainEntity(
				testTrustDomainName, true);
		expect(this.mockTrustDomainDAO.getTrustDomain(testTrustDomainName))
				.andStubReturn(testTrustDomain);

		// prepare
		replay(this.mockTrustDomainDAO, this.mockTrustPointDAO);

		// operate & verify
		try {
			this.testedInstance.addTrustPoint(testTrustDomainName,
					encodedTestCertificate);
			fail();
		} catch (CertificateEncodingException e) {
			// expected
			verify(this.mockTrustDomainDAO, this.mockTrustPointDAO);
		}
	}
}
