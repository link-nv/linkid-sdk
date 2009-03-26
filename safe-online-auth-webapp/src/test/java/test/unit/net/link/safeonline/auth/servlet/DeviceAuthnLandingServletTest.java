/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.auth.servlet.DeviceAuthnLandingServlet;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.device.sdk.auth.saml2.response.AuthnResponseFactory;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.core.Response;


public class DeviceAuthnLandingServletTest {

    private static final Log      LOG                  = LogFactory.getLog(DeviceAuthnLandingServletTest.class);

    private ServletTestManager    servletTestManager;

    private JndiTestUtils         jndiTestUtils;

    private HttpClient            httpClient;

    private String                location;

    private String                deviceErrorPath      = "device-error";

    private String                startUrl             = "start";

    private String                loginPath            = "login";

    private String                tryAnotherDevicePath = "try-another-device";

    private AuthenticationService mockAuthenticationService;

    private SubjectService        mockSubjectService;

    private HelpdeskManager       mockHelpdeskManager;

    private Object[]              mockObjects;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockAuthenticationService = createMock(AuthenticationService.class);
        mockSubjectService = createMock(SubjectService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);
        jndiTestUtils.bindComponent("SafeOnline/SubjectServiceBean/local", mockSubjectService);
        jndiTestUtils.bindComponent("SafeOnline/HelpdeskManagerBean/local", mockHelpdeskManager);

        servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(DeviceAuthnLandingServlet.LOGIN_PATH, loginPath);
        initParams.put(DeviceAuthnLandingServlet.TRY_ANOTHER_DEVICE_PATH, tryAnotherDevicePath);
        initParams.put(DeviceAuthnLandingServlet.DEVICE_ERROR_PATH, deviceErrorPath);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(ProtocolHandlerManager.PROTOCOL_HANDLER_ID_ATTRIBUTE, Saml2PostProtocolHandler.class.getName());
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE, mockAuthenticationService);
        initialSessionAttributes.put(AuthenticationUtils.REQUEST_URL_SESSION_ATTRIBUTE, startUrl);

        servletTestManager.setUp(DeviceAuthnLandingServlet.class, initParams, null, null, initialSessionAttributes);
        location = servletTestManager.getServletLocation();
        httpClient = new HttpClient();

        mockObjects = new Object[] { mockAuthenticationService, mockSubjectService, mockHelpdeskManager };

        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(50);

        SafeOnlineTestConfig.loadTest(servletTestManager);

    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void getNotAllowed()
            throws Exception {

        // setup
        GetMethod getMethod = new GetMethod(location);

        // operate
        int result = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("result: " + result);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, result);
    }

    @Test
    public void authenticationNotRegistered()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String samlResponse = AuthnResponseFactory.createAuthResponse("test-inresponse-to", applicationName, applicationName,
                UUID.randomUUID().toString(), "test-device", applicationKeyPair, 0, location);
        String encodedSamlAuthnResponse = Base64.encode(samlResponse.getBytes());
        PostMethod postMethod = new PostMethod(location);
        NameValuePair[] data = { new NameValuePair("SAMLResponse", encodedSamlAuthnResponse) };
        postMethod.setRequestBody(data);

        // expectations
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
        expect(mockAuthenticationService.authenticate((Response) EasyMock.anyObject())).andStubReturn(null);
        expect(mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(tryAnotherDevicePath));
    }

    @Test
    public void authenticationFailed()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String samlResponse = AuthnResponseFactory.createAuthResponse("test-inresponse-to", applicationName, applicationName,
                UUID.randomUUID().toString(), "test-device", applicationKeyPair, 0, location);
        String encodedSamlAuthnResponse = Base64.encode(samlResponse.getBytes());
        PostMethod postMethod = new PostMethod(location);
        NameValuePair[] data = { new NameValuePair("SAMLResponse", encodedSamlAuthnResponse) };
        postMethod.setRequestBody(data);

        // expectations
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.INITIALIZED);
        expect(mockAuthenticationService.authenticate((Response) EasyMock.anyObject())).andStubReturn(null);
        expect(mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(startUrl));
    }

    @Test
    public void authenticationSuccess()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String samlResponse = AuthnResponseFactory.createAuthResponse("test-inresponse-to", applicationName, applicationName,
                UUID.randomUUID().toString(), "test-device", applicationKeyPair, 0, location);
        String encodedSamlAuthnResponse = Base64.encode(samlResponse.getBytes());
        PostMethod postMethod = new PostMethod(location);
        NameValuePair[] data = { new NameValuePair("SAMLResponse", encodedSamlAuthnResponse) };
        postMethod.setRequestBody(data);
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);
        DeviceEntity device = new DeviceEntity();
        AuthenticationAssertion authenticationAssertion = new AuthenticationAssertion(subject);
        authenticationAssertion.addAuthentication(new DateTime(), device);

        // expectations
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
        expect(mockAuthenticationService.authenticate((Response) EasyMock.anyObject())).andReturn(authenticationAssertion);
        expect(mockAuthenticationService.getRegisteredDevice()).andStubReturn(device);
        expect(mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);
        expect(mockAuthenticationService.getSsoCookies()).andReturn(new LinkedList<Cookie>());

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        LOG.debug("result body: " + postMethod.getResponseBodyAsString());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(loginPath));
        String resultUserId = (String) servletTestManager.getSessionAttribute(LoginManager.USERID_ATTRIBUTE);
        assertEquals(userId, resultUserId);
        AuthenticationAssertion resultAuthenticationAssertion = (AuthenticationAssertion) servletTestManager
                                                                                                            .getSessionAttribute(LoginManager.AUTHENTICATION_ASSERTION_ATTRIBUTE);
        assertEquals(authenticationAssertion, resultAuthenticationAssertion);
    }
}
