/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.device.sdk.auth.servlet;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.auth.saml2.DeviceManager;
import net.link.safeonline.device.sdk.auth.servlet.AbstractDeviceAuthenticationLandingServlet;
import net.link.safeonline.keystore.KeyStoreUtils;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.util.servlet.SafeOnlineConfig;

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


public class DeviceAuthenticationLandingServletTest {

    private static final Log               LOG                = LogFactory.getLog(DeviceAuthenticationLandingServletTest.class);

    private ServletTestManager             servletTestManager;

    private WebServiceTestUtils            webServiceTestUtils;

    private JndiTestUtils                  jndiTestUtils;

    private HttpClient                     httpClient;

    private String                         location;

    private String                         authenticationPath = "authentication";

    private String                         deviceName         = "test-device";

    private String                         applicationName    = "test-application";

    private Set<String>                    wantedDevices;

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
        initParams.put(AbstractDeviceAuthenticationLandingServlet.AUTHENTICATION_PATH, authenticationPath);

        servletTestManager.setUp(TestDeviceAuthenticationLandingServlet.class, initParams, null, null, null);
        location = servletTestManager.getServletLocation();
        httpClient = new HttpClient();

        SafeOnlineConfig.load(servletTestManager, webServiceTestUtils);
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
    public void testLanding()
            throws Exception {

        // setup
        wantedDevices = Collections.singleton(deviceName);
        String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, nodeKeyPair,
                "http://test.authn.service", location, new Challenge<String>(), wantedDevices, false);
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
        assertTrue(resultLocation.endsWith(authenticationPath));
        String resultApplicationId = (String) servletTestManager.getSessionAttribute(DeviceManager.APPLICATION_ID_SESSION_ATTRIBUTE);
        assertEquals(applicationName, resultApplicationId);
        assertNull(servletTestManager.getSessionAttribute(DeviceManager.APPLICATION_NAME_SESSION_ATTRIBUTE));
        AuthenticationContext authenticationContext = (AuthenticationContext) servletTestManager
                                                                                                .getSessionAttribute(AuthenticationContext.AUTHENTICATION_CONTEXT);
        assertNotNull(authenticationContext);
        assertEquals(applicationName, authenticationContext.getApplication());
        assertNull(authenticationContext.getApplicationFriendlyName());
        assertEquals(wantedDevices, authenticationContext.getWantedDevices());
    }


    public static class TestDeviceAuthenticationLandingServlet extends AbstractDeviceAuthenticationLandingServlet {

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
