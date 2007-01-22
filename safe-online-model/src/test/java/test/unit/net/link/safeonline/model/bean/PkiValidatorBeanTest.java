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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.model.bean.PkiValidatorBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.ocsp.BasicOCSPResp;
import org.bouncycastle.ocsp.BasicOCSPRespGenerator;
import org.bouncycastle.ocsp.CertificateID;
import org.bouncycastle.ocsp.CertificateStatus;
import org.bouncycastle.ocsp.OCSPException;
import org.bouncycastle.ocsp.OCSPReq;
import org.bouncycastle.ocsp.OCSPResp;
import org.bouncycastle.ocsp.OCSPRespGenerator;
import org.bouncycastle.ocsp.Req;
import org.bouncycastle.ocsp.RevokedStatus;
import org.bouncycastle.util.encoders.Hex;
import org.joda.time.DateTime;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;

public class PkiValidatorBeanTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(PkiValidatorBeanTest.class);

	private PkiValidatorBean testedInstance;

	private TrustPointDAO mockTrustPointDAO;

	private Server server;

	private URI ocspUri;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new PkiValidatorBean();

		this.mockTrustPointDAO = createMock(TrustPointDAO.class);

		EJBTestUtils.inject(this.testedInstance, this.mockTrustPointDAO);
		EJBTestUtils.init(this.testedInstance);

		// TODO: move to safe-online-test-util
		this.server = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(0);
		this.server.addConnector(connector);

		Context context = new Context();
		context.setContextPath("/");
		this.server.addHandler(context);

		ServletHandler handler = context.getServletHandler();

		ServletHolder servletHolder = new ServletHolder();
		servletHolder.setClassName(TestOcspResponderServlet.class.getName());
		String servletName = "TestOCSPResponderServlet";
		servletHolder.setName(servletName);
		handler.addServlet(servletHolder);

		ServletMapping servletMapping = new ServletMapping();
		servletMapping.setServletName(servletName);
		servletMapping.setPathSpecs(new String[] { "/*" });
		handler.addServletMapping(servletMapping);

		this.server.start();

		int port = connector.getLocalPort();
		LOG.debug("port: " + port);

		String ocspServletLocation = "http://localhost:" + port + "/";
		this.ocspUri = new URI(ocspServletLocation);
	}

	@Override
	public void tearDown() throws Exception {
		this.server.stop();

		super.tearDown();
	}

	public void testGetOcspUri() throws Exception {
		// setup
		URI ocspUri = new URI("http://test.ocsp.location/");
		X509Certificate certificate = generateTestSelfSignedCert(ocspUri);

		// operate
		URI resultOcspUri = this.testedInstance.getOcspUri(certificate);

		// verify
		assertEquals(ocspUri, resultOcspUri);
	}

	public void testGetOcspUriGivesNullOnMissingOcspAccessLocation()
			throws Exception {
		// setup
		X509Certificate certificate = generateTestSelfSignedCert(null);

		// operate
		URI resultOcspUri = this.testedInstance.getOcspUri(certificate);

		// verify
		assertNull(resultOcspUri);
	}

	public void testPerformOcspCheckFailsIfOcspResponderIsDown()
			throws Exception {
		// setup
		URI ocspUri = new URI("http://localhost:1/");
		X509Certificate certificate = generateTestSelfSignedCert(ocspUri);

		// operate
		boolean result = this.testedInstance.performOcspCheck(certificate,
				certificate);

		// verify
		assertFalse(result);
	}

	private X509Certificate generateTestSelfSignedCert(URI ocspUri)
			throws Exception {
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		DateTime now = new DateTime();
		DateTime notBefore = now.minusDays(1);
		DateTime notAfter = now.plusDays(1);
		X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair
				.getPublic(), "CN=Test", keyPair.getPrivate(), null, notBefore,
				notAfter, null, true, false, ocspUri);
		return certificate;
	}

	public void testPerformOcspCheckFailsIfOcspResponderDoesNotExist()
			throws Exception {
		// setup
		URI ocspUri = new URI("http://foobar.ocsp.responder/");
		X509Certificate certificate = generateTestSelfSignedCert(ocspUri);

		// operate
		boolean result = this.testedInstance.performOcspCheck(certificate,
				certificate);

		// verify
		assertFalse(result);
	}

	public void testPerformOcspCheck() throws Exception {
		// setup
		X509Certificate certificate = generateTestSelfSignedCert(this.ocspUri);

		// operate
		boolean result = this.testedInstance.verifyOcspStatus(this.ocspUri,
				certificate, certificate);

		// verify
		assertTrue(result);
		assertTrue(TestOcspResponderServlet.hasBeenCalled());
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
		expect(this.mockTrustPointDAO.getTrustPoints(trustDomain))
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
		trustPoints.add(new TrustPointEntity(trustDomain, caCertificate));

		// stubs
		expect(this.mockTrustPointDAO.getTrustPoints(trustDomain))
				.andStubReturn(trustPoints);

		// prepare
		replay(this.mockTrustPointDAO);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		assertTrue(result);
		assertTrue(TestOcspResponderServlet.hasBeenCalled());
	}

	public void testValidateCertificateFailsIfOCSPRevokes() throws Exception {
		// setup
		TestOcspResponderServlet.setCertificateStatus(new RevokedStatus(
				new Date(), CRLReason.keyCompromise));

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
		expect(this.mockTrustPointDAO.getTrustPoints(trustDomain))
				.andStubReturn(trustPoints);

		// prepare
		replay(this.mockTrustPointDAO);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		assertFalse(result);
		assertTrue(TestOcspResponderServlet.hasBeenCalled());

		TestOcspResponderServlet.setCertificateStatus(CertificateStatus.GOOD);
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
		expect(this.mockTrustPointDAO.getTrustPoints(trustDomain))
				.andStubReturn(trustPoints);

		// prepare
		replay(this.mockTrustPointDAO);

		// operate
		boolean result = this.testedInstance.validateCertificate(trustDomain,
				certificate);

		// verify
		verify(this.mockTrustPointDAO);
		assertTrue(result);
		assertTrue(TestOcspResponderServlet.hasBeenCalled());
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
		expect(this.mockTrustPointDAO.getTrustPoints(trustDomain))
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

	public static class TestOcspResponderServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private static final Log LOG = LogFactory
				.getLog(TestOcspResponderServlet.class);

		private X509Certificate ocspResponderCertificate;

		private PrivateKey ocspResponderPrivateKey;

		private static boolean called;

		private static CertificateStatus certificateStatus = CertificateStatus.GOOD;

		public static void setCertificateStatus(
				CertificateStatus certificateStatus) {
			TestOcspResponderServlet.certificateStatus = certificateStatus;
		}

		public static boolean hasBeenCalled() {
			return TestOcspResponderServlet.called;
		}

		@Override
		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			LOG.debug("doPost");
			String contentType = request.getContentType();
			if (false == "application/ocsp-request".equals(contentType)) {
				LOG.error("incorrect content type");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			BasicOCSPRespGenerator basicOCSPRespGenerator;
			try {
				basicOCSPRespGenerator = new BasicOCSPRespGenerator(
						this.ocspResponderCertificate.getPublicKey());
			} catch (OCSPException e) {
				throw new UnavailableException(
						"cound not create basic OCSP response generator");
			}

			OCSPReq ocspReq = new OCSPReq(request.getInputStream());
			Req[] requestList = ocspReq.getRequestList();
			for (Req ocspRequest : requestList) {
				CertificateID certificateID = ocspRequest.getCertID();
				LOG.debug("certificate Id hash algo OID: "
						+ certificateID.getHashAlgOID());
				if (false == CertificateID.HASH_SHA1.equals(certificateID
						.getHashAlgOID())) {
					LOG.debug("only supporting SHA1 hash algo");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
				BigInteger serialNumber = certificateID.getSerialNumber();
				LOG.debug("serial number: " + serialNumber);
				LOG.debug("issuer name hash: "
						+ new String(Hex.encode(certificateID
								.getIssuerNameHash())));
				LOG.debug("issuer key hash: "
						+ new String(Hex.encode(certificateID
								.getIssuerKeyHash())));
				basicOCSPRespGenerator.addResponse(certificateID,
						TestOcspResponderServlet.certificateStatus);
				TestOcspResponderServlet.called = true;
			}

			try {
				BasicOCSPResp basicOCSPResp = basicOCSPRespGenerator.generate(
						"SHA1WITHRSA", this.ocspResponderPrivateKey, null,
						new Date(), BouncyCastleProvider.PROVIDER_NAME);
				OCSPRespGenerator ocspRespGenerator = new OCSPRespGenerator();
				OCSPResp ocspResp = ocspRespGenerator.generate(
						OCSPRespGenerator.SUCCESSFUL, basicOCSPResp);
				response.setContentType("application/ocsp-response");
				response.getOutputStream().write(ocspResp.getEncoded());
			} catch (NoSuchProviderException e) {
				throw new UnavailableException("NoSuchProviderException: "
						+ e.getMessage());
			} catch (IllegalArgumentException e) {
				throw new UnavailableException("IllegalArgumentException: "
						+ e.getMessage());
			} catch (OCSPException e) {
				throw new UnavailableException("OCSPException: "
						+ e.getMessage());
			}
		}

		@Override
		public void destroy() {
			LOG.debug("destroy");
			super.destroy();
		}

		@Override
		public void init() throws ServletException {
			super.init();
			LOG.debug("init");

			KeyPair keyPair;
			try {
				keyPair = PkiTestUtils.generateKeyPair();
			} catch (Exception e) {
				throw new UnavailableException("error: " + e.getMessage());
			}
			DateTime now = new DateTime();
			DateTime notBefore = now.minusDays(1);
			DateTime notAfter = now.plusDays(1);
			try {
				this.ocspResponderCertificate = PkiTestUtils
						.generateSelfSignedCertificate(keyPair,
								"CN=TestOCSPResponder", notBefore, notAfter,
								null, false, true);
			} catch (Exception e) {
				throw new UnavailableException("error: " + e.getMessage());
			}
			this.ocspResponderPrivateKey = keyPair.getPrivate();

			TestOcspResponderServlet.called = false;
		}
	}
}
