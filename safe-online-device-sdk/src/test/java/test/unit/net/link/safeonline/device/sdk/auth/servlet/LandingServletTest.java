/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.device.sdk.auth.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.auth.servlet.LandingServlet;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.test.util.TestClassLoader;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
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


public class LandingServletTest {

    private static final Log    LOG                = LogFactory.getLog(LandingServletTest.class);

    private ServletTestManager  servletTestManager;

    private WebServiceTestUtils webServiceTestUtils;

    private JndiTestUtils       jndiTestUtils;

    private ClassLoader         originalContextClassLoader;

    private TestClassLoader     testClassLoader;

    private HttpClient          httpClient;

    private String              location;

    private String              authenticationUrl  = "authentication";

    private String              servletEndpointUrl = "http://test.device/servlet";

    private String              deviceName         = "test-device";

    private String              applicationName    = "test-application";

    private Set<String>         wantedDevices;

    private KeyPair             keyPair;


    @Before
    public void setUp() throws Exception {

        this.originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        this.testClassLoader = new TestClassLoader();
        Thread.currentThread().setContextClassLoader(this.testClassLoader);

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");

        WSSecurityConfigurationService mockWSSecurityConfigurationService = EasyMock
                .createMock(WSSecurityConfigurationService.class);
        this.jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local",
                mockWSSecurityConfigurationService);
        EasyMock.expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(
                Long.MAX_VALUE);
        EasyMock.replay(mockWSSecurityConfigurationService);

        this.webServiceTestUtils = new WebServiceTestUtils();
        SecurityTokenServicePort port = new SecurityTokenServicePortImpl();
        this.webServiceTestUtils.setUp(port, "/safe-online-ws/sts");

        this.keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate cert = PkiTestUtils.generateSelfSignedCertificate(this.keyPair, "CN=TestApplication");
        File tmpP12File = File.createTempFile("application-", ".p12");
        tmpP12File.deleteOnExit();
        PkiTestUtils.persistKey(tmpP12File, this.keyPair.getPrivate(), cert, "secret", "secret");

        String p12ResourceName = "p12-resource-name.p12";
        this.testClassLoader.addResource(p12ResourceName, tmpP12File.toURI().toURL());

        this.servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("AuthenticationUrl", this.authenticationUrl);
        initParams.put("KeyStoreResource", p12ResourceName);
        initParams.put("KeyStorePassword", "secret");
        initParams.put("DeviceName", this.deviceName);
        initParams.put("ServletEndpointUrl", this.servletEndpointUrl);
        initParams.put("StsWsLocation", this.webServiceTestUtils.getLocation());

        this.servletTestManager.setUp(LandingServlet.class, initParams, null, null, null);
        this.location = this.servletTestManager.getServletLocation();
        this.httpClient = new HttpClient();
    }

    @After
    public void tearDown() throws Exception {

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
    public void testLanding() throws Exception {

        // setup
        this.wantedDevices = Collections.singleton(this.deviceName);
        String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(this.applicationName, this.applicationName,
                null, this.keyPair, "http://test.authn.service", this.servletEndpointUrl, new Challenge<String>(),
                this.wantedDevices);
        String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest.getBytes());
        NameValuePair[] postData = { new NameValuePair("SAMLRequest", encodedSamlAuthnRequest) };

        // operate
        PostMethod postMethod = new PostMethod(this.location);
        postMethod.setRequestBody(postData);
        int statusCode = this.httpClient.executeMethod(postMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(this.authenticationUrl));
        String resultApplicationId = (String) this.servletTestManager.getSessionAttribute("applicationId");
        assertEquals(this.applicationName, resultApplicationId);
        String resultApplicationName = (String) this.servletTestManager.getSessionAttribute("applicationName");
        assertEquals(this.applicationName, resultApplicationName);
        AuthenticationContext authenticationContext = (AuthenticationContext) this.servletTestManager
                .getSessionAttribute(AuthenticationContext.AUTHENTICATION_CONTEXT);
        assertNotNull(authenticationContext);
        assertEquals(this.applicationName, authenticationContext.getApplication());
        assertEquals(this.applicationName, authenticationContext.getApplicationFriendlyName());
        assertEquals(this.wantedDevices, authenticationContext.getWantedDevices());
    }
}
