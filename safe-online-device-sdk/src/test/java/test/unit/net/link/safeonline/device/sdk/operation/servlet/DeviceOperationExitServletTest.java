/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.device.sdk.operation.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.operation.servlet.AbstractDeviceOperationExitServlet;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;


public class DeviceOperationExitServletTest {

    private static final Log   LOG    = LogFactory.getLog(DeviceOperationExitServletTest.class);

    private ServletTestManager servletTestManager;

    private HttpClient         httpClient;

    private String             location;

    String                     userId = UUID.randomUUID().toString();


    @Before
    public void setUp()
            throws Exception {

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(TestDeviceOperationExitServlet.class);
        location = servletTestManager.getServletLocation();
        httpClient = new HttpClient();

        SafeOnlineTestConfig.loadTest(servletTestManager);
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void testNoProtocolHandler()
            throws Exception {

        // setup

        // operate
        GetMethod getMethod = new GetMethod(location);
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, statusCode);
        InputStream resultStream = getMethod.getResponseBodyAsStream();

        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        Document resultDocument = tidy.parseDOM(resultStream, null);
        LOG.debug("result document: " + DomTestUtils.domToString(resultDocument));
        Node h1Node = XPathAPI.selectSingleNode(resultDocument, "//h1/text()");
        assertNotNull(h1Node);
        assertEquals("Error(s)", h1Node.getNodeValue());
    }


    public static class TestDeviceOperationExitServlet extends AbstractDeviceOperationExitServlet {

        private static final long serialVersionUID = 1L;

    }
}