/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;
import net.link.safeonline.sdk.auth.servlet.LogoutServlet;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.test.util.TestClassLoader;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.httpclient.HttpClient;
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
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;


public class LogoutServletTest {

    private static final Log    LOG                  = LogFactory.getLog(LogoutServletTest.class);

    private ServletTestManager  servletTestManager;

    private WebServiceTestUtils webServiceTestUtils;

    private JndiTestUtils       jndiTestUtils;

    private String              logoutUrl            = "logout";

    private String              errorPage            = "error";

    private String              servletEndpointUrl   = "http://test.logout/servlet";

    private String              logoutExitServiceUrl = "http://test.auth/logoutexit";

    private String              applicationName      = "test-application-id";

    private KeyPair             keyPair;

    private ClassLoader         originalContextClassLoader;

    private TestClassLoader     testClassLoader;

    private String              location;

    private HttpClient          httpClient;


    @Before
    public void setUp()
            throws Exception {

        this.originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        this.testClassLoader = new TestClassLoader();
        Thread.currentThread().setContextClassLoader(this.testClassLoader);

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");

        WSSecurityConfigurationService mockWSSecurityConfigurationService = EasyMock.createMock(WSSecurityConfigurationService.class);
        this.jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", mockWSSecurityConfigurationService);
        EasyMock.expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        EasyMock.expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck((X509Certificate) EasyMock.anyObject()))
                .andStubReturn(true);
        EasyMock.replay(mockWSSecurityConfigurationService);

        this.keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate cert = PkiTestUtils.generateSelfSignedCertificate(this.keyPair, "CN=TestApplication");
        File tmpP12File = File.createTempFile("application-", ".p12");
        tmpP12File.deleteOnExit();
        PkiTestUtils.persistKey(tmpP12File, this.keyPair.getPrivate(), cert, "secret", "secret");

        String p12ResourceName = "p12-resource-name.p12";
        this.testClassLoader.addResource(p12ResourceName, tmpP12File.toURI().toURL());

        this.webServiceTestUtils = new WebServiceTestUtils();
        SecurityTokenServicePort port = new SecurityTokenServicePortImpl();
        this.webServiceTestUtils.setUp(port, "/safe-online-ws/sts");

        this.servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("LogoutUrl", this.logoutUrl);
        initParams.put("ErrorPage", this.errorPage);
        initParams.put(SafeOnlineLoginUtils.LOGOUT_EXIT_SERVICE_URL_INIT_PARAM, this.logoutExitServiceUrl);
        initParams.put(SafeOnlineLoginUtils.APPLICATION_NAME_INIT_PARAM, this.applicationName);
        initParams.put(SafeOnlineLoginUtils.KEY_STORE_RESOURCE_INIT_PARAM, p12ResourceName);
        initParams.put(SafeOnlineLoginUtils.KEY_STORE_PASSWORD_INIT_PARAM, "secret");
        initParams.put("ServletEndpointUrl", this.servletEndpointUrl);
        initParams.put("WsLocation", this.webServiceTestUtils.getLocation());

