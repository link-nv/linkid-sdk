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
import java.util.Map;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.servlet.AuthnEntryServlet;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
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
import org.opensaml.saml2.core.AuthnRequest;


public class AuthnEntryServletTest {

    private static final Log                 LOG                    = LogFactory.getLog(AuthnEntryServletTest.class);

    private ServletTestManager               authnEntryServletTestManager;

    private String                           firstTimeUrl           = "first-time";

    private String                           startUrl               = "start";

    private String                           servletEndpointUrl     = "http://test.auth/servlet";

    private String                           unsupportedProtocolUrl = "unsupported-protocol";

    private String                           protocolErrorUrl       = "protocol-error";

    private JndiTestUtils                    jndiTestUtils;

    private ApplicationAuthenticationService mockApplicationAuthenticationService;

    private PkiValidator                     mockPkiValidator;

    private DevicePolicyService              mockDevicePolicyService;

    private AuthenticationService            mockAuthenticationService;

    private Object[]                         mockObjects;


    @Before
    public void setUp() throws Exception {

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        this.jndiTestUtils.bindComponent("SafeOnline/ApplicationAuthenticationServiceBean/local",
                this.mockApplicationAuthenticationService);
        this.mockPkiValidator = createMock(PkiValidator.class);
        this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local", this.mockPkiValidator);

        SamlAuthorityService mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn("test-issuer-name");
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(10 * 60);
        this.jndiTestUtils.bindComponent("SafeOnline/SamlAuthorityServiceBean/local", mockSamlAuthorityService);

        this.mockDevicePolicyService = createMock(DevicePolicyService.class);
        this.jndiTestUtils.bindComponent("SafeOnline/DevicePolicyServiceBean/local", this.mockDevicePolicyService);

        this.mockAuthenticationService = createMock(AuthenticationService.class);

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        this.authnEntryServletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("StartUrl", this.startUrl);
        initParams.put("FirstTimeUrl", this.firstTimeUrl);
        initParams.put("ServletEndpointUrl", this.servletEndpointUrl);
        initParams.put("UnsupportedProtocolUrl", this.unsupportedProtocolUrl);
        initParams.put("ProtocolErrorUrl", this.protocolErrorUrl);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE,
                this.mockAuthenticationService);

        this.authnEntryServletTestManager.setUp(AuthnEntryServlet.class, initParams, null, null,
                initialSessionAttributes);

        this.mockObjects = new Object[] { this.mockApplicationAuthenticationService, this.mockPkiValidator,
                mockSamlAuthorityService, this.mockDevicePolicyService, this.mockAuthenticationService };
    }

    @After
    public void tearDown() throws Exception {

        this.authnEntryServletTestManager.tearDown();
        this.jndiTestUtils.tearDown();
    }

    @Test
    public void unsupportedAuthenticationProtocol() throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(this.authnEntryServletTestManager.getServletLocation());
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
        assertTrue(location.endsWith(this.unsupportedProtocolUrl));
    }

    @Test
    public void saml2AuthenticationProtocol() throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = this.authnEntryServletTestManager.getServletLocation();
        PostMethod postMethod = new PostMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, this.servletEndpointUrl, null, null);
        String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest.getBytes());

        NameValuePair[] data = { new NameValuePair("SAMLRequest", encodedSamlAuthnRequest) };
        postMethod.setRequestBody(data);

        // expectations
        this.mockAuthenticationService.initialize((AuthnRequest) EasyMock.anyObject());
        expect(this.mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.INIT);
        expect(this.mockAuthenticationService.getExpectedApplicationId()).andStubReturn(applicationName);
        expect(this.mockAuthenticationService.getExpectedApplicationFriendlyName()).andStubReturn(applicationName);
        expect(this.mockAuthenticationService.getExpectedTarget()).andStubReturn(assertionConsumerService);
        expect(this.mockAuthenticationService.getRequiredDevicePolicy()).andStubReturn(null);

        // prepare
        replay(this.mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(this.mockObjects);
        LOG.debug("status code: " + statusCode);
        LOG.debug("result body: " + postMethod.getResponseBodyAsString());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + location);
        assertTrue(location.endsWith(this.firstTimeUrl));
        String resultApplicationId = (String) this.authnEntryServletTestManager
                .getSessionAttribute(LoginManager.APPLICATION_ID_ATTRIBUTE);
        assertEquals(applicationName, resultApplicationId);
        String resultApplicationName = (String) this.authnEntryServletTestManager
                .getSessionAttribute(LoginManager.APPLICATION_FRIENDLY_NAME_ATTRIBUTE);
        assertEquals(applicationName, resultApplicationName);
        String target = (String) this.authnEntryServletTestManager.getSessionAttribute(LoginManager.TARGET_ATTRIBUTE);
        assertEquals(assertionConsumerService, target);
    }
}
