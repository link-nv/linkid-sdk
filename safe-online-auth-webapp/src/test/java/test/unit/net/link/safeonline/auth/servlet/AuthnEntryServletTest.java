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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.servlet.AuthnEntryServlet;
import net.link.safeonline.auth.webapp.pages.FirstTimePage;
import net.link.safeonline.auth.webapp.pages.UnsupportedProtocolPage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
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
import org.opensaml.saml2.core.AuthnRequest;


public class AuthnEntryServletTest {

    private static final Log                 LOG        = LogFactory.getLog(AuthnEntryServletTest.class);

    private ServletTestManager               authnEntryServletTestManager;

    private String                           loginPath  = "login";

    private String                           cookiePath = "/test-path/";

    private JndiTestUtils                    jndiTestUtils;

    private ApplicationAuthenticationService mockApplicationAuthenticationService;

    private PkiValidator                     mockPkiValidator;

    private DevicePolicyService              mockDevicePolicyService;

    private AuthenticationService            mockAuthenticationService;

    private Object[]                         mockObjects;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        jndiTestUtils.bindComponent(ApplicationAuthenticationService.JNDI_BINDING, mockApplicationAuthenticationService);
        mockPkiValidator = createMock(PkiValidator.class);
        jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, mockPkiValidator);

        SamlAuthorityService mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn("test-issuer-name");
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(10 * 60);
        jndiTestUtils.bindComponent(SamlAuthorityService.JNDI_BINDING, mockSamlAuthorityService);

        mockDevicePolicyService = createMock(DevicePolicyService.class);
        jndiTestUtils.bindComponent(DevicePolicyService.JNDI_BINDING, mockDevicePolicyService);

        mockAuthenticationService = createMock(AuthenticationService.class);
        jndiTestUtils.bindComponent(AuthenticationService.JNDI_BINDING, mockAuthenticationService);

        authnEntryServletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(AuthnEntryServlet.LOGIN_PATH, loginPath);
        initParams.put(AuthnEntryServlet.COOKIE_PATH, cookiePath);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE, mockAuthenticationService);

        authnEntryServletTestManager.setUp(AuthnEntryServlet.class, initParams, null, null, initialSessionAttributes);
        SafeOnlineTestConfig.loadTest(authnEntryServletTestManager);

        mockObjects = new Object[] { mockApplicationAuthenticationService, mockPkiValidator, mockSamlAuthorityService,
                mockDevicePolicyService, mockAuthenticationService };
    }

    @After
    public void tearDown()
            throws Exception {

        authnEntryServletTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void unsupportedAuthenticationProtocol()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(authnEntryServletTestManager.getServletLocation());
        /*
         * Here we simulate a user that directly visits the authentication web application.
         */
        getMethod.setFollowRedirects(false);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = getMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + location);
        assertTrue(location.endsWith(UnsupportedProtocolPage.PATH));
    }

    @Test
    public void saml2AuthenticationProtocol()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = authnEntryServletTestManager.getServletLocation();
        PostMethod postMethod = new PostMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        long applicationId = 1234567890;
        String applicationName = "test-application-id";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, servletLocation, null, null, false);
        String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest.getBytes());

        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlAuthnRequest) };
        postMethod.setRequestBody(data);

        // expectations
        expect(
                mockAuthenticationService.initialize((Locale) EasyMock.anyObject(), (Integer) EasyMock.anyObject(),
                        (Boolean) EasyMock.anyObject(), (AuthnRequest) EasyMock.anyObject())).andStubReturn(
                new ProtocolContext(applicationId, applicationName, applicationName, assertionConsumerService, null, null, null, null));
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.INITIALIZED);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        LOG.debug("result body: " + postMethod.getResponseBodyAsString());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + location);
        assertTrue(location.endsWith(FirstTimePage.PATH));
        long resultApplicationId = (Long) authnEntryServletTestManager.getSessionAttribute(LoginManager.APPLICATION_ID_ATTRIBUTE);
        assertEquals(applicationId, resultApplicationId);
        String resultApplicationName = (String) authnEntryServletTestManager
                                                                            .getSessionAttribute(LoginManager.APPLICATION_FRIENDLY_NAME_ATTRIBUTE);
        assertEquals(applicationName, resultApplicationName);
        String target = (String) authnEntryServletTestManager.getSessionAttribute(LoginManager.TARGET_ATTRIBUTE);
        assertEquals(assertionConsumerService, target);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void saml2AuthenticationProtocolSingleSignOn()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = authnEntryServletTestManager.getServletLocation();
        PostMethod postMethod = new PostMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        long applicationId = 1234567890;
        String applicationName = "test-application-id";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, servletLocation, null, null, true);
        String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest.getBytes());

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);
        DeviceClassEntity deviceClass = new DeviceClassEntity(SafeOnlineConstants.PKI_DEVICE_CLASS,
                SafeOnlineConstants.PKI_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = new DeviceEntity(BeIdConstants.BEID_DEVICE_ID, deviceClass, null, null, null, null, null, null, null, null);

        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlAuthnRequest) };
        postMethod.setRequestBody(data);
        postMethod.addRequestHeader("Cookie", SafeOnlineCookies.DEFLOWERED_COOKIE + "=true");
        postMethod.addRequestHeader("Cookie", SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + applicationName + "=value");

        AuthenticationAssertion authenticationAssertion = new AuthenticationAssertion(subject);
        authenticationAssertion.addAuthentication(new DateTime(), device);

        // expectations
        expect(
                mockAuthenticationService.initialize((Locale) EasyMock.anyObject(), (Integer) EasyMock.anyObject(),
                        (Boolean) EasyMock.anyObject(), (AuthnRequest) EasyMock.anyObject())).andStubReturn(
                new ProtocolContext(applicationId, applicationName, applicationName, assertionConsumerService, null, null, null, null));
        expect(mockAuthenticationService.login((List<Cookie>) EasyMock.anyObject())).andReturn(
                Collections.singletonList(authenticationAssertion));
        expect(mockAuthenticationService.getInvalidCookies()).andReturn(new LinkedList<Cookie>());

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        LOG.debug("result body: " + postMethod.getResponseBodyAsString());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + location);
        assertTrue(location.endsWith(loginPath));
        long resultApplicationId = (Long) authnEntryServletTestManager.getSessionAttribute(LoginManager.APPLICATION_ID_ATTRIBUTE);
        assertEquals(applicationId, resultApplicationId);
        String resultApplicationName = (String) authnEntryServletTestManager
                                                                            .getSessionAttribute(LoginManager.APPLICATION_FRIENDLY_NAME_ATTRIBUTE);
        assertEquals(applicationName, resultApplicationName);
        String target = (String) authnEntryServletTestManager.getSessionAttribute(LoginManager.TARGET_ATTRIBUTE);
        assertEquals(assertionConsumerService, target);
        String resultUserId = (String) authnEntryServletTestManager.getSessionAttribute(LoginManager.USERID_ATTRIBUTE);
        assertEquals(userId, resultUserId);
        AuthenticationAssertion resultAuthenticationAssertion = (AuthenticationAssertion) authnEntryServletTestManager
                                                                                                                      .getSessionAttribute(LoginManager.AUTHENTICATION_ASSERTION_ATTRIBUTE);
        assertEquals(authenticationAssertion, resultAuthenticationAssertion);
    }
}
