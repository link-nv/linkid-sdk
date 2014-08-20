/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.filter;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.auth.filter.AuthnResponseFilter;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolRequestContext;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolHandler;
import net.link.safeonline.sdk.configuration.Protocol;
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


public class AuthnResponseFilterTest {

    private ServletTestManager servletTestManager;

    private ProtocolHandler             mockProtocolHandler;
    private AuthnProtocolRequestContext authnRequest;

    @Before
    public void setUp()
            throws Exception {

        servletTestManager = new ServletTestManager();
        HashMap<String, ProtocolContext> contexts = new HashMap<String, ProtocolContext>();

        mockProtocolHandler = createMock( ProtocolHandler.class );
        authnRequest = new AuthnProtocolRequestContext( UUID.randomUUID().toString(), "test-application", mockProtocolHandler, null, false );
        contexts.put( authnRequest.getId(), authnRequest );
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( LoginTestServlet.class ), //
                new FilterSetup( AuthnResponseFilter.class ) ) //
                .addSessionAttribute( ProtocolContext.SESSION_CONTEXTS, contexts ) );
        servletTestManager.createSocketConnector();
    }

    public static class LoginTestServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        }
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void normalRequestPasses()
            throws Exception {

        // Setup Data
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod( servletTestManager.getServletLocation() );

        // Setup Mocks
        //noinspection unchecked
        expect( mockProtocolHandler.findAndValidateAuthnResponse( (HttpServletRequest) anyObject() ) ).andReturn( null );
        replay( mockProtocolHandler );

        // Test
        int statusCode = httpClient.executeMethod( getMethod );

        // Verify
        assertEquals( HttpStatus.SC_OK, statusCode );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canLogin()
            throws Exception {

        // Setup Data
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod( servletTestManager.getServletLocation() );

        // Setup Mocks
        String userId = UUID.randomUUID().toString();
        AuthnProtocolResponseContext authnResponse = new AuthnProtocolResponseContext( authnRequest, UUID.randomUUID().toString(), userId, null,
                new HashMap<String, List<AttributeSDK<Serializable>>>(), true, null, null );
        expect( mockProtocolHandler.findAndValidateAuthnResponse( (HttpServletRequest) anyObject() ) ).andReturn( authnResponse );
        expect( mockProtocolHandler.getProtocol() ).andReturn( Protocol.SAML2 );
        replay( mockProtocolHandler );

        // Test
        int statusCode = httpClient.executeMethod( getMethod );

        // Verify
        assertEquals( HttpStatus.SC_OK, statusCode );
        String resultUserId = (String) servletTestManager.getSessionAttribute( LoginManager.USERID_SESSION_ATTRIBUTE );
        assertEquals( userId, resultUserId );
    }
}
