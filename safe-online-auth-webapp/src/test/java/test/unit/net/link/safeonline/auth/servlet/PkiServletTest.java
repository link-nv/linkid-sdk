/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.servlet.PkiServlet;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.openssl.PEMReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PkiServletTest {

    private static final Log   LOG = LogFactory.getLog(PkiServletTest.class);

    private ServletTestManager servletTestManager;

    private JmxTestUtils       jmxTestUtils;

    private X509Certificate    certificate;


    @Before
    public void setUp()
            throws Exception {

        jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        GetCertificateMBeanActionHandler actionHandler = new GetCertificateMBeanActionHandler(certificate);
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate", actionHandler);

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(PkiServlet.class);
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
        jmxTestUtils.tearDown();
    }


    static class GetCertificateMBeanActionHandler implements MBeanActionHandler {

        private final X509Certificate certificate;


        public GetCertificateMBeanActionHandler(X509Certificate certificate) {

            this.certificate = certificate;
        }

        public Object invoke(Object[] arguments) {

            return certificate;
        }
    }


    @Test
    public void testGetCertificate()
            throws Exception {

        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(servletTestManager.getServletLocation());

        int statusCode = httpClient.executeMethod(getMethod);

        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_OK, statusCode);
        String responseBody = getMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);

        StringReader stringReader = new StringReader(responseBody);
        PEMReader pemReader = new PEMReader(stringReader);
        Object obj = pemReader.readObject();
        LOG.debug("obj class: " + obj.getClass().getName());
        assertTrue(obj instanceof X509Certificate);
        X509Certificate resultCertificate = (X509Certificate) obj;
        assertEquals(certificate, resultCertificate);
    }
}
