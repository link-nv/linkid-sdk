/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.webapp.filter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.service.AuthorizationService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.webapp.filter.ServletLoginFilter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ServletLoginFilterTest extends TestCase {

    private static final Log     LOG = LogFactory.getLog(ServletLoginFilterTest.class);

    private ServletTestManager   servletTestManager;

    private JndiTestUtils        jndiTestUtils;

    private String               userId;

    private AuthorizationService mockAuthorizationService;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        mockAuthorizationService = createMock(AuthorizationService.class);
        jndiTestUtils.bindComponent("SafeOnline/AuthorizationServiceBean/local", mockAuthorizationService);

        userId = UUID.randomUUID().toString();
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(ServletLoginFilterTestServlet.class, ServletLoginFilter.class, null, initialSessionAttributes);

        ServletLoginFilterTestServlet.reset();
    }

    @Override
    protected void tearDown()
            throws Exception {

        servletTestManager.tearDown();
        jndiTestUtils.tearDown();

        super.tearDown();
    }

    public void testServletContainerLogin()
            throws Exception {

        // setup
        String testExpectedRole = "test-role-" + getName();
        ServletLoginFilterTestServlet.addExpectedRole(testExpectedRole);

        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(servletTestManager.getServletLocation());

        // stubs
        expect(mockAuthorizationService.getRoles(userId)).andStubReturn(Collections.singleton(testExpectedRole));
        replay(mockAuthorizationService);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(mockAuthorizationService);
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(ServletLoginFilterTestServlet.isInvoked());
        LOG.debug("last user principal: " + ServletLoginFilterTestServlet.getLastUserPrincipal());
        assertNotNull(ServletLoginFilterTestServlet.getLastUserPrincipal());
        assertEquals(userId, ServletLoginFilterTestServlet.getLastUserPrincipal().getName());
        assertTrue(ServletLoginFilterTestServlet.isExpectedRolePresent(testExpectedRole));
        assertFalse(ServletLoginFilterTestServlet.isExpectedRolePresent(testExpectedRole + "-not-expected"));
    }


    public static class ServletLoginFilterTestServlet extends HttpServlet {

        private static final long           serialVersionUID = 1L;

        private static final Log            testServletLOG   = LogFactory.getLog(ServletLoginFilterTestServlet.class);

        private static boolean              invoked;

        private static Principal            lastUserPrincipal;

        private static Map<String, Boolean> expectedRoles;


        public static void reset() {

            ServletLoginFilterTestServlet.invoked = false;
            ServletLoginFilterTestServlet.lastUserPrincipal = null;
            ServletLoginFilterTestServlet.expectedRoles = new HashMap<String, Boolean>();
        }

        public static void addExpectedRole(String expectedRole) {

            ServletLoginFilterTestServlet.expectedRoles.put(expectedRole, false);
        }

        public static boolean isExpectedRolePresent(String expectedRole) {

            Boolean expectedRolePresent = ServletLoginFilterTestServlet.expectedRoles.get(expectedRole);
            if (null == expectedRolePresent)
                return false;
            return expectedRolePresent;
        }

        public static boolean isInvoked() {

            return ServletLoginFilterTestServlet.invoked;
        }

        public static Principal getLastUserPrincipal() {

            return ServletLoginFilterTestServlet.lastUserPrincipal;
        }

        @Override
        protected void doGet(HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {

            testServletLOG.debug("doGet");
            ServletLoginFilterTestServlet.invoked = true;
            Principal principal = request.getUserPrincipal();
            testServletLOG.debug("user principal: " + principal);
            ServletLoginFilterTestServlet.lastUserPrincipal = principal;

            for (String expectedRole : ServletLoginFilterTestServlet.expectedRoles.keySet()) {
                boolean rolePresent = request.isUserInRole(expectedRole);
                ServletLoginFilterTestServlet.expectedRoles.put(expectedRole, rolePresent);
            }
        }
    }
}
