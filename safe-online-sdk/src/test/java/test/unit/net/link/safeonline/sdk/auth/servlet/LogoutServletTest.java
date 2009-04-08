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

import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.seam.SafeOnlineAuthenticationUtils;
import net.link.safeonline.sdk.auth.servlet.LogoutServlet;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;
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

    private static final Log    LOG                   = LogFactory.getLog(LogoutServletTest.class);

    private ServletTestManager  servletTestManager;

    private WebServiceTestUtils webServiceTestUtils;

    private JndiTestUtils       jndiTestUtils;

    private String              logoutPath            = "logout";

    private String              errorPage             = "error";

    private String              logoutExitServicePath = "logoutexit";

    private String              applicationName       = "test-application-id";

    private KeyPair             keyPair;

    private ClassLoader         originalContextClassLoader;

    private TestClassLoader     testClassLoader;

    private String              location;

    private HttpClient          httpClient;


    @Before
    public void setUp()
            throws Exception {

        originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        testClassLoader = new TestClassLoader();
        Thread.currentThread().setContextClassLoader(testClassLoader);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", "SafeOnline/WSSecurityConfigurationBean/local");
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);

        WSSecurityConfigurationService mockWSSecurityConfigurationService = EasyMock.createMock(WSSecurityConfigurationService.class);
        jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", mockWSSecurityConfigurationService);
        EasyMock.expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        EasyMock.expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck((X509Certificate) EasyMock.anyObject()))
                .andStubReturn(true);
        EasyMock.replay(mockWSSecurityConfigurationService);

        keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate cert = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=TestApplication");
        File tmpP12File = File.createTempFile("application-", ".p12");
        tmpP12File.deleteOnExit();
        PkiTestUtils.persistInPKCS12KeyStore(tmpP12File, keyPair.getPrivate(), cert, "secret", "secret");

        String p12ResourceName = "p12-resource-name.p12";
        testClassLoader.addResource(p12ResourceName, tmpP12File.toURI().toURL());

        webServiceTestUtils = new WebServiceTestUtils();
        SecurityTokenServicePort port = new SecurityTokenServicePortImpl();
        webServiceTestUtils.setUp(port, "/safe-online-ws/sts");

        servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(LogoutServlet.LOGOUT_PATH, logoutPath);
        initParams.put(LogoutServlet.ERROR_PAGE, errorPage);
        initParams.put(SafeOnlineAuthenticationUtils.LOGOUT_EXIT_SERVICE_PATH_INIT_PARAM, logoutExitServicePath);
        initParams.put(SafeOnlineAuthenticationUtils.APPLICATION_NAME_CONTEXT_PARAM, applicationName);
        initParams.put(SafeOnlineAuthenticationUtils.KEY_STORE_RESOURCE_CONTEXT_PARAM, p12ResourceName);
        initParams.put(SafeOnlineAuthenticationUtils.KEY_STORE_PASSWORD_CONTEXT_PARAM, "secret");

        servletTestManager.setUp(LogoutServlet.class, initParams, null, null, null);
        location = servletTestManager.getServletLocation();
        httpClient = new HttpClient();

        SafeOnlineTestConfig.loadTest(servletTestManager, webServiceTestUtils);
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
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
        servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE, mockAuthenticationProtocolHandler);

        String targetUrl = "target";
        servletTestManager.setSessionAttribute(AuthenticationProtocolManager.TARGET_ATTRIBUTE, targetUrl);

        // operate
        GetMethod getMethod = new GetMethod(location);
        getMethod.setFollowRedirects(false);
        int statusCode = httpClient.executeMethod(getMethod);

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
        servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE, mockAuthenticationProtocolHandler);

        // expectations
        mockAuthenticationProtocolHandler.sendLogoutResponse(EasyMock.anyBoolean(), (HttpServletRequest) EasyMock.anyObject(),
                (HttpServletResponse) EasyMock.anyObject());

        // prepare
        replay(mockAuthenticationProtocolHandler);

        // operate
        GetMethod getMethod = new GetMethod(location);
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(mockAuthenticationProtocolHandler);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_OK, statusCode);
        assertNull(servletTestManager.getSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE));
    }

    @Test
    public void testPostFinalizeSingleLogout()
            throws Exception {

        // setup
        AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
        servletTestManager.setSessionAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE, mockAuthenticationProtocolHandler);

        // expectations
        expect(
                mockAuthenticationProtocolHandler.finalizeLogout((HttpServletRequest) EasyMock.anyObject(),
                        (HttpServletResponse) EasyMock.anyObject())).andStubReturn(true);

        // prepare
        replay(mockAuthenticationProtocolHandler);

        // operate
        PostMethod postMethod = new PostMethod(location);
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockAuthenticationProtocolHandler);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String resultTarget = postMethod.getResponseHeader("Location").getValue();
        assertTrue(resultTarget.endsWith(logoutPath));
    }

    @Test
    public void testPostHandleSaml2LogoutRequest()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        Challenge<String> challenge = new Challenge<String>();
        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, applicationName, keyPair, location, challenge, null);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        servletTestManager.setSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        PostMethod postMethod = new PostMethod(location);
        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlLogoutRequest) };
        postMethod.setRequestBody(data);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String resultTarget = postMethod.getResponseHeader("Location").getValue();
        assertTrue(resultTarget.endsWith(logoutPath));
    }

    @Test
    public void testPostHandleSaml2LogoutRequestWrongUser()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String fooUserId = UUID.randomUUID().toString();
        Challenge<String> challenge = new Challenge<String>();
        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, applicationName, keyPair, location, challenge, null);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        servletTestManager.setSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, fooUserId);

        PostMethod postMethod = new PostMethod(location);
        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlLogoutRequest) };
        postMethod.setRequestBody(data);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        String resultTarget = postMethod.getResponseHeader("Location").getValue();
        assertTrue(resultTarget.endsWith(errorPage));
    }
}
