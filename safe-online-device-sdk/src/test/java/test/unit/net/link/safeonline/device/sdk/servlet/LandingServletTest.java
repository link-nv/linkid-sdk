/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.device.sdk.servlet;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.manage.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.manage.saml2.request.DeviceOperationRequestFactory;
import net.link.safeonline.device.sdk.manage.servlet.AbstractDeviceManagementLandingServlet;
import net.link.safeonline.keystore.KeyStoreUtils;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;


public class LandingServletTest {

    private static final Log               LOG                           = LogFactory.getLog(LandingServletTest.class);

    private ServletTestManager             servletTestManager;

    private WebServiceTestUtils            webServiceTestUtils;

    private JndiTestUtils                  jndiTestUtils;

    private HttpClient                     httpClient;

    private String                         location;

    private String                         registrationUrl               = "registration";

    private String                         removalUrl                    = "removal";

    private String                         updateUrl                     = "update";

    private String                         deviceName                    = "test-device";

    private String                         authenticatedDeviceName       = "test-authenticated-device";

    private String                         deviceRegistrationAttributeId = UUID.randomUUID().toString();

    private String                         deviceRegistrationAttribute   = "test-attribute";

    private String                         applicationName               = "test-application";

    private String                         servletEndpointUrl            = "http://test.device/servlet";

    String                                 userId                        = UUID.randomUUID().toString();

    private WSSecurityConfigurationService mockWSSecurityConfigurationService;

    static KeyPair                         nodeKeyPair;

    static X509Certificate                 nodeCertificate;