        this.servletTestManager.setUp(LogoutServlet.class, initParams, null, null, null);
        this.location = this.servletTestManager.getServletLocation();
        this.httpClient = new HttpClient();
    }

    @After
    public void tearDown()
            throws Exception {

        this.servletTestManager.tearDown();
        Thread.currentThread().setContextClassLoader(this.originalContextClassLoader);
    }


    @WebService(endpointInterface = "org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort")
    @HandlerChain(file = "test-sts-ws-handlers.xml")
    public static class SecurityTokenServicePortImpl implements SecurityTokenServicePort {

        public RequestSecurityTokenResponseType requestSecurityToken(RequestSecurityTokenType request) {

            return createResponse(SecurityTokenServiceConstants.STATUS_VALID, "test-token", null);
        }

        private RequestSecurityTokenResponseType createResponse(String statusCode, String tokenType, String reason) {

            ObjectFactory objectFactory = new ObjectFactory();
            RequestSecurityTokenResponseType response = new RequestSecurityTokenResponseType();
            StatusType status = objectFactory.createStatusType();
            status.setCode(statusCode);
            if (null != reason) {
                status.setReason(reason);
            }
            if (null != tokenType) {
                response.getAny().add(objectFactory.createTokenType(tokenType));
            }
            response.getAny().add(objectFactory.createStatus(status));
            return response;
        }
    }


    @Test
    public void testGetTargetSet()
            throws Exception {

        // setup
        AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
        this.servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE,
                mockAuthenticationProtocolHandler);

        String targetUrl = "target";
        this.servletTestManager.setSessionAttribute(AuthenticationProtocolManager.TARGET_ATTRIBUTE, targetUrl);

        // operate
        GetMethod getMethod = new GetMethod(this.location);
        getMethod.setFollowRedirects(false);
        int statusCode = this.httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = getMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(targetUrl));

    }

    @Test
    public void testGetFinalizeLocalLogout()
            throws Exception {

        // setup
        AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
        this.servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE,
                mockAuthenticationProtocolHandler);

        // expectations
        mockAuthenticationProtocolHandler.sendLogoutResponse(EasyMock.anyBoolean(), (HttpServletRequest) EasyMock.anyObject(),
                (HttpServletResponse) EasyMock.anyObject());

        // prepare
        replay(mockAuthenticationProtocolHandler);

        // operate
        GetMethod getMethod = new GetMethod(this.location);
        int statusCode = this.httpClient.executeMethod(getMethod);

        // verify
        verify(mockAuthenticationProtocolHandler);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_OK, statusCode);
        assertNull(this.servletTestManager.getSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE));
    }

    @Test
    public void testPostFinalizeSingleLogout()
            throws Exception {

        // setup
        AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
        this.servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE,
                mockAuthenticationProtocolHandler);

        // expectations
        expect(
                mockAuthenticationProtocolHandler.finalizeLogout((HttpServletRequest) EasyMock.anyObject(),
                        (HttpServletResponse) EasyMock.anyObject())).andStubReturn(true);

        // prepare
        replay(mockAuthenticationProtocolHandler);

        // operate
        PostMethod postMethod = new PostMethod(this.location);
        int statusCode = this.httpClient.executeMethod(postMethod);

        // verify
        verify(mockAuthenticationProtocolHandler);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String resultTarget = postMethod.getResponseHeader("Location").getValue();
        assertTrue(resultTarget.endsWith(this.logoutUrl));
    }

    @Test
    public void testPostHandleSaml2LogoutRequest()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        Challenge<String> challenge = new Challenge<String>();
        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, this.applicationName, this.keyPair,
                this.servletEndpointUrl, challenge);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        this.servletTestManager.setSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        PostMethod postMethod = new PostMethod(this.location);
        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlLogoutRequest) };
        postMethod.setRequestBody(data);

        // operate
        int statusCode = this.httpClient.executeMethod(postMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String resultTarget = postMethod.getResponseHeader("Location").getValue();
        assertTrue(resultTarget.endsWith(this.logoutUrl));
    }

    @Test
    public void testPostHandleSaml2LogoutRequestWrongUser()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String fooUserId = UUID.randomUUID().toString();
        Challenge<String> challenge = new Challenge<String>();
        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, this.applicationName, this.keyPair,
                this.servletEndpointUrl, challenge);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        this.servletTestManager.setSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, fooUserId);

        PostMethod postMethod = new PostMethod(this.location);
        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlLogoutRequest) };
        postMethod.setRequestBody(data);

        // operate
        int statusCode = this.httpClient.executeMethod(postMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String resultTarget = postMethod.getResponseHeader("Location").getValue();
        assertTrue(resultTarget.endsWith(this.errorPage));
    }
}
