/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.filter;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.AuthnResponseFilter;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AuthnResponseFilterTest {

    private static final Log              LOG = LogFactory.getLog(AuthnResponseFilterTest.class);

    private ServletTestManager            servletTestManager;

    private AuthenticationProtocolHandler mockProtocolHandler;


    @Before
    public void setUp() throws Exception {

        this.servletTestManager = new ServletTestManager();

        this.mockProtocolHandler = createMock(AuthenticationProtocolHandler.class);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes
                .put(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE, this.mockProtocolHandler);
        this.servletTestManager
                .setUp(LoginTestServlet.class, AuthnResponseFilter.class, null, initialSessionAttributes);
    }


    public static class LoginTestServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        private static final Log  ltLOG            = LogFactory.getLog(LoginTestServlet.class);


        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) {

            ltLOG.debug("doGet");
        }
    }


    @After
    public void tearDown() throws Exception {

        this.servletTestManager.tearDown();
    }

    @Test
    public void normalRequestPasses() throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(this.servletTestManager.getServletLocation());

        // expectations
        expect(
                this.mockProtocolHandler.finalizeAuthentication((HttpServletRequest) anyObject(),
                        (HttpServletResponse) anyObject())).andReturn(null);

        // prepare
        replay(this.mockProtocolHandler);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(this.mockProtocolHandler);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_OK, statusCode);
    }

    @Test
    public void canLogin() throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(this.servletTestManager.getServletLocation());

        // expectations
        String userId = UUID.randomUUID().toString();
        expect(
                this.mockProtocolHandler.finalizeAuthentication((HttpServletRequest) anyObject(),
                        (HttpServletResponse) anyObject())).andReturn(userId);

        // prepare
        replay(this.mockProtocolHandler);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        assertEquals(HttpStatus.SC_OK, statusCode);
        String resultUserId = (String) this.servletTestManager
                .getSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE);
        LOG.debug("result userId: " + resultUserId);
        assertEquals(userId, resultUserId);
    }
}
