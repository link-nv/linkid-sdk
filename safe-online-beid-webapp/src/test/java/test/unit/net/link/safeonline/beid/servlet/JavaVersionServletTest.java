/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.beid.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.beid.servlet.JavaVersionServlet;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class JavaVersionServletTest {

    private static final Log   LOG = LogFactory.getLog(JavaVersionServletTest.class);

    private ServletTestManager servletTestManager;

    private String             location;


    @Before
    public void setUp()
            throws Exception {

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(JavaVersionServlet.class);
        location = servletTestManager.getServletLocation();
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void testLinux()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(location);
        postMethod.addParameter("platform", "Linux i686");
        postMethod.addParameter("javaEnabled", "true");
        postMethod.addParameter("javaVersion", "1.6.0_04");

        // operate
        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        assertEquals(JavaVersionServlet.PLATFORM.LINUX, servletTestManager.getSessionAttribute("platform"));
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("result location: " + resultLocation);
        assertEquals(location + "beid-applet.seam", resultLocation);
    }

    @Test
    public void testWindows()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(location);
        postMethod.addParameter("platform", "Win32");
        postMethod.addParameter("javaEnabled", "true");
        postMethod.addParameter("javaVersion", "1.6.0_04");

        // operate
        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        assertEquals(JavaVersionServlet.PLATFORM.WINDOWS, servletTestManager.getSessionAttribute("platform"));
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("result location: " + resultLocation);
        assertEquals(location + "beid-applet.seam", resultLocation);
    }

    @Test
    public void testMacIntel()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(location);
        postMethod.addParameter("platform", "MacIntel");
        postMethod.addParameter("javaEnabled", "true");
        postMethod.addParameter("javaVersion", "1.6.0_04");

        // operate
        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        assertEquals(JavaVersionServlet.PLATFORM.MAC, servletTestManager.getSessionAttribute("platform"));
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("result location: " + resultLocation);
        assertEquals(location + "beid-applet.seam", resultLocation);
    }

    @Test
    public void testUnsupportedPlatform()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(location);
        postMethod.addParameter("platform", "GameBoy");
        postMethod.addParameter("javaEnabled", "true");
        postMethod.addParameter("javaVersion", "1.6.0_04");

        // operate
        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        assertEquals(JavaVersionServlet.PLATFORM.UNSUPPORTED, servletTestManager.getSessionAttribute("platform"));
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("result location: " + resultLocation);
        assertEquals(location + "unsupported-platform.seam", resultLocation);
    }

    @Test
    public void testWindowsJavaDisabled()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(location);
        postMethod.addParameter("platform", "Win32");
        postMethod.addParameter("javaEnabled", "false");
        postMethod.addParameter("javaVersion", "1.6.0_04");

        // operate
        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        assertEquals(JavaVersionServlet.PLATFORM.WINDOWS, servletTestManager.getSessionAttribute("platform"));
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("result location: " + resultLocation);
        assertEquals(location + "java-disabled.seam", resultLocation);
    }

    @Test
    public void testWindowsJava1_4()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(location);
        postMethod.addParameter("platform", "Win32");
        postMethod.addParameter("javaEnabled", "true");
        postMethod.addParameter("javaVersion", "1.4.1");

        // operate
        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        assertEquals(JavaVersionServlet.PLATFORM.WINDOWS, servletTestManager.getSessionAttribute("platform"));
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("result location: " + resultLocation);
        assertEquals(location + "java-version.seam", resultLocation);
    }

    @Test
    public void testRegExpression()
            throws Exception {

        assertTrue(Pattern.matches(JavaVersionServlet.JAVA_VERSION_REG_EXPR, "1.6.0_04"));
        assertTrue(Pattern.matches(JavaVersionServlet.JAVA_VERSION_REG_EXPR, "1.5.0_12"));
        assertFalse(Pattern.matches(JavaVersionServlet.JAVA_VERSION_REG_EXPR, "1.4.1"));
    }
}
