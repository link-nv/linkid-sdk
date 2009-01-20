/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.password.auth.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationGetInstanceRequestType;
import net.lin_k.safe_online.auth.AuthenticationGetInstanceResponseType;
import net.lin_k.safe_online.auth.DeviceAuthenticationPort;
import net.lin_k.safe_online.auth.GetDeviceAuthenticationPort;
import net.lin_k.safe_online.auth.GetDeviceAuthenticationService;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.WSAuthenticationService;
import net.link.safeonline.device.auth.ws.DeviceAuthenticationServiceFactory;
import net.link.safeonline.device.auth.ws.GetDeviceAuthenticationServiceFactory;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.password.auth.ws.PasswordAuthenticationPortImpl;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sdk.ws.auth.AuthenticationUtil;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PasswordAuthenticationPortImplTest {

    private static final Log                 LOG               = LogFactory.getLog(PasswordAuthenticationPortImplTest.class);

    private WebServiceTestUtils              webServiceTestUtils;
    private WebServiceTestUtils              getWebServiceTestUtils;

    private DeviceAuthenticationPort         clientPort;

    private JndiTestUtils                    jndiTestUtils;

    private WSSecurityConfigurationService   mockWSSecurityConfigurationService;

    private PkiValidator                     mockPkiValidator;

    private ApplicationAuthenticationService mockApplicationAuthenticationService;

    private DeviceAuthenticationService      mockDeviceAuthenticationService;

    private NodeAuthenticationService        mockNodeAuthenticationService;

    private SamlAuthorityService             mockSamlAuthorityService;

    private WSAuthenticationService          mockWSAuthenticationService;

    private PasswordDeviceService            mockPasswordDeviceServce;

    private Object[]                         mockObjects;

    private PublicKey                        testpublicKey;

    private X509Certificate                  certificate;

    private X509Certificate                  olasCertificate;

    private PrivateKey                       olasPrivateKey;

    private String                           testLanguage      = Locale.ENGLISH.getLanguage();

    private String                           testIssuerName    = "test-issuer-name";

    private String                           testApplicationId = "test-application-name";


    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
            throws Exception {

        LOG.debug("setup");

        // setup JMX
        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");
        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return authCertificate;
            }
        });

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", WSSecurityConfiguration.JNDI_BINDING);
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);
        jndiTestUtils.bindComponent("java:comp/env/wsLocation", "wsLocation");

        mockWSSecurityConfigurationService = createMock(WSSecurityConfigurationService.class);
        mockPkiValidator = createMock(PkiValidator.class);
        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockWSAuthenticationService = createMock(WSAuthenticationService.class);
        mockPasswordDeviceServce = createMock(PasswordDeviceService.class);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockPkiValidator,
                mockApplicationAuthenticationService, mockSamlAuthorityService, mockPasswordDeviceServce };

        jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, mockWSSecurityConfigurationService);
        jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, mockPkiValidator);
        jndiTestUtils.bindComponent(ApplicationAuthenticationService.JNDI_BINDING, mockApplicationAuthenticationService);
        jndiTestUtils.bindComponent(DeviceAuthenticationService.JNDI_BINDING, mockDeviceAuthenticationService);
        jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, mockNodeAuthenticationService);
        jndiTestUtils.bindComponent(SamlAuthorityService.JNDI_BINDING, mockSamlAuthorityService);
        jndiTestUtils.bindComponent(WSAuthenticationService.JNDI_BINDING, mockWSAuthenticationService);
        jndiTestUtils.bindComponent(PasswordDeviceService.JNDI_BINDING, mockPasswordDeviceServce);

        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject()))
                                                                                                                                .andStubReturn(
                                                                                                                                        PkiResult.VALID);

        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        expect(mockWSAuthenticationService.getAuthenticationTimeout()).andStubReturn(60 * 30);
        replay(mockWSAuthenticationService);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);

        // Init Password Authentication Port
        DeviceAuthenticationPort wsPort = new PasswordAuthenticationPortImpl();
        webServiceTestUtils = new WebServiceTestUtils();
        webServiceTestUtils.setUp(wsPort);

        // Get stateful Authentication Port instance
        getWebServiceTestUtils = new WebServiceTestUtils();
        GetDeviceAuthenticationService getService = GetDeviceAuthenticationServiceFactory.newInstance();
        // Use Test implementation of get device authentication port, will set the name id mapping client to the test name id mapping
        // client
        GetDeviceAuthenticationPort wsGetPort = new GetTestPasswordAuthenticationPortImpl();
        getWebServiceTestUtils.setUp(wsGetPort);
        GetDeviceAuthenticationPort getPort = getService.getGetDeviceAuthenticationPort();
        getWebServiceTestUtils.setEndpointAddress(getPort);
        AuthenticationGetInstanceResponseType response = getPort.getInstance(new AuthenticationGetInstanceRequestType());
        W3CEndpointReference endpoint = response.getEndpoint();

        net.lin_k.safe_online.auth.DeviceAuthenticationService service = DeviceAuthenticationServiceFactory.newInstance();
        clientPort = service.getPort(endpoint, DeviceAuthenticationPort.class, new AddressingFeature(true));
        webServiceTestUtils.setEndpointAddress(clientPort);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=OLAS");
        olasPrivateKey = olasKeyPair.getPrivate();

        BindingProvider bindingProvider = (BindingProvider) clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(certificate, keyPair.getPrivate());
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);
    }

    @After
    public void tearDown()
            throws Exception {

        LOG.debug("tearDown");
        webServiceTestUtils.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // setup
        String testPassword = "secret";

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE, "test-user");
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE, testPassword);

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(testApplicationId,
                PasswordConstants.PASSWORD_DEVICE_ID, testLanguage, nameValuePairs, testpublicKey);

        // expectations
        expect(mockPasswordDeviceServce.authenticate(PasswordTestNameIdentifierMappingClientImpl.testUserId, testPassword)).andReturn(
                PasswordTestNameIdentifierMappingClientImpl.testUserId);
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        WSAuthenticationResponseType response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(PasswordConstants.PASSWORD_DEVICE_ID, response.getDeviceName());
        assertEquals(PasswordTestNameIdentifierMappingClientImpl.testUserId, response.getUserId());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testAuthenticationFailed()
            throws Exception {

        // setup
        String testPassword = "secret";

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE, "test-user");
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE, testPassword);

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(testApplicationId,
                PasswordConstants.PASSWORD_DEVICE_ID, testLanguage, nameValuePairs, testpublicKey);

        // expectations
        expect(mockPasswordDeviceServce.authenticate(PasswordTestNameIdentifierMappingClientImpl.testUserId, testPassword)).andReturn(
                null);
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        WSAuthenticationResponseType response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(PasswordConstants.PASSWORD_DEVICE_ID, response.getDeviceName());
        assertNull(response.getUserId());
        assertEquals(WSAuthenticationErrorCode.AUTHENTICATION_FAILED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testAuthenticationDeviceDisabled()
            throws Exception {

        // setup
        String testPassword = "secret";

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE, "test-user");
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE, testPassword);

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(testApplicationId,
                PasswordConstants.PASSWORD_DEVICE_ID, testLanguage, nameValuePairs, testpublicKey);

        // expectations
        expect(mockPasswordDeviceServce.authenticate(PasswordTestNameIdentifierMappingClientImpl.testUserId, testPassword)).andThrow(
                new DeviceDisabledException());
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        WSAuthenticationResponseType response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(PasswordConstants.PASSWORD_DEVICE_ID, response.getDeviceName());
        assertNull(response.getUserId());
        assertEquals(WSAuthenticationErrorCode.DEVICE_DISABLED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    private void outputAuthenticationResponse(WSAuthenticationResponseType response)
            throws Exception {

        JAXBContext context = JAXBContext.newInstance(net.lin_k.safe_online.auth.ObjectFactory.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        net.lin_k.safe_online.auth.ObjectFactory objectFactory = new net.lin_k.safe_online.auth.ObjectFactory();
        marshaller.marshal(objectFactory.createWSAuthenticationResponse(response), stringWriter);
        LOG.debug("response: " + stringWriter);
    }

}
