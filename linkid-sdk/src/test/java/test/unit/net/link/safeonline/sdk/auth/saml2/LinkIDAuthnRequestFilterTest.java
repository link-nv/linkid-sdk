/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static net.link.safeonline.sdk.configuration.LinkIDTestConfigHolder.testConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.security.cert.X509Certificate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.filter.LinkIDAuthnRequestFilter;
import net.link.safeonline.sdk.configuration.LinkIDTestConfigHolder;
import net.link.util.config.KeyProviderImpl;
import net.link.util.test.j2ee.TestClassLoader;
import net.link.util.test.pkix.PkiTestUtils;
import net.link.util.test.web.ContainerSetup;
import net.link.util.test.web.FilterSetup;
import net.link.util.test.web.ServletSetup;
import net.link.util.test.web.ServletTestManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class LinkIDAuthnRequestFilterTest {

    private ServletTestManager servletTestManager;

    private ClassLoader originalContextClassLoader;

    @Before
    public void setUp()
            throws Exception {

        originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        TestClassLoader testClassLoader = new TestClassLoader();
        Thread.currentThread().setContextClassLoader( testClassLoader );
        servletTestManager = new ServletTestManager();
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
        Thread.currentThread().setContextClassLoader( originalContextClassLoader );
    }

    public static class TestServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException {

            throw new UnsupportedOperationException( "should never get called" );
        }
    }

    @Test
    public void performSaml2AuthnRequest()
            throws Exception {

        // Setup Data
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( TestServlet.class ), new FilterSetup( LinkIDAuthnRequestFilter.class ) ) );

        new LinkIDTestConfigHolder( servletTestManager.createSocketConnector(), servletTestManager.getServletContext() ).install();
        testConfig().linkID().app().keyProvider = new KeyProviderImpl( PkiTestUtils.generateKeyEntry( "CN=TestApplication" ),
                ImmutableMap.<String, X509Certificate>of() );

        // Test
        GetMethod getMethod = new GetMethod( servletTestManager.getServletLocation() );
        int statusCode = new HttpClient().executeMethod( getMethod );

        // Verify
        assertEquals( HttpStatus.SC_OK, statusCode );
    }

    @Test
    public void performSaml2AuthnRequestWithCustomTemplate()
            throws Exception {

        // Setup Data
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( TestServlet.class ), new FilterSetup( LinkIDAuthnRequestFilter.class ) ) );

        new LinkIDTestConfigHolder( servletTestManager.createSocketConnector(), servletTestManager.getServletContext() ).install();
        testConfig().proto().saml().postBindingTemplate = "/test-saml2-post-binding.vm";
        testConfig().linkID().app().keyProvider = new KeyProviderImpl( PkiTestUtils.generateKeyEntry( "CN=TestApplication" ),
                ImmutableMap.<String, X509Certificate>of() );

        GetMethod getMethod = new GetMethod( servletTestManager.getServletLocation() );
        HttpClient httpClient = new HttpClient();

        // Test
        int statusCode = httpClient.executeMethod( getMethod );

        // Verify
        assertEquals( HttpStatus.SC_OK, statusCode );
        String response = getMethod.getResponseBodyAsString();
        assertTrue( response.contains( "custom template" ) );
    }
}
