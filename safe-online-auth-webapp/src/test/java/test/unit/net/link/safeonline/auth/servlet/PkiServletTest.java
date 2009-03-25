/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.servlet.PkiServlet;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;
import net.link.safeonline.test.util.ServletTestManager;

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

    private KeyService         mockKeyService;

    private JndiTestUtils      jndiTestUtils;

    private X509Certificate    nodeCertificate;


    @Before
    public void setUp()
            throws Exception {

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(PkiServlet.class);

        SafeOnlineTestConfig.loadTest(servletTestManager);

    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
        jndiTestUtils.tearDown();
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
        assertEquals(nodeCertificate, resultCertificate);
    }
}
