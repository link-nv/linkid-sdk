/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Copyright 2005-2006 Frank Cornelis.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.URI;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.model.CachedOcspValidator;
import net.link.safeonline.model.bean.PkiValidatorBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

public class PkiValidatorBeanTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(PkiValidatorBeanTest.class);

	private PkiValidatorBean testedInstance;

	private TrustPointDAO mockTrustPointDAO;

	private CachedOcspValidator mockCachedOcspValidatorBean;

	private URI ocspUri;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new PkiValidatorBean();

		this.mockTrustPointDAO = createMock(TrustPointDAO.class);

		this.mockCachedOcspValidatorBean = createMock(CachedOcspValidator.class);

		EJBTestUtils.inject(this.testedInstance, this.mockTrustPointDAO);
		EJBTestUtils.inject(this.testedInstance,
				this.mockCachedOcspValidatorBean);
		EJBTestUtils.init(this.testedInstance);
	}

	public void testValidateCertificateOnEmptyTrustDomainFails()
			throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");
		String trustDomainName = "test-trust-domain";
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();

		// stubs
		expect(this.mockTrustPointDAO.listTrustPoints(trustDomain))
				.andStubReturn(trustPoints);

		// prepare
		replay(this.mockTrustPointDAO);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		assertFalse(result);
	}

	public void testValidateNullCertificateThrowsIllegalArgumentException()
			throws Exception {
		// operate & verify
		try {
			this.testedInstance.validateCertificate(new TrustDomainEntity(),
					null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testValidateCertificate() throws Exception {
		// setup
		KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
		DateTime now = new DateTime();
		DateTime caNotBefore = now.minusDays(10);
		DateTime caNotAfter = now.plusDays(10);
		X509Certificate caCertificate = PkiTestUtils
				.generateSelfSignedCertificate(caKeyPair, "CN=TestCA",
						caNotBefore, caNotAfter, null, true, false);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		DateTime notBefore = now.minusDays(1);
		DateTime notAfter = now.plusDays(1);
		X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair
				.getPublic(), "CN=Test", caKeyPair.getPrivate(), caCertificate,
				notBefore, notAfter, null, false, false, this.ocspUri);

		String trustDomainName = "test-trust-domain";
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
		TrustPointEntity caTrustPoint = new TrustPointEntity(trustDomain,
				caCertificate);
		LOG.debug("ca key id: " + caTrustPoint.getPk().getKeyId());
		trustPoints.add(caTrustPoint);

		// stubs
		expect(this.mockTrustPointDAO.listTrustPoints(trustDomain))
				.andStubReturn(trustPoints);
		expect(
				this.mockCachedOcspValidatorBean.performCachedOcspCheck(
						trustDomain, certificate, caCertificate)).andReturn(
				true);

		// prepare
		replay(this.mockTrustPointDAO);
		replay(this.mockCachedOcspValidatorBean);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		verify(this.mockCachedOcspValidatorBean);
		assertTrue(result);
	}

	public void testValidateTrustPointCertificate() throws Exception {
		// setup
		KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
		DateTime now = new DateTime();
		DateTime caNotBefore = now.minusDays(10);
		DateTime caNotAfter = now.plusDays(10);
		X509Certificate caCertificate = PkiTestUtils
				.generateSelfSignedCertificate(caKeyPair, "CN=TestCA",
						caNotBefore, caNotAfter, null, true, false);

		String trustDomainName = "test-trust-domain";
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
		TrustPointEntity caTrustPoint = new TrustPointEntity(trustDomain,
				caCertificate);
		LOG.debug("ca key id: " + caTrustPoint.getPk().getKeyId());
		trustPoints.add(caTrustPoint);

		// stubs
		expect(this.mockTrustPointDAO.listTrustPoints(trustDomain))
				.andStubReturn(trustPoints);
		expect(
				this.mockCachedOcspValidatorBean.performCachedOcspCheck(
						trustDomain, caCertificate, caCertificate)).andReturn(
				true);

		// prepare
		replay(this.mockTrustPointDAO);
		replay(this.mockCachedOcspValidatorBean);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				caCertificate);

		// verify
		verify(this.mockTrustPointDAO);
		verify(this.mockCachedOcspValidatorBean);
		assertTrue(result);
	}

	public void testValidateCertificateFailsIfOCSPRevokes() throws Exception {
		// setup
		KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
		DateTime now = new DateTime();
		DateTime caNotBefore = now.minusDays(10);
		DateTime caNotAfter = now.plusDays(10);
		X509Certificate caCertificate = PkiTestUtils
				.generateSelfSignedCertificate(caKeyPair, "CN=TestCA",
						caNotBefore, caNotAfter, null, true, false);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		DateTime notBefore = now.minusDays(1);
		DateTime notAfter = now.plusDays(1);
		X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair
				.getPublic(), "CN=Test", caKeyPair.getPrivate(), caCertificate,
				notBefore, notAfter, null, false, false, this.ocspUri);

		String trustDomainName = "test-trust-domain";
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
		trustPoints.add(new TrustPointEntity(trustDomain, caCertificate));

		// stubs
		expect(this.mockTrustPointDAO.listTrustPoints(trustDomain))
				.andStubReturn(trustPoints);
		expect(
				this.mockCachedOcspValidatorBean.performCachedOcspCheck(
						trustDomain, certificate, caCertificate)).andReturn(
				false);

		// prepare
		replay(this.mockTrustPointDAO);
		replay(this.mockCachedOcspValidatorBean);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		verify(this.mockCachedOcspValidatorBean);
		assertFalse(result);
	}

	public void testValidateCertificateRootCaAndInterCa() throws Exception {
		// setup
		KeyPair rootCaKeyPair = PkiTestUtils.generateKeyPair();
		DateTime now = new DateTime();
		DateTime rootCaNotBefore = now.minusDays(10);
		DateTime rootCaNotAfter = now.plusDays(10);
		X509Certificate rootCaCertificate = PkiTestUtils
				.generateSelfSignedCertificate(rootCaKeyPair, "CN=TestRootCA",
						rootCaNotBefore, rootCaNotAfter, null, true, false);

		KeyPair interCaKeyPair = PkiTestUtils.generateKeyPair();
		DateTime interCaNotBefore = now.minusDays(5);
		DateTime interCaNotAfter = now.plusDays(5);
		X509Certificate interCaCertificate = PkiTestUtils.generateCertificate(
				interCaKeyPair.getPublic(), "CN=TestInterCA", rootCaKeyPair
						.getPrivate(), rootCaCertificate, interCaNotBefore,
				interCaNotAfter, null, true, false, null);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		DateTime notBefore = now.minusDays(1);
		DateTime notAfter = now.plusDays(1);
		X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair
				.getPublic(), "CN=Test", interCaKeyPair.getPrivate(),
				interCaCertificate, notBefore, notAfter, null, false, false,
				this.ocspUri);

		String trustDomainName = "test-trust-domain";
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
		trustPoints.add(new TrustPointEntity(trustDomain, rootCaCertificate));
		trustPoints.add(new TrustPointEntity(trustDomain, interCaCertificate));

		// stubs
		expect(this.mockTrustPointDAO.listTrustPoints(trustDomain))
				.andStubReturn(trustPoints);
		expect(
				this.mockCachedOcspValidatorBean.performCachedOcspCheck(
						trustDomain, certificate, interCaCertificate))
				.andReturn(true);

		// prepare
		replay(this.mockTrustPointDAO);
		replay(this.mockCachedOcspValidatorBean);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		verify(this.mockCachedOcspValidatorBean);
		assertTrue(result);
	}

	public void testValidationFailsIfTrustPointIsNotCA() throws Exception {
		// setup
		KeyPair rootCaKeyPair = PkiTestUtils.generateKeyPair();
		DateTime now = new DateTime();
		DateTime rootCaNotBefore = now.minusDays(10);
		DateTime rootCaNotAfter = now.plusDays(10);
		X509Certificate rootCaCertificate = PkiTestUtils
				.generateSelfSignedCertificate(rootCaKeyPair, "CN=TestRootCA",
						rootCaNotBefore, rootCaNotAfter, null, true, false);

		KeyPair interCaKeyPair = PkiTestUtils.generateKeyPair();
		DateTime interCaNotBefore = now.minusDays(5);
		DateTime interCaNotAfter = now.plusDays(5);
		X509Certificate interCaCertificate = PkiTestUtils.generateCertificate(
				interCaKeyPair.getPublic(), "CN=TestInterCA", rootCaKeyPair
						.getPrivate(), rootCaCertificate, interCaNotBefore,
				interCaNotAfter, null, false, false, null);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		DateTime notBefore = now.minusDays(1);
		DateTime notAfter = now.plusDays(1);
		X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair
				.getPublic(), "CN=Test", interCaKeyPair.getPrivate(),
				interCaCertificate, notBefore, notAfter, null, false, false,
				this.ocspUri);

		String trustDomainName = "test-trust-domain";
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
		trustPoints.add(new TrustPointEntity(trustDomain, rootCaCertificate));
		trustPoints.add(new TrustPointEntity(trustDomain, interCaCertificate));

		// stubs
		expect(this.mockTrustPointDAO.listTrustPoints(trustDomain))
				.andStubReturn(trustPoints);
		expect(
				this.mockCachedOcspValidatorBean.performCachedOcspCheck(
						trustDomain, certificate, interCaCertificate))
				.andStubReturn(true);

		// prepare
		replay(this.mockTrustPointDAO);
		replay(this.mockCachedOcspValidatorBean);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		verify(this.mockCachedOcspValidatorBean);
		assertFalse(result);
	}

	public void testValidateCertificateIfRootIsNotSelfSignedFails()
			throws Exception {
		// setup
		KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
		KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
		DateTime now = new DateTime();
		DateTime caNotBefore = now.minusDays(5);
		DateTime caNotAfter = now.plusDays(5);
		X509Certificate caCertificate = PkiTestUtils
				.generateCertificate(caKeyPair.getPublic(), "CN=TestCA",
						rootKeyPair.getPrivate(), null, caNotBefore,
						caNotAfter, null, false, false, this.ocspUri);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		DateTime notBefore = now.minusDays(1);
		DateTime notAfter = now.plusDays(1);
		X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair
				.getPublic(), "CN=Test", caKeyPair.getPrivate(), caCertificate,
				notBefore, notAfter, null, false, false, this.ocspUri);

		String trustDomainName = "test-trust-domain";
		TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName,
				true);
		List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
		trustPoints.add(new TrustPointEntity(trustDomain, caCertificate));

		// stubs
		expect(this.mockTrustPointDAO.listTrustPoints(trustDomain))
				.andStubReturn(trustPoints);

		// prepare
		replay(this.mockTrustPointDAO);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		assertFalse(result);
	}
}
