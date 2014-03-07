/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static net.link.safeonline.sdk.configuration.TestConfigHolder.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableMap;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.auth.protocol.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.servlet.LogoutServlet;
import net.link.safeonline.sdk.configuration.TestConfigHolder;
import net.link.util.common.DomUtils;
import net.link.util.config.KeyProviderImpl;
import net.link.util.test.pkix.PkiTestUtils;
import net.link.util.test.web.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.apache.xpath.XPathAPI;
import org.junit.*;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.*;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;


public class LogoutServletTest {

    private static final Log LOG = LogFactory.getLog( LogoutServletTest.class );

    private ServletTestManager servletTestManager;

    private static final String applicationName = "test-application-id";

    private String servletLocation;

    private HttpClient                   httpClient;
    private LogoutProtocolRequestContext logoutRequest;
    private ProtocolHandler              mockProtocolHandler;
    private String                       target;
    private KeyPair                      keyPair;

    @Before
    public void setUp()
            throws Exception {

        servletTestManager = new ServletTestManager();

        HashMap<String, ProtocolContext> contexts = new HashMap<String, ProtocolContext>();
        logoutRequest = new LogoutProtocolRequestContext( UUID.randomUUID().toString(), "test-application",
                mockProtocolHandler = createMock( ProtocolHandler.class ), target = "http://test.target", UUID.randomUUID().toString() );
        contexts.put( logoutRequest.getId(), logoutRequest );

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( LogoutServlet.class ) ) //
                .addSessionAttribute( ProtocolContext.SESSION_CONTEXTS, contexts ) );

        new TestConfigHolder( servletTestManager.createSocketConnector(), servletTestManager.getServletContext() ).install();
        keyPair = PkiTestUtils.generateKeyPair();
        testConfig().linkID().app().keyProvider = new KeyProviderImpl(
                new KeyStore.PrivateKeyEntry( keyPair.getPrivate(), new Certificate[] {
                        PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=TestApplication" )
                } ), ImmutableMap.<String, X509Certificate>of() );

        servletLocation = servletTestManager.getServletLocation();
        httpClient = new HttpClient();
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void testPostFinalizeSingleLogout()
            throws Exception {

        // Setup Data

        // Setup Mocks
        LogoutProtocolResponseContext logoutResponse = new LogoutProtocolResponseContext( logoutRequest, UUID.randomUUID().toString(), true,
                null );
        expect( mockProtocolHandler.findAndValidateLogoutResponse( (HttpServletRequest) anyObject() ) ).andStubReturn( logoutResponse );
        replay( mockProtocolHandler );

        // Test
        PostMethod postMethod = new PostMethod( servletLocation );
        int statusCode = httpClient.executeMethod( postMethod );

        // Verify
        LOG.debug( "status code: " + statusCode );
        assertEquals( HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode );
        String resultTarget = postMethod.getResponseHeader( "Location" ).getValue();
        assertTrue( resultTarget.endsWith( target ) );
    }

    @Test
    public void testPostHandleSaml2LogoutRequest()
            throws Exception {

        // Setup Data
        String userId = UUID.randomUUID().toString();
        LogoutRequest samlLogoutRequest = LogoutRequestFactory.createLogoutRequest( userId, applicationName, servletLocation, null );
        String encodedSamlLogoutRequest = Base64.encode(
                DomUtils.domToString( LinkIDSaml2Utils.sign( samlLogoutRequest, keyPair, null ) ).getBytes() );

        servletTestManager.setSessionAttribute( LoginManager.USERID_SESSION_ATTRIBUTE, userId );

        PostMethod postMethod = new PostMethod( servletLocation );
        NameValuePair[] data = { new NameValuePair( "SAMLRequest", encodedSamlLogoutRequest ) };
        postMethod.setRequestBody( data );

        // Test
        int statusCode = httpClient.executeMethod( postMethod );

        // Verify
        LOG.debug( "status code: " + statusCode );
        assertEquals( HttpServletResponse.SC_OK, statusCode );

        Tidy tidy = new Tidy();
        tidy.setQuiet( true );
        tidy.setShowWarnings( false );
        Document resultDocument = tidy.parseDOM( postMethod.getResponseBodyAsStream(), null );
        LOG.debug( "result document: " + DomTestUtils.domToString( resultDocument ) );
        String action = XPathAPI.selectSingleNode( resultDocument, "//form/@action" ).getNodeValue();
        String samlResponse = XPathAPI.selectSingleNode( resultDocument, "//form/input[@name='SAMLResponse']/@value" ).getNodeValue();

        HttpServletRequest mockServletRequest = createMock( HttpServletRequest.class );
        expect( mockServletRequest.getMethod() ).andReturn( "POST" );
        expect( mockServletRequest.getParameter( "RelayState" ) ).andReturn( null );
        expect( mockServletRequest.getParameter( "SAMLRequest" ) ).andReturn( null );
        expect( mockServletRequest.getParameter( "SAMLResponse" ) ).andReturn( samlResponse );
        expect( mockServletRequest.getRequestURL() ).andReturn( new StringBuffer( action ) );
        replay( mockServletRequest );

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport( new HttpServletRequestAdapter( mockServletRequest ) );
        new HTTPPostDecoder().decode( messageContext );

        String status = ((LogoutResponse) messageContext.getInboundSAMLMessage()).getStatus().getStatusCode().getValue();
        assertEquals( StatusCode.SUCCESS_URI, status );
    }

    @Test
    public void testPostHandleSaml2LogoutRequestWrongUser()
            throws Exception {

        // Setup Data
        String userId = UUID.randomUUID().toString();
        String notUserId = "not " + userId;
        LogoutRequest samlLogoutRequest = LogoutRequestFactory.createLogoutRequest( userId, applicationName, servletLocation, null );
        String encodedSamlLogoutRequest = Base64.encode(
                DomUtils.domToString( LinkIDSaml2Utils.sign( samlLogoutRequest, keyPair, null ) ).getBytes() );

        servletTestManager.setSessionAttribute( LoginManager.USERID_SESSION_ATTRIBUTE, notUserId );

        PostMethod postMethod = new PostMethod( servletLocation );
        NameValuePair[] data = { new NameValuePair( "SAMLRequest", encodedSamlLogoutRequest ) };
        postMethod.setRequestBody( data );

        // Test
        int statusCode = httpClient.executeMethod( postMethod );

        // Verify
        LOG.debug( "status code: " + statusCode );
        assertEquals( HttpServletResponse.SC_OK, statusCode );

        Tidy tidy = new Tidy();
        tidy.setQuiet( true );
        tidy.setShowWarnings( false );
        Document resultDocument = tidy.parseDOM( postMethod.getResponseBodyAsStream(), null );
        LOG.debug( "result document: " + DomTestUtils.domToString( resultDocument ) );
        String action = XPathAPI.selectSingleNode( resultDocument, "//form/@action" ).getNodeValue();
        String samlResponse = XPathAPI.selectSingleNode( resultDocument, "//form/input[@name='SAMLResponse']/@value" ).getNodeValue();

        HttpServletRequest mockServletRequest = createMock( HttpServletRequest.class );
        expect( mockServletRequest.getMethod() ).andReturn( "POST" );
        expect( mockServletRequest.getParameter( "RelayState" ) ).andReturn( null );
        expect( mockServletRequest.getParameter( "SAMLRequest" ) ).andReturn( null );
        expect( mockServletRequest.getParameter( "SAMLResponse" ) ).andReturn( samlResponse );
        expect( mockServletRequest.getRequestURL() ).andReturn( new StringBuffer( action ) );
        replay( mockServletRequest );

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport( new HttpServletRequestAdapter( mockServletRequest ) );
        new HTTPPostDecoder().decode( messageContext );

        String status = ((LogoutResponse) messageContext.getInboundSAMLMessage()).getStatus().getStatusCode().getValue();
        assertEquals( StatusCode.SUCCESS_URI, status );
    }
}
