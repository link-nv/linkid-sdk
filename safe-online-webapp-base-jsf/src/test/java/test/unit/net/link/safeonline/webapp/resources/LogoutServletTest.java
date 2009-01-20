/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.webapp.resources;

import java.util.Collections;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.webapp.resources.LogoutServlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LogoutServletTest extends TestCase {

    private static final Log   LOG           = LogFactory.getLog(LogoutServletTest.class);

    private ServletTestManager servletTestManager;

    private String             servletLocation;

    private String             logoutExitUrl = "logoutexit";


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(LogoutServlet.class, Collections.singletonMap("LogoutExitUrl", logoutExitUrl), null, null,
                Collections.singletonMap(LoginManager.USERID_SESSION_ATTRIBUTE, (Object) UUID.randomUUID().toString()));
        servletLocation = servletTestManager.getServletLocation();
    }

    @Override
    protected void tearDown()
            throws Exception {

        servletTestManager.tearDown();

        super.tearDown();
    }

    public void testDoGet()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(servletLocation);
        getMethod.setFollowRedirects(false);

        // operate
        int result = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("result: " + result);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, result);
        String resultLocation = getMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(logoutExitUrl));

        String resultUserId = (String) servletTestManager.getSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE);
        assertNull(resultUserId);
    }
}
