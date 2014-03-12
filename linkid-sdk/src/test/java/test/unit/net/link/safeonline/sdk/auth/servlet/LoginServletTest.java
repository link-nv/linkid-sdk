/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.servlet;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import com.google.common.base.Function;
import java.io.InputStream;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.auth.servlet.LoginServlet;
import net.link.safeonline.sdk.configuration.*;
import net.link.util.test.web.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.xpath.XPathAPI;
import org.easymock.EasyMock;
import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;


public class LoginServletTest {

    private ServletTestManager servletTestManager;

    private String                           servletLocation;
    private ProtocolHandler                  mockProtocolHandler;
    private HashMap<String, ProtocolContext> contexts;

    private String                      target;
    private AuthnProtocolRequestContext authnRequest;

    @Before
    public void setUp()
            throws Exception {

        contexts = new HashMap<String, ProtocolContext>();
        authnRequest = new AuthnProtocolRequestContext( UUID.randomUUID().toString(), "test-application",
                mockProtocolHandler = createMock( ProtocolHandler.class ), target = "http://test.target", false, false, false );
        contexts.put( authnRequest.getId(), authnRequest );

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( LoginServlet.class ) ) //
                .addSessionAttribute( ProtocolContext.SESSION_CONTEXTS, contexts ) );

        new TestConfigHolder( servletTestManager.createSocketConnector(), servletTestManager.getServletContext() ).install();
        servletLocation = servletTestManager.getServletLocation();
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void testNoProtocolHandler()
            throws Exception {

        // Setup Data
        contexts.clear();

        // Test
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod( servletLocation );
        int statusCode = httpClient.executeMethod( getMethod );

        // Verify
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, statusCode );
        InputStream resultStream = getMethod.getResponseBodyAsStream();

        Tidy tidy = new Tidy();
        tidy.setQuiet( true );
        tidy.setShowWarnings( false );
        Document resultDocument = tidy.parseDOM( resultStream, null );
        Node h1Node = XPathAPI.selectSingleNode( resultDocument, "//h1/text()" );
        assertNotNull( h1Node );
        assertEquals( "Error(s)", h1Node.getNodeValue() );
    }

    @Test
    public void testHandlerCannotFinalize()
            throws Exception {

        // Setup Mocks
        //noinspection unchecked
        expect( mockProtocolHandler.findAndValidateAuthnResponse( (HttpServletRequest) EasyMock.anyObject(),
                (Function<AuthnProtocolResponseContext, AuthenticationContext>) anyObject() ) ).andReturn( null );
        replay( mockProtocolHandler );

        // Test
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod( servletLocation );
        int statusCode = httpClient.executeMethod( getMethod );

        // Verify
        verify( mockProtocolHandler );
        assertEquals( HttpServletResponse.SC_BAD_REQUEST, statusCode );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLogin()
            throws Exception {

        // Setup Data
        String userId = UUID.randomUUID().toString();
        String authenticatedDevice = "test-device";
        AuthnProtocolResponseContext authnResponse = new AuthnProtocolResponseContext( authnRequest, UUID.randomUUID().toString(), userId, null,
                Collections.singletonList( authenticatedDevice ), new HashMap<String, List<AttributeSDK<?>>>(), true, null, null );

        // Setup Mocks
        expect( mockProtocolHandler.findAndValidateAuthnResponse( (HttpServletRequest) anyObject(),
                (Function<AuthnProtocolResponseContext, AuthenticationContext>) anyObject() ) ).andReturn( authnResponse );
        expect( mockProtocolHandler.getProtocol() ).andReturn( Protocol.SAML2 );
        replay( mockProtocolHandler );

        // Test
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod( servletLocation );
        getMethod.setFollowRedirects( false );
        int statusCode = httpClient.executeMethod( getMethod );

        // Verify
        verify( mockProtocolHandler );
        assertEquals( HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode );
        String resultUserId = (String) servletTestManager.getSessionAttribute( LoginManager.USERID_SESSION_ATTRIBUTE );
        assertEquals( userId, resultUserId );
        List<String> resultAuthenticatedDevices = (List<String>) servletTestManager.getSessionAttribute( LoginManager.AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE );
        assertTrue( resultAuthenticatedDevices.contains( authenticatedDevice ) );
        String resultTarget = getMethod.getResponseHeader( "Location" ).getValue();
        assertEquals( target, resultTarget );
    }
}
