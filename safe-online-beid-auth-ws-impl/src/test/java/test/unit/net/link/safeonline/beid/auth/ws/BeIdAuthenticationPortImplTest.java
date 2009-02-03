/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.beid.auth.ws;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
import net.link.safeonline.authentication.service.AuthenticationStatement;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.WSAuthenticationService;
import net.link.safeonline.beid.auth.ws.BeIdAuthenticationPortImpl;
import net.link.safeonline.beid.auth.ws.GetBeIdAuthenticationPortImpl;
import net.link.safeonline.device.auth.ws.DeviceAuthenticationServiceFactory;
import net.link.safeonline.device.auth.ws.GetDeviceAuthenticationServiceFactory;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.beid.BeIdDeviceService;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sdk.ws.auth.AuthenticationUtil;
import net.link.safeonline.shared.JceSigner;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class BeIdAuthenticationPortImplTest {

    private static final Log                 LOG               = LogFactory.getLog(BeIdAuthenticationPortImplTest.class);

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

    private BeIdDeviceService                mockBeIdDeviceService;

    private KeyService                       mockKeyService;

    private Object[]                         mockObjects;

    private KeyPair                          testKeyPair;

    private X509Certificate                  testCertificate;

    private KeyPair                          olasKeyPair;
    private X509Certificate                  olasCertificate;

    private String                           testLanguage      = Locale.ENGLISH.getLanguage();

    private String                           testIssuerName    = "test-issuer-name";

    private String                           testApplicationId = "test-application-name";


    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
            throws Exception {

        LOG.debug("setup");

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
        mockBeIdDeviceService = createMock(BeIdDeviceService.class);
        mockKeyService = createMock(KeyService.class);

        olasKeyPair = PkiTestUtils.generateKeyPair();
        olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineKeyStore.class)).andReturn(
                new PrivateKeyEntry(olasKeyPair.getPrivate(), new Certificate[] { olasCertificate }));

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockPkiValidator, mockApplicationAuthenticationService,
                mockSamlAuthorityService, mockBeIdDeviceService };

        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);
        jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, mockWSSecurityConfigurationService);
        jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, mockPkiValidator);
        jndiTestUtils.bindComponent(ApplicationAuthenticationService.JNDI_BINDING, mockApplicationAuthenticationService);
        jndiTestUtils.bindComponent(DeviceAuthenticationService.JNDI_BINDING, mockDeviceAuthenticationService);
        jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, mockNodeAuthenticationService);
        jndiTestUtils.bindComponent(SamlAuthorityService.JNDI_BINDING, mockSamlAuthorityService);
        jndiTestUtils.bindComponent(WSAuthenticationService.JNDI_BINDING, mockWSAuthenticationService);
        jndiTestUtils.bindComponent(BeIdDeviceService.JNDI_BINDING, mockBeIdDeviceService);

        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        expect(mockWSAuthenticationService.getAuthenticationTimeout()).andStubReturn(60 * 30);
        replay(mockWSAuthenticationService);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);

        // Init BeId Authentication Port
        DeviceAuthenticationPort wsPort = new BeIdAuthenticationPortImpl();
        webServiceTestUtils = new WebServiceTestUtils();
        webServiceTestUtils.setUp(wsPort);

        // Get stateful Authentication Port instance
        getWebServiceTestUtils = new WebServiceTestUtils();
        GetDeviceAuthenticationService getService = GetDeviceAuthenticationServiceFactory.newInstance();
        GetDeviceAuthenticationPort wsGetPort = new GetBeIdAuthenticationPortImpl();
        getWebServiceTestUtils.setUp(wsGetPort);
        GetDeviceAuthenticationPort getPort = getService.getGetDeviceAuthenticationPort();
        getWebServiceTestUtils.setEndpointAddress(getPort);
        AuthenticationGetInstanceResponseType response = getPort.getInstance(new AuthenticationGetInstanceRequestType());
        W3CEndpointReference endpoint = response.getEndpoint();

        net.lin_k.safe_online.auth.DeviceAuthenticationService service = DeviceAuthenticationServiceFactory.newInstance();
        clientPort = service.getPort(endpoint, DeviceAuthenticationPort.class, new AddressingFeature(true));
        webServiceTestUtils.setEndpointAddress(clientPort);

        testKeyPair = PkiTestUtils.generateKeyPair();
        testCertificate = PkiTestUtils.generateSelfSignedCertificate(testKeyPair, "CN=Test");

        BindingProvider bindingProvider = (BindingProvider) clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(testCertificate, testKeyPair.getPrivate());
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
        Signer signer = new JceSigner(testKeyPair.getPrivate(), testCertificate);
        String testUserId = UUID.randomUUID().toString();

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(testApplicationId, BeIdConstants.BEID_DEVICE_ID,
                testLanguage, null, testKeyPair.getPublic());

        // expectations
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(testCertificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasKeyPair.getPrivate());

        // prepare
        replay(mockObjects);

        // operate
        WSAuthenticationResponseType response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(BeIdConstants.BEID_DEVICE_ID, response.getDeviceName());
        assertNull(response.getUserId());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());
        assertNotNull(response.getDeviceAuthenticationInformation());
        assertEquals(BeIdConstants.BEID_WS_AUTH_SESSION_ID_ATTRIBUTE, response.getDeviceAuthenticationInformation().getNameValuePair().get(
                0).getName());

        String sessionId = response.getDeviceAuthenticationInformation().getNameValuePair().get(0).getValue();

        outputAuthenticationResponse(response);

        // reset
        reset(mockObjects);

        // setup
        net.link.safeonline.shared.statement.AuthenticationStatement testAuthenticationStatement = new net.link.safeonline.shared.statement.AuthenticationStatement(
                sessionId, testApplicationId, signer);

        byte[] encodedAuthenticationStatement = testAuthenticationStatement.generateStatement();
        // set length to 0 so no wrapping occurs, else the signature becomes invalid ...
        String base64EncodedAuthenticationStatement = Base64.encode(encodedAuthenticationStatement, 0);

        Map<String, String> deviceCredentials = new HashMap<String, String>();
        deviceCredentials.put(BeIdConstants.BEID_WS_AUTH_STATEMENT_ATTRIBUTE, new String(base64EncodedAuthenticationStatement));

        request = AuthenticationUtil.getAuthenticationRequest(testApplicationId, BeIdConstants.BEID_DEVICE_ID, testLanguage,
                deviceCredentials, testKeyPair.getPublic());

        outputAuthenticationRequest(request);

        // expectations
        expect(
                mockBeIdDeviceService.authenticate((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                        (AuthenticationStatement) EasyMock.anyObject())).andReturn(testUserId);
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(testCertificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasKeyPair.getPrivate());
        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        // prepare
        replay(mockObjects);

        // operate
        response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(BeIdConstants.BEID_DEVICE_ID, response.getDeviceName());
        assertEquals(testUserId, response.getUserId());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testAuthenticationFailedInvalidStatement()
            throws Exception {

        // setup
        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(testApplicationId, BeIdConstants.BEID_DEVICE_ID,
                testLanguage, null, testKeyPair.getPublic());

        // expectations
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(testCertificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasKeyPair.getPrivate());

        // prepare
        replay(mockObjects);

        // operate
        WSAuthenticationResponseType response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(BeIdConstants.BEID_DEVICE_ID, response.getDeviceName());
        assertNull(response.getUserId());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());
        assertNotNull(response.getDeviceAuthenticationInformation());
        assertEquals(BeIdConstants.BEID_WS_AUTH_SESSION_ID_ATTRIBUTE, response.getDeviceAuthenticationInformation().getNameValuePair().get(
                0).getName());

        outputAuthenticationResponse(response);

        // reset
        reset(mockObjects);

        // setup
        String fooStatement = "foo-statement";

        Map<String, String> deviceCredentials = new HashMap<String, String>();
        deviceCredentials.put(BeIdConstants.BEID_WS_AUTH_STATEMENT_ATTRIBUTE, fooStatement);

        request = AuthenticationUtil.getAuthenticationRequest(testApplicationId, BeIdConstants.BEID_DEVICE_ID, testLanguage,
                deviceCredentials, testKeyPair.getPublic());

        outputAuthenticationRequest(request);

        // expectations
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(testCertificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasKeyPair.getPrivate());
        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        // prepare
        replay(mockObjects);

        // operate
        response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(BeIdConstants.BEID_DEVICE_ID, response.getDeviceName());
        assertNull(response.getUserId());
        assertEquals(WSAuthenticationErrorCode.INVALID_CREDENTIALS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testAuthenticationFailedDeviceDisabled()
            throws Exception {

        // setup
        Signer signer = new JceSigner(testKeyPair.getPrivate(), testCertificate);

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(testApplicationId, BeIdConstants.BEID_DEVICE_ID,
                testLanguage, null, testKeyPair.getPublic());

        // expectations
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(testCertificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasKeyPair.getPrivate());

        // prepare
        replay(mockObjects);

        // operate
        WSAuthenticationResponseType response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(BeIdConstants.BEID_DEVICE_ID, response.getDeviceName());
        assertNull(response.getUserId());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());
        assertNotNull(response.getDeviceAuthenticationInformation());
        assertEquals(BeIdConstants.BEID_WS_AUTH_SESSION_ID_ATTRIBUTE, response.getDeviceAuthenticationInformation().getNameValuePair().get(
                0).getName());

        String sessionId = response.getDeviceAuthenticationInformation().getNameValuePair().get(0).getValue();

        outputAuthenticationResponse(response);

        // reset
        reset(mockObjects);

        // setup
        net.link.safeonline.shared.statement.AuthenticationStatement testAuthenticationStatement = new net.link.safeonline.shared.statement.AuthenticationStatement(
                sessionId, testApplicationId, signer);

        byte[] encodedAuthenticationStatement = testAuthenticationStatement.generateStatement();
        // set length to 0 so no wrapping occurs, else the signature becomes invalid ...
        String base64EncodedAuthenticationStatement = Base64.encode(encodedAuthenticationStatement, 0);

        Map<String, String> deviceCredentials = new HashMap<String, String>();
        deviceCredentials.put(BeIdConstants.BEID_WS_AUTH_STATEMENT_ATTRIBUTE, new String(base64EncodedAuthenticationStatement));

        request = AuthenticationUtil.getAuthenticationRequest(testApplicationId, BeIdConstants.BEID_DEVICE_ID, testLanguage,
                deviceCredentials, testKeyPair.getPublic());

        outputAuthenticationRequest(request);

        // expectations
        expect(
                mockBeIdDeviceService.authenticate((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                        (AuthenticationStatement) EasyMock.anyObject())).andThrow(new DeviceDisabledException());
        expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);
        expect(mockApplicationAuthenticationService.authenticate(testCertificate)).andReturn("test-application-name");
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(testCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasKeyPair.getPrivate());
        expect(mockPkiValidator.validateCertificate((String) EasyMock.anyObject(), (X509Certificate) EasyMock.anyObject())).andStubReturn(
                PkiResult.VALID);
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        // prepare
        replay(mockObjects);

        // operate
        response = clientPort.authenticate(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        assertEquals(BeIdConstants.BEID_DEVICE_ID, response.getDeviceName());
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

    private void outputAuthenticationRequest(WSAuthenticationRequestType request)
            throws Exception {

        JAXBContext context = JAXBContext.newInstance(net.lin_k.safe_online.auth.ObjectFactory.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        net.lin_k.safe_online.auth.ObjectFactory objectFactory = new net.lin_k.safe_online.auth.ObjectFactory();
        marshaller.marshal(objectFactory.createWSAuthenticationRequest(request), stringWriter);
        LOG.debug("request: " + stringWriter);
    }

}
