/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Date;

import net.link.safeonline.dao.CachedOcspResponseDAO;
import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.OcspValidator;
import net.link.safeonline.model.OcspValidator.OcspResult;
import net.link.safeonline.model.bean.CachedOcspValidatorBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import junit.framework.TestCase;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyBoolean;

public class CachedOcspValidatorBeanTest extends TestCase {

	private CachedOcspValidatorBean testedInstance;

	private CachedOcspResponseDAO mockCachedOcspResponseDAOBean;

	private OcspValidator mockOcspValidatorBean;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new CachedOcspValidatorBean();

		this.mockOcspValidatorBean = createMock(OcspValidator.class);
		this.mockCachedOcspResponseDAOBean = createMock(CachedOcspResponseDAO.class);

		EJBTestUtils.inject(this.testedInstance, this.mockOcspValidatorBean);
		EJBTestUtils.inject(this.testedInstance,
				this.mockCachedOcspResponseDAOBean);
		EJBTestUtils.init(this.testedInstance);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testValid() throws Exception {
		// setup
		URI ocspUri = new URI("http://test.ocsp.responder/");
		X509Certificate certificate = PkiTestUtils
				.generateTestSelfSignedCert(ocspUri);

		CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(
				null, true, null);

		TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain",
				true, 3600000);

		// stubs call 1
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(null);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate)).andReturn(OcspResult.GOOD);
		expect(
				this.mockCachedOcspResponseDAOBean.addCachedOcspResponse(
						(String) anyObject(), anyBoolean(),
						(TrustDomainEntity) anyObject())).andReturn(
				cachedOcspResponse);

		// stubs call 2
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(cachedOcspResponse);

		// prepare
		replay(this.mockCachedOcspResponseDAOBean);
		replay(this.mockOcspValidatorBean);

		// operate
		boolean firstLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);
		boolean secondLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);

		// verify
		verify(this.mockCachedOcspResponseDAOBean);
		verify(this.mockOcspValidatorBean);
		assertTrue(firstLookup);
		assertTrue(secondLookup);
	}

	public void testRevoked() throws Exception {
		// setup
		URI ocspUri = new URI("http://test.ocsp.responder/");
		X509Certificate certificate = PkiTestUtils
				.generateTestSelfSignedCert(ocspUri);

		CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(
				null, false, null);

		TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain",
				true, 3600000);

		// stubs call 1
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(null);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate))
				.andReturn(OcspResult.REVOKED);
		expect(
				this.mockCachedOcspResponseDAOBean.addCachedOcspResponse(
						(String) anyObject(), anyBoolean(),
						(TrustDomainEntity) anyObject())).andReturn(
				cachedOcspResponse);

		// stubs call 2
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(cachedOcspResponse);

		// prepare
		replay(this.mockCachedOcspResponseDAOBean);
		replay(this.mockOcspValidatorBean);

		// operate
		boolean firstLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);
		boolean secondLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);

		// verify
		verify(this.mockCachedOcspResponseDAOBean);
		verify(this.mockOcspValidatorBean);
		assertFalse(firstLookup);
		assertFalse(secondLookup);
	}

	public void testFailed() throws Exception {
		// setup
		URI ocspUri = new URI("http://test.ocsp.responder/");
		X509Certificate certificate = PkiTestUtils
				.generateTestSelfSignedCert(ocspUri);

		// stubs call 1
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(null);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate)).andReturn(OcspResult.FAILED);

		// stubs call 2
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(null);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate)).andReturn(OcspResult.FAILED);

		// prepare
		replay(this.mockCachedOcspResponseDAOBean);
		replay(this.mockOcspValidatorBean);

		// operate
		boolean firstLookup = this.testedInstance.performCachedOcspCheck(null,
				certificate, certificate);
		boolean secondLookup = this.testedInstance.performCachedOcspCheck(null,
				certificate, certificate);

		// verify
		verify(this.mockCachedOcspResponseDAOBean);
		verify(this.mockOcspValidatorBean);
		assertFalse(firstLookup);
		assertFalse(secondLookup);
	}

	public void testExpiredValid() throws Exception {
		// setup
		URI ocspUri = new URI("http://test.ocsp.responder/");
		X509Certificate certificate = PkiTestUtils
				.generateTestSelfSignedCert(ocspUri);

		CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(
				null, true, null);
		cachedOcspResponse.setEntryDate(new Date(0));

		TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain",
				true, 3600000);

		// stubs call 1
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(null);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate)).andReturn(OcspResult.GOOD);
		expect(
				this.mockCachedOcspResponseDAOBean.addCachedOcspResponse(
						(String) anyObject(), anyBoolean(),
						(TrustDomainEntity) anyObject())).andReturn(
				cachedOcspResponse);

		// stubs call 2
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(cachedOcspResponse);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate)).andReturn(OcspResult.GOOD);

		// prepare
		replay(this.mockCachedOcspResponseDAOBean);
		replay(this.mockOcspValidatorBean);

		// operate
		boolean firstLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);
		boolean secondLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);

		// verify
		verify(this.mockCachedOcspResponseDAOBean);
		verify(this.mockOcspValidatorBean);
		assertTrue(firstLookup);
		assertTrue(secondLookup);
	}

	public void testExpiredRevoked() throws Exception {
		// setup
		URI ocspUri = new URI("http://test.ocsp.responder/");
		X509Certificate certificate = PkiTestUtils
				.generateTestSelfSignedCert(ocspUri);

		CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(
				null, false, null);
		cachedOcspResponse.setEntryDate(new Date(0));

		TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain",
				true, 3600000);

		// stubs call 1
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(null);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate))
				.andReturn(OcspResult.REVOKED);
		expect(
				this.mockCachedOcspResponseDAOBean.addCachedOcspResponse(
						(String) anyObject(), anyBoolean(),
						(TrustDomainEntity) anyObject())).andReturn(
				cachedOcspResponse);

		// stubs call 2
		expect(this.mockOcspValidatorBean.getOcspUri(certificate)).andReturn(
				ocspUri);
		expect(
				this.mockCachedOcspResponseDAOBean
						.findCachedOcspResponse((String) anyObject()))
				.andReturn(cachedOcspResponse);
		expect(
				this.mockOcspValidatorBean.verifyOcspStatus(ocspUri,
						certificate, certificate))
				.andReturn(OcspResult.REVOKED);

		// prepare
		replay(this.mockCachedOcspResponseDAOBean);
		replay(this.mockOcspValidatorBean);

		// operate
		boolean firstLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);
		boolean secondLookup = this.testedInstance.performCachedOcspCheck(
				trustDomain, certificate, certificate);

		// verify
		verify(this.mockCachedOcspResponseDAOBean);
		verify(this.mockOcspValidatorBean);
		assertFalse(firstLookup);
		assertFalse(secondLookup);
	}

}
