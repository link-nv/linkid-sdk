/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolContext;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.Saml2BrowserPostAuthenticationProtocolHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;


public class Saml2BrowserPostAuthenticationProtocolHandlerTest {

    private static final Log    LOG = LogFactory.getLog(Saml2BrowserPostAuthenticationProtocolHandlerTest.class);

    private ServletTestManager  requestServletTestManager;

    private ServletTestManager  responseServletTestManager;

    private WebServiceTestUtils webServiceTestUtils;

    private JndiTestUtils       jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");

        WSSecurityConfigurationService mockWSSecurityConfigurationService = EasyMock.createMock(WSSecurityConfigurationService.class);
        jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", mockWSSecurityConfigurationService);
        EasyMock.expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        EasyMock.expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck((X509Certificate) EasyMock.anyObject()))
                .andStubReturn(true);
        EasyMock.replay(mockWSSecurityConfigurationService);

        webServiceTestUtils = new WebServiceTestUtils();
        SecurityTokenServicePort port = new SecurityTokenServicePortImpl();
        webServiceTestUtils.setUp(port, "/safe-online-ws/sts");

        requestServletTestManager = new ServletTestManager();
        requestServletTestManager.setUp(SamlRequestTestServlet.class, Collections.singletonMap("WsLocation",
                webServiceTestUtils.getLocation()));

        responseServletTestManager = new ServletTestManager();
        responseServletTestManager.setUp(SamlResponseTestServlet.class, Collections.singletonMap("WsLocation",
                webServiceTestUtils.getLocation()));
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
        webServiceTestUtils.tearDown();
        requestServletTestManager.tearDown();
        responseServletTestManager.tearDown();
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

    public static class SamlRequestTestServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        private String            wsLocation;


        @Override
        public void init(ServletConfig config) {

            wsLocation = config.getInitParameter("WsLocation");
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            KeyPair keyPair;
            try {
                keyPair = PkiTestUtils.generateKeyPair();
            } catch (Exception e) {
                throw new ServletException("could not generate RSA key pair");
            }

            Map<String, String> configParams = Collections.singletonMap("WsLocation", wsLocation);
            AuthenticationProtocolHandler authenticationProtocolHandler = AuthenticationProtocolManager
                                                                                                       .createAuthenticationProtocolHandler(
                                                                                                               AuthenticationProtocol.SAML2_BROWSER_POST,
                                                                                                               "http://test.authn.service",
                                                                                                               "test-application", null,
                                                                                                               keyPair, null, false,
                                                                                                               configParams, request);
            authenticationProtocolHandler.initiateAuthentication(request, response, "http://target", null, null, null);
        }
    }

    public static class SamlResponseTestServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        private static final Log  srtLOG           = LogFactory.getLog(SamlResponseTestServlet.class);

        private String            wsLocation;


        @Override
        public void init(ServletConfig config) {

            wsLocation = config.getInitParameter("WsLocation");
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            srtLOG.debug("doPost");
            KeyPair keyPair;
            try {
                keyPair = PkiTestUtils.generateKeyPair();
            } catch (Exception e) {
                throw new ServletException("could not generate RSA key pair");
            }
            X509Certificate certificate;
            try {
                certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=TestApplication");
            } catch (Exception e) {
                throw new ServletException("could not generate certificate");
            }
            Map<String, String> configParams = Collections.singletonMap("WsLocation", wsLocation);
            AuthenticationProtocolHandler authenticationProtocolHandler = AuthenticationProtocolManager
                                                                                                       .createAuthenticationProtocolHandler(
                                                                                                               AuthenticationProtocol.SAML2_BROWSER_POST,
                                                                                                               "http://test.authn.service",
                                                                                                               "test-application", null,
                                                                                                               keyPair, certificate, false,
                                                                                                               configParams, request);
            Saml2BrowserPostAuthenticationProtocolHandler saml2Handler = (Saml2BrowserPostAuthenticationProtocolHandler) authenticationProtocolHandler;
            try {
                Field challengeField = Saml2BrowserPostAuthenticationProtocolHandler.class.getDeclaredField("challenge");
                challengeField.setAccessible(true);
                Challenge<String> challenge = (Challenge<String>) challengeField.get(saml2Handler);
                challenge.setValue("test-in-response-to");
            } catch (Exception e) {
                throw new ServletException("reflection error: " + e.getMessage(), e);
            }
            Writer out = response.getWriter();
            AuthenticationProtocolContext authenticationProtocolContext = authenticationProtocolHandler.finalizeAuthentication(request,
                    response);
            if (null != authenticationProtocolContext) {
                LoginManager.setUserId(authenticationProtocolContext.getUserId(), request);
                LoginManager.setAuthenticatedDevice(authenticationProtocolContext.getAuthenticatedDevice(), request);
                out.write("userId: " + authenticationProtocolContext.getUserId());
            }
            out.flush();
        }
    }


    @Test
    public void doGet()
            throws Exception {

        // setup
        LOG.debug("test doGet");
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(requestServletTestManager.getServletLocation());

        // operate
        int resultStatusCode = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("result status code: " + resultStatusCode);
        assertEquals(HttpStatus.SC_OK, resultStatusCode);
        String response = getMethod.getResponseBodyAsString();
        LOG.debug("response body: " + response);

        File tmpFile = File.createTempFile("saml-post-request-", ".html");
        IOUtils.write(response, new FileOutputStream(tmpFile));
    }

    @Test
    public void defaultVelocityMacroResource()
            throws Exception {

        // operate
        URL result = Saml2BrowserPostAuthenticationProtocolHandler.class
                                                                        .getResource(Saml2BrowserPostAuthenticationProtocolHandler.SAML2_POST_BINDING_VM_RESOURCE);

        // verify
        assertNotNull(result);
    }

    @Test
    public void testResponseHandling()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = responseServletTestManager.getServletLocation();
        PostMethod postMethod = new PostMethod(servletLocation);

        InputStream xmlInputStream = Saml2BrowserPostAuthenticationProtocolHandlerTest.class.getResourceAsStream("/test-saml-response.xml");
        String xmlInputString = IOUtils.toString(xmlInputStream);
        DateTime now = new DateTime();
        xmlInputString = replaceAll("replaceWithCurrentTime", now.toString(), xmlInputString);
        xmlInputString = replaceAll("replaceWithCurrentPlusValidityTime", now.plusMinutes(10).toString(), xmlInputString);
        xmlInputString = replaceAll("replaceWithDestination", servletLocation, xmlInputString);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(IOUtils.toInputStream(xmlInputString), byteOutputStream);
        byte[] samlResponse = byteOutputStream.toByteArray();
        byte[] encodedSamlResponse = Base64.encodeBase64(samlResponse);
        NameValuePair[] data = { new NameValuePair("SAMLResponse", new String(encodedSamlResponse)) };
        postMethod.setRequestBody(data);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_OK, statusCode);
        String responseBody = postMethod.getResponseBodyAsString();
        LOG.debug("response body: \"" + responseBody + "\"");
        String userId = (String) responseServletTestManager.getSessionAttribute(LoginManager.USERID_SESSION_ATTRIBUTE);
        LOG.debug("authenticated userId: " + userId);
        assertNotNull(userId);
        String authenticatedDevice = (String) responseServletTestManager
                                                                             .getSessionAttribute(LoginManager.AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE);
        LOG.debug("authenticated device: " + authenticatedDevice);
        assertNotNull(authenticatedDevice);
    }

    private static String replaceAll(String oldStr, String newStr, String inString) {

        int start;
        String resultString = inString;
        while (true) {
            start = resultString.indexOf(oldStr);
            if (start == -1) {
                break;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(resultString.substring(0, start));
            sb.append(newStr);
            sb.append(resultString.substring(start + oldStr.length()));
            resultString = sb.toString();
        }
        return resultString;
    }
}
