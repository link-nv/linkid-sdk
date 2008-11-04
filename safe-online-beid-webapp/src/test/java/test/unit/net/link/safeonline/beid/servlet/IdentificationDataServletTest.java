/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.beid.servlet;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.beid.servlet.IdentificationDataServlet;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class IdentificationDataServletTest {

    private static final Log   LOG = LogFactory.getLog(IdentificationDataServletTest.class);

    private ServletTestManager servletTestManager;


    @Before
    public void setUp()
            throws Exception {

        this.servletTestManager = new ServletTestManager();
        this.servletTestManager.setUp(IdentificationDataServlet.class);
    }

    @After
    public void tearDown()
            throws Exception {

        this.servletTestManager.tearDown();
    }

    @Test
    public void testInvocation()
            throws Exception {

        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(this.servletTestManager.getServletLocation());
        postMethod.addParameter("name", "Doe");
        postMethod.addParameter("firstname", "John");
        postMethod.addParameter("dob", "01/01/1970");
        postMethod.addParameter("nationality", "Belg");
        postMethod.addParameter("sex", "M");
        postMethod.addParameter("name", "John");
        postMethod.addParameter("street", "test 1234 /1");
        postMethod.addParameter("city", "Gent");
        postMethod.addParameter("zip", "9000");
        postMethod.addParameter("nnr", "123456789");

        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_OK, statusCode);

        String sessionStreet = (String) this.servletTestManager.getSessionAttribute(IdentificationDataServlet.STREET_SESSION_ATTRIBUTE);
        LOG.debug("session street: " + sessionStreet);
        assertEquals("test", sessionStreet);
        String sessionHouseNr = (String) this.servletTestManager.getSessionAttribute(IdentificationDataServlet.HOUSE_NR_SESSION_ATTRIBUTE);
        assertEquals("1234 /1", sessionHouseNr);
    }
}
