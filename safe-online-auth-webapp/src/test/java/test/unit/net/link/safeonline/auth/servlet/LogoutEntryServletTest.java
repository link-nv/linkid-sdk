/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;

import net.link.safeonline.auth.protocol.LogoutServiceManager;
import net.link.safeonline.auth.servlet.LogoutEntryServlet;
import net.link.safeonline.auth.webapp.pages.UnsupportedProtocolPage;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.core.LogoutRequest;


public class LogoutEntryServletTest {

    private static final Log   LOG            = LogFactory.getLog(LogoutEntryServletTest.class);

    private ServletTestManager logoutEntryServletTestManager;

    private String             logoutExitPath = "logout-exit";

    private String             cookiePath     = "/test-path/";

    private JndiTestUtils      jndiTestUtils;

    private LogoutService      mockLogoutService;

    private Object[]           mockObjects;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockLogoutService = createMock(LogoutService.class);
        jndiTestUtils.bindComponent(LogoutService.JNDI_BINDING, mockLogoutService);

        logoutEntryServletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(LogoutEntryServlet.LOGOUT_EXIT_PATH, logoutExitPath);
        initParams.put(LogoutEntryServlet.COOKIE_PATH, cookiePath);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(LogoutServiceManager.LOGOUT_SERVICE_ATTRIBUTE, mockLogoutService);

        logoutEntryServletTestManager.setUp(LogoutEntryServlet.class, initParams, null, null, initialSessionAttributes);

        mockObjects = new Object[] { mockLogoutService };

        SafeOnlineTestConfig.loadTest(logoutEntryServletTestManager);

    }

    @After
    public void tearDown()
            throws Exception {

        logoutEntryServletTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void unsupportedProtocol()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(logoutEntryServletTestManager.getServletLocation());
        /*
         * Here we simulate a user that directly visits the authentication web application.
         */
        getMethod.setFollowRedirects(false);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = getMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + location);
        assertTrue(location.endsWith(UnsupportedProtocolPage.PATH));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void saml2LogoutProtocol()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = logoutEntryServletTestManager.getServletLocation();
        PostMethod postMethod = new PostMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String userId = UUID.randomUUID().toString();
        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, applicationName, applicationKeyPair, servletLocation,
                null, null);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlLogoutRequest) };
        postMethod.setRequestBody(data);
        postMethod.addRequestHeader("Cookie", SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + applicationName + "=value");

        // expectations
        expect(mockLogoutService.initialize((LogoutRequest) EasyMock.anyObject())).andStubReturn(
                new LogoutProtocolContext(applicationName, servletLocation));
        mockLogoutService.logout((List<Cookie>) EasyMock.anyObject());
        expect(mockLogoutService.getInvalidCookies()).andReturn(new LinkedList<Cookie>());

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        LOG.debug("result body: " + postMethod.getResponseBodyAsString());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + location);
        assertTrue(location.endsWith(logoutExitPath));
    }
}
