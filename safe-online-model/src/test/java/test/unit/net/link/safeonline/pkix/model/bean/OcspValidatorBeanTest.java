/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.model.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.pkix.model.bean.OcspValidatorBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.bouncycastle.util.encoders.Hex;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class OcspValidatorBeanTest {

    private OcspValidatorBean   testedInstance;

    private URI                 ocspUri;

    private KeyPair             caKeyPair;

    private X509Certificate     caCertificate;

    private ServletTestManager  servletTestManager;

    private ResourceAuditLogger mockResourceAuditLogger;

    private Object[]            mockObjects;


    @Before
    public void setUp()
            throws Exception {

        testedInstance = new OcspValidatorBean();

        servletTestManager = new ServletTestManager();

        servletTestManager.setUp(TestOcspResponderServlet.class);

        ocspUri = new URI(servletTestManager.getServletLocation());

        caKeyPair = PkiTestUtils.generateKeyPair();
        caCertificate = PkiTestUtils.generateSelfSignedCertificate(caKeyPair, "CN=TestCA");

        KeyPair ocspResponderKeyPair = PkiTestUtils.generateKeyPair();

        TestOcspResponderServlet.certificate = PkiTestUtils.generateCertificate(ocspResponderKeyPair.getPublic(), "CN=TestOCSPResponder",
                caKeyPair.getPrivate(), caCertificate, new DateTime(caCertificate.getNotBefore()),
                new DateTime(caCertificate.getNotAfter()), null, true, false, false, null);
        TestOcspResponderServlet.privateKey = ocspResponderKeyPair.getPrivate();

        TestOcspResponderServlet.called = false;

        mockResourceAuditLogger = createMock(ResourceAuditLogger.class);
        mockObjects = new Object[] { mockResourceAuditLogger };
        EJBTestUtils.inject(testedInstance, mockResourceAuditLogger);
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void testGetOcspUri()
            throws Exception {

        // setup
        URI testOcspUri = new URI("http://test.ocsp.location/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(testOcspUri);

        // operate
        URI resultOcspUri = testedInstance.getOcspUri(certificate);

        // verify
        assertEquals(testOcspUri, resultOcspUri);
    }

    @Test
    public void testGetOcspUriGivesNullOnMissingOcspAccessLocation()
            throws Exception {

        // setup
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(null);

        // operate
        URI resultOcspUri = testedInstance.getOcspUri(certificate);

        // verify
        assertNull(resultOcspUri);
    }

    @Test
    public void testPerformOcspCheckFailsIfOcspResponderIsDown()
            throws Exception {

        // setup
        URI testOcspUri = new URI("http://localhost:1/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(testOcspUri);

        // expectations
        mockResourceAuditLogger.addResourceAudit(ResourceNameType.OCSP, ResourceLevelType.RESOURCE_UNAVAILABLE, "/",
                "OCSP Responder is down");

        // prepare
        replay(mockObjects);

        // operate
        boolean result = testedInstance.performOcspCheck(certificate, certificate);

        // verify
        verify(mockObjects);
        assertFalse(result);
    }

    @Test
    public void testPerformOcspCheckFailsIfOcspResponderDoesNotExist()
            throws Exception {

        // setup
        URI testOcspUri = new URI("http://foobar.ocsp.responder/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(testOcspUri);

        // operate
        boolean result = testedInstance.performOcspCheck(certificate, certificate);

        // verify
        assertFalse(result);
    }

    @Test
    public void testPerformOcspCheck()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", caKeyPair.getPrivate(),
                caCertificate, new DateTime(caCertificate.getNotBefore()), new DateTime(caCertificate.getNotAfter()), null, true, false,
                false, ocspUri);

        // operate
        boolean result = testedInstance.performOcspCheck(certificate, caCertificate);

        // verify
        assertTrue(result);
        assertTrue(TestOcspResponderServlet.hasBeenCalled());
    }


    public static class TestOcspResponderServlet extends HttpServlet {

        private static final long        serialVersionUID  = 1L;

        private static final Log         LOG               = LogFactory.getLog(TestOcspResponderServlet.class);

        static X509Certificate           certificate;

        static PrivateKey                privateKey;

        static boolean                   called;

        private static CertificateStatus certificateStatus = CertificateStatus.GOOD;


        public static void setCertificateStatus(CertificateStatus certificateStatus) {

            TestOcspResponderServlet.certificateStatus = certificateStatus;
        }

        public static boolean hasBeenCalled() {

            return TestOcspResponderServlet.called;
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            LOG.debug("doPost");
            String contentType = request.getContentType();
            if (false == "application/ocsp-request".equals(contentType)) {
                LOG.error("incorrect content type");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            BasicOCSPRespGenerator basicOCSPRespGenerator;
            try {
                basicOCSPRespGenerator = new BasicOCSPRespGenerator(TestOcspResponderServlet.certificate.getPublicKey());
            } catch (OCSPException e) {
                throw new UnavailableException("cound not create basic OCSP response generator");
            }

            OCSPReq ocspReq = new OCSPReq(request.getInputStream());
            Req[] requestList = ocspReq.getRequestList();
            for (Req ocspRequest : requestList) {
                CertificateID certificateID = ocspRequest.getCertID();
                LOG.debug("certificate Id hash algo OID: " + certificateID.getHashAlgOID());
                if (false == CertificateID.HASH_SHA1.equals(certificateID.getHashAlgOID())) {
                    LOG.debug("only supporting SHA1 hash algo");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                BigInteger serialNumber = certificateID.getSerialNumber();
                LOG.debug("serial number: " + serialNumber);
                LOG.debug("issuer name hash: " + new String(Hex.encode(certificateID.getIssuerNameHash())));
                LOG.debug("issuer key hash: " + new String(Hex.encode(certificateID.getIssuerKeyHash())));
                basicOCSPRespGenerator.addResponse(certificateID, TestOcspResponderServlet.certificateStatus);
                TestOcspResponderServlet.called = true;
            }

            try {
                BasicOCSPResp basicOCSPResp = basicOCSPRespGenerator.generate("SHA1WITHRSA", TestOcspResponderServlet.privateKey,
                        new X509Certificate[] { TestOcspResponderServlet.certificate }, new Date(), BouncyCastleProvider.PROVIDER_NAME);
                OCSPRespGenerator ocspRespGenerator = new OCSPRespGenerator();
                OCSPResp ocspResp = ocspRespGenerator.generate(OCSPRespGenerator.SUCCESSFUL, basicOCSPResp);
                response.setContentType("application/ocsp-response");
                response.getOutputStream().write(ocspResp.getEncoded());
            } catch (NoSuchProviderException e) {
                throw new UnavailableException("NoSuchProviderException: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new UnavailableException("IllegalArgumentException: " + e.getMessage());
            } catch (OCSPException e) {
                throw new UnavailableException("OCSPException: " + e.getMessage());
            }
        }

        @Override
        public void destroy() {

            LOG.debug("destroy");
            super.destroy();
        }

        @Override
        public void init()
                throws ServletException {

            super.init();
            LOG.debug("init");

            TestOcspResponderServlet.called = false;
        }
    }
}
