/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocolContext;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.servlet.LoginServlet;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.util.servlet.SafeOnlineConfig;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;


public class LoginServletTest {

    private ServletTestManager servletTestManager;

    private static final Log   LOG = LogFactory.getLog(LoginServletTest.class);


    @Before
    public void setUp()
            throws Exception {

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(LoginServlet.class);
        SafeOnlineConfig.load(servletTestManager);
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
        String location = servletTestManager.getServletLocation();
        LOG.debug("servlet location: " + location);

        // operate
        HttpClient httpClient = new HttpClient();
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

    @Test
    public void testHandlerCannotFinalize()
            throws Exception {

        // setup
        String location = servletTestManager.getServletLocation();
        LOG.debug("servlet location: " + location);
        AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
        servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE, mockAuthenticationProtocolHandler);

        // expectations
        expect(
                mockAuthenticationProtocolHandler.finalizeAuthentication((HttpServletRequest) EasyMock.anyObject(),
                        (HttpServletResponse) EasyMock.anyObject())).andReturn(null);

        // prepare
        replay(mockAuthenticationProtocolHandler);

        // operate
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(location);
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(mockAuthenticationProtocolHandler);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, statusCode);
        String responseBody = getMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);
    }

    @Test
    public void testLogin()
            throws Exception {

        // setup
        String location = servletTestManager.getServletLocation();
        LOG.debug("servlet location: " + location);
        AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
        servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE, mockAuthenticationProtocolHandler);
        String target = "http://test.target";
        servletTestManager.setSessionAttribute(AuthenticationProtocolManager.TARGET_ATTRIBUTE, target);
        String userId = UUID.randomUUID().toString();
        String authenticatedDevice = "test-device";
        AuthenticationProtocolContext authenticationProtocolContext = new AuthenticationProtocolContext(userId, authenticatedDevice);

        // expectations
        expect(
                mockAuthenticationProtocolHandler.finalizeAuthentication((HttpServletRequest) EasyMock.anyObject(),
                        (HttpServletResponse) EasyMock.anyObject())).andReturn(authenticationProtocolContext);

        // prepare
        replay(mockAuthenticationProtocolHandler);

        // operate
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(location);
        getMethod.setFollowRedirects(false);
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(mockAuthenticationProtocolHandler);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String responseBody = getMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);
        String resultUserId = (String) servletTestManager.getSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE);
        assertEquals(userId, resultUserId);
        String resultAuthenticatedDevice = (String) servletTestManager
                                                                      .getSessionAttribute(LoginManager.AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE);
        assertEquals(authenticatedDevice, resultAuthenticatedDevice);
        String resultTarget = getMethod.getResponseHeader("Location").getValue();
        assertEquals(target, resultTarget);
    }
}