    @BeforeClass
    public static void init()
            throws Exception {

        nodeKeyPair = PkiTestUtils.generateKeyPair();
        nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
    }

    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", "SafeOnline/WSSecurityConfigurationBean/local");
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);

        mockWSSecurityConfigurationService = EasyMock.createMock(WSSecurityConfigurationService.class);
        jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", mockWSSecurityConfigurationService);
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck((X509Certificate) EasyMock.anyObject())).andStubReturn(true);
        replay(mockWSSecurityConfigurationService);

        webServiceTestUtils = new WebServiceTestUtils();
        SecurityTokenServicePort port = new SecurityTokenServicePortImpl();
        webServiceTestUtils.setUp(port, "/safe-online-ws/sts");

        servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("RegistrationUrl", registrationUrl);
        initParams.put("UpdateUrl", updateUrl);
        initParams.put("RemovalUrl", removalUrl);
        initParams.put("ServletEndpointUrl", servletEndpointUrl);
        initParams.put("WsLocation", webServiceTestUtils.getLocation());

        servletTestManager.setUp(TestDeviceManagementLandingServlet.class, initParams, null, null, null);
        location = servletTestManager.getServletLocation();
        httpClient = new HttpClient();
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
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
    public void testRegistration()
            throws Exception {

        // setup
        String deviceOperationRequest = DeviceOperationRequestFactory.createDeviceOperationRequest(applicationName, userId, nodeKeyPair,
                "http://test.authn.service", servletEndpointUrl, DeviceOperationType.REGISTER, new Challenge<String>(), deviceName,
                authenticatedDeviceName, null, null);
        String encodedSamlAuthnRequest = Base64.encode(deviceOperationRequest.getBytes());
        NameValuePair[] postData = { new NameValuePair("SAMLRequest", encodedSamlAuthnRequest) };

        // operate
        PostMethod postMethod = new PostMethod(location);
        postMethod.setRequestBody(postData);
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockWSSecurityConfigurationService);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(registrationUrl));
        String resultUserId = (String) servletTestManager.getSessionAttribute("userId");
        assertEquals(userId, resultUserId);
        String resultOperation = (String) servletTestManager.getSessionAttribute("operation");
        assertEquals(DeviceOperationType.REGISTER.name(), resultOperation);
        ProtocolContext protocolContext = (ProtocolContext) servletTestManager.getSessionAttribute(ProtocolContext.PROTOCOL_CONTEXT);
        assertNotNull(protocolContext);
        assertEquals(DeviceOperationType.REGISTER, protocolContext.getDeviceOperation());
        assertEquals(deviceName, protocolContext.getDevice());
        assertEquals(authenticatedDeviceName, protocolContext.getAuthenticatedDevice());
        assertEquals(userId, protocolContext.getSubject());
    }

    @Test
    public void testRemoval()
            throws Exception {

        // setup
        String samlAuthnRequest = DeviceOperationRequestFactory.createDeviceOperationRequest(applicationName, userId, nodeKeyPair,
                "http://test.authn.service", servletEndpointUrl, DeviceOperationType.REMOVE, new Challenge<String>(), deviceName,
                authenticatedDeviceName, deviceRegistrationAttributeId, deviceRegistrationAttribute);
        String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest.getBytes());
        NameValuePair[] postData = { new NameValuePair("SAMLRequest", encodedSamlAuthnRequest) };

        // operate
        PostMethod postMethod = new PostMethod(location);
        postMethod.setRequestBody(postData);
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockWSSecurityConfigurationService);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(removalUrl));
        String resultUserId = (String) servletTestManager.getSessionAttribute("userId");
        assertEquals(userId, resultUserId);
        String resultOperation = (String) servletTestManager.getSessionAttribute("operation");
        assertEquals(DeviceOperationType.REMOVE.name(), resultOperation);
        ProtocolContext protocolContext = (ProtocolContext) servletTestManager.getSessionAttribute(ProtocolContext.PROTOCOL_CONTEXT);
        assertNotNull(protocolContext);
        assertEquals(DeviceOperationType.REMOVE, protocolContext.getDeviceOperation());
        assertEquals(deviceName, protocolContext.getDevice());
        assertEquals(authenticatedDeviceName, protocolContext.getAuthenticatedDevice());
        assertEquals(userId, protocolContext.getSubject());
        assertEquals(deviceRegistrationAttribute, protocolContext.getAttribute());
        assertEquals(deviceRegistrationAttributeId, protocolContext.getAttributeId());
    }

    @Test
    public void testUpdate()
            throws Exception {

        // setup
        String samlAuthnRequest = DeviceOperationRequestFactory.createDeviceOperationRequest(applicationName, userId, nodeKeyPair,
                "http://test.authn.service", servletEndpointUrl, DeviceOperationType.UPDATE, new Challenge<String>(), deviceName,
                authenticatedDeviceName, deviceRegistrationAttributeId, deviceRegistrationAttribute);
        String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest.getBytes());
        NameValuePair[] postData = { new NameValuePair("SAMLRequest", encodedSamlAuthnRequest) };

        // operate
        PostMethod postMethod = new PostMethod(location);
        postMethod.setRequestBody(postData);
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockWSSecurityConfigurationService);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(updateUrl));
        String resultUserId = (String) servletTestManager.getSessionAttribute("userId");
        assertEquals(userId, resultUserId);
        String resultOperation = (String) servletTestManager.getSessionAttribute("operation");
        assertEquals(DeviceOperationType.UPDATE.name(), resultOperation);
        ProtocolContext protocolContext = (ProtocolContext) servletTestManager.getSessionAttribute(ProtocolContext.PROTOCOL_CONTEXT);
        assertNotNull(protocolContext);
        assertEquals(DeviceOperationType.UPDATE, protocolContext.getDeviceOperation());
        assertEquals(deviceName, protocolContext.getDevice());
        assertEquals(authenticatedDeviceName, protocolContext.getAuthenticatedDevice());
        assertEquals(userId, protocolContext.getSubject());
        assertEquals(deviceRegistrationAttribute, protocolContext.getAttribute());
        assertEquals(deviceRegistrationAttributeId, protocolContext.getAttributeId());
    }


    public static class TestDeviceManagementLandingServlet extends AbstractDeviceManagementLandingServlet {

        private static final long   serialVersionUID  = 1L;
        private static final String KEYSTORE_PASSWORD = "test-password";


        /**
         * {@inheritDoc}
         */
        @Override
        protected String getIssuer() {

            return "safe-online";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected OlasKeyStore getKeyStore() {

            return new OlasKeyStore() {

                public PrivateKey getPrivateKey() {

                    return getKeyPair().getPrivate();
                }

                public KeyPair getKeyPair() {

                    return nodeKeyPair;
                }

                public X509Certificate getCertificate() {

                    return nodeCertificate;
                }

                public PrivateKeyEntry _getPrivateKeyEntry() {

                    try {
                        File tempFile = File.createTempFile("test-keystore", "jks");
                        KeyStoreUtils.persistKey(tempFile, getPrivateKey(), getCertificate(), KEYSTORE_PASSWORD.toCharArray(),
                                KEYSTORE_PASSWORD.toCharArray());

                        return KeyStoreUtils.loadPrivateKeyEntry("pkcs12", new FileInputStream(tempFile), KEYSTORE_PASSWORD,
                                KEYSTORE_PASSWORD);
                    }

                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    }
}
