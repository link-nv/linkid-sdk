/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sts.ws;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.device.sdk.auth.saml2.response.AuthnResponseFactory;
import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.operation.saml2.request.DeviceOperationRequestFactory;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponseFactory;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;
import net.link.safeonline.sts.ws.SecurityTokenServiceConstants;
import net.link.safeonline.sts.ws.SecurityTokenServiceFactory;
import net.link.safeonline.sts.ws.SecurityTokenServicePortImpl;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ObjectFactory;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenResponseType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.RequestSecurityTokenType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenService;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenServicePort;
import org.oasis_open.docs.ws_sx.ws_trust._200512.StatusType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ValidateTargetType;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class SecurityTokenServicePortImplTest {

    static final Log                         LOG = LogFactory.getLog(SecurityTokenServicePortImplTest.class);

    private WebServiceTestUtils              webServiceTestUtils;

    private JndiTestUtils                    jndiTestUtils;

    private WSSecurityConfigurationService   mockWSSecurityConfigurationService;

    private ApplicationAuthenticationService mockApplicationAuthenticationService;

    private NodeAuthenticationService        mockNodeAuthenticationService;

    private PkiValidator                     mockPkiValidator;

    private Object[]                         mockObjects;

    private KeyService                       mockKeyService;

    private KeyPair                          nodeKeyPair;

    private X509Certificate                  nodeCertificate;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", "SafeOnline/WSSecurityConfigurationBean/local");
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);

        mockWSSecurityConfigurationService = createMock(WSSecurityConfiguration.class);
        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockPkiValidator = createMock(PkiValidator.class);
        mockKeyService = createMock(KeyService.class);

        nodeKeyPair = PkiTestUtils.generateKeyPair();
        nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockApplicationAuthenticationService,
                mockNodeAuthenticationService, mockPkiValidator };

        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);
        jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, mockWSSecurityConfigurationService);
        jndiTestUtils.bindComponent(ApplicationAuthenticationService.JNDI_BINDING, mockApplicationAuthenticationService);
        jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, mockNodeAuthenticationService);
        jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, mockPkiValidator);

        webServiceTestUtils = new WebServiceTestUtils();

        SecurityTokenServicePort port = new SecurityTokenServicePortImpl();
        webServiceTestUtils.setUp(port);

        String testNodeName = "test-node-name";
        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, nodeCertificate))
                                                                                                                                .andStubReturn(
                                                                                                                                        PkiResult.INVALID);
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN, nodeCertificate)).andStubReturn(
                PkiResult.VALID);
        expect(mockNodeAuthenticationService.authenticate(nodeCertificate)).andStubReturn(testNodeName);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(nodeCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(nodeCertificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(nodeCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(nodeKeyPair.getPrivate());

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
    }

    @After
    public void tearDown()
            throws Exception {

        webServiceTestUtils.tearDown();
        jndiTestUtils.tearDown();
    }

    @BeforeClass
    public static void classSetUp()
            throws Exception {

        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException("could not bootstrap the OpenSAML2 library");
        }
    }

    @SuppressWarnings( { "unchecked", "null" })
    @Test
    public void testWSAuthnRequest()
            throws Exception {

        // setup
        String testIssuer = "test-issuer";
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();
        SecurityTokenServicePort port = service.getSecurityTokenServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceTestUtils.getEndpointAddress());
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new WSSecurityClientHandler(nodeCertificate, nodeKeyPair.getPrivate()));
        handlers.add(new LoggingHandler());
        handlers.add(new SignatureVerificationTestHandler());

        bindingProvider.getBinding().setHandlerChain(handlers);

        expect(mockNodeAuthenticationService.getCertificates(testIssuer)).andStubReturn(Collections.singletonList(nodeCertificate));

        // prepare
        replay(mockObjects);

        // operate
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate#"
                + TrustDomainType.NODE);
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        request.getAny().add(requestType);
        JAXBElement<String> tokenType = objectFactory.createTokenType(SecurityTokenServiceConstants.TOKEN_TYPE_STATUS);
        request.getAny().add(tokenType);
        ValidateTargetType validateTarget = new ValidateTargetType();

        Element requestToken = createAuthnRequest(testIssuer, "test-application-id", "http://test.consumer/url",
                "http://test.destination/url");
        validateTarget.setAny(requestToken);
        request.getAny().add(objectFactory.createValidateTarget(validateTarget));

        RequestSecurityTokenResponseType response = port.requestSecurityToken(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results) {
            if (result instanceof JAXBElement) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType) {
                    status = (StatusType) value;
                }
            }
        }
        assertNotNull(status);
        String statusCode = status.getCode();
        assertEquals(SecurityTokenServiceConstants.STATUS_VALID, statusCode);
    }

    @SuppressWarnings( { "unchecked", "null" })
    @Test
    public void testWSAuthnResponse()
            throws Exception {

        // setup
        String testIssuer = "test-issuer";
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();
        SecurityTokenServicePort port = service.getSecurityTokenServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceTestUtils.getEndpointAddress());
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new WSSecurityClientHandler(nodeCertificate, nodeKeyPair.getPrivate()));
        handlers.add(new LoggingHandler());
        handlers.add(new SignatureVerificationTestHandler());

        bindingProvider.getBinding().setHandlerChain(handlers);

        expect(mockNodeAuthenticationService.getCertificates(testIssuer)).andStubReturn(Collections.singletonList(nodeCertificate));

        // prepare
        replay(mockObjects);

        // operate
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate#"
                + TrustDomainType.NODE);
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        request.getAny().add(requestType);
        JAXBElement<String> tokenType = objectFactory.createTokenType(SecurityTokenServiceConstants.TOKEN_TYPE_STATUS);
        request.getAny().add(tokenType);
        ValidateTargetType validateTarget = new ValidateTargetType();

        Element responseToken = createAuthnResponse("test-in-response-to", testIssuer, "test-subject", 60, "http://test.target/url");
        validateTarget.setAny(responseToken);
        request.getAny().add(objectFactory.createValidateTarget(validateTarget));

        RequestSecurityTokenResponseType response = port.requestSecurityToken(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results) {
            if (result instanceof JAXBElement) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType) {
                    status = (StatusType) value;
                }
            }
        }
        assertNotNull(status);
        String statusCode = status.getCode();
        assertEquals(SecurityTokenServiceConstants.STATUS_VALID, statusCode);
    }

    @SuppressWarnings( { "unchecked", "null" })
    @Test
    public void testWSLogoutRequest()
            throws Exception {

        // setup
        String testIssuer = "test-issuer";
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();
        SecurityTokenServicePort port = service.getSecurityTokenServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceTestUtils.getEndpointAddress());
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new WSSecurityClientHandler(nodeCertificate, nodeKeyPair.getPrivate()));
        handlers.add(new LoggingHandler());
        handlers.add(new SignatureVerificationTestHandler());

        bindingProvider.getBinding().setHandlerChain(handlers);

        expect(mockNodeAuthenticationService.getCertificates(testIssuer)).andStubReturn(Collections.singletonList(nodeCertificate));

        // prepare
        replay(mockObjects);

        // operate
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate#"
                + TrustDomainType.NODE);
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        request.getAny().add(requestType);
        JAXBElement<String> tokenType = objectFactory.createTokenType(SecurityTokenServiceConstants.TOKEN_TYPE_STATUS);
        request.getAny().add(tokenType);
        ValidateTargetType validateTarget = new ValidateTargetType();

        Element requestToken = createLogoutRequest("test-subject", testIssuer, "test-destination");
        validateTarget.setAny(requestToken);
        request.getAny().add(objectFactory.createValidateTarget(validateTarget));

        RequestSecurityTokenResponseType response = port.requestSecurityToken(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results) {
            if (result instanceof JAXBElement) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType) {
                    status = (StatusType) value;
                }
            }
        }
        assertNotNull(status);
        String statusCode = status.getCode();
        assertEquals(SecurityTokenServiceConstants.STATUS_VALID, statusCode);
    }

    @SuppressWarnings( { "unchecked", "null" })
    @Test
    public void testWSLogoutResponse()
            throws Exception {

        // setup
        String testIssuer = "test-issuer";
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();
        SecurityTokenServicePort port = service.getSecurityTokenServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceTestUtils.getEndpointAddress());
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new WSSecurityClientHandler(nodeCertificate, nodeKeyPair.getPrivate()));
        handlers.add(new LoggingHandler());
        handlers.add(new SignatureVerificationTestHandler());

        bindingProvider.getBinding().setHandlerChain(handlers);

        expect(mockNodeAuthenticationService.getCertificates(testIssuer)).andStubReturn(Collections.singletonList(nodeCertificate));

        // prepare
        replay(mockObjects);

        // operate
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate#"
                + TrustDomainType.NODE);
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        request.getAny().add(requestType);
        JAXBElement<String> tokenType = objectFactory.createTokenType(SecurityTokenServiceConstants.TOKEN_TYPE_STATUS);
        request.getAny().add(tokenType);
        ValidateTargetType validateTarget = new ValidateTargetType();

        Element responseToken = createLogoutResponse("test-inresponse-to", testIssuer, "test-target");
        validateTarget.setAny(responseToken);
        request.getAny().add(objectFactory.createValidateTarget(validateTarget));

        RequestSecurityTokenResponseType response = port.requestSecurityToken(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results) {
            if (result instanceof JAXBElement) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType) {
                    status = (StatusType) value;
                }
            }
        }
        assertNotNull(status);
        String statusCode = status.getCode();
        assertEquals(SecurityTokenServiceConstants.STATUS_VALID, statusCode);
    }

    @SuppressWarnings( { "unchecked", "null" })
    @Test
    public void testWSDeviceOperationRequest()
            throws Exception {

        // setup
        String testIssuer = "test-issuer";
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();
        SecurityTokenServicePort port = service.getSecurityTokenServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceTestUtils.getEndpointAddress());
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new WSSecurityClientHandler(nodeCertificate, nodeKeyPair.getPrivate()));
        handlers.add(new LoggingHandler());
        handlers.add(new SignatureVerificationTestHandler());

        bindingProvider.getBinding().setHandlerChain(handlers);

        expect(mockNodeAuthenticationService.getCertificates(testIssuer)).andStubReturn(Collections.singletonList(nodeCertificate));

        // prepare
        replay(mockObjects);

        // operate
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate#"
                + TrustDomainType.NODE);
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        request.getAny().add(requestType);
        JAXBElement<String> tokenType = objectFactory.createTokenType(SecurityTokenServiceConstants.TOKEN_TYPE_STATUS);
        request.getAny().add(tokenType);
        ValidateTargetType validateTarget = new ValidateTargetType();

        Element requestToken = createDeviceOperationRequest(testIssuer, UUID.randomUUID().toString(), "http://test.consumer/url",
                "test-destination");
        validateTarget.setAny(requestToken);
        request.getAny().add(objectFactory.createValidateTarget(validateTarget));

        RequestSecurityTokenResponseType response = port.requestSecurityToken(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results) {
            if (result instanceof JAXBElement) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType) {
                    status = (StatusType) value;
                }
            }
        }
        assertNotNull(status);
        String statusCode = status.getCode();
        assertEquals(SecurityTokenServiceConstants.STATUS_VALID, statusCode);
    }

    @SuppressWarnings( { "unchecked", "null" })
    @Test
    public void testWSDeviceOperationResponse()
            throws Exception {

        // setup
        String testIssuer = "test-issuer";
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();
        SecurityTokenServicePort port = service.getSecurityTokenServicePort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webServiceTestUtils.getEndpointAddress());
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new WSSecurityClientHandler(nodeCertificate, nodeKeyPair.getPrivate()));
        handlers.add(new LoggingHandler());
        handlers.add(new SignatureVerificationTestHandler());

        bindingProvider.getBinding().setHandlerChain(handlers);

        expect(mockNodeAuthenticationService.getCertificates(testIssuer)).andStubReturn(Collections.singletonList(nodeCertificate));

        // prepare
        replay(mockObjects);

        // operate
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> requestType = objectFactory.createRequestType("http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate#"
                + TrustDomainType.NODE);
        RequestSecurityTokenType request = new RequestSecurityTokenType();
        request.getAny().add(requestType);
        JAXBElement<String> tokenType = objectFactory.createTokenType(SecurityTokenServiceConstants.TOKEN_TYPE_STATUS);
        request.getAny().add(tokenType);
        ValidateTargetType validateTarget = new ValidateTargetType();

        Element responseToken = createDeviceOperationResponse("test-inresponse-to", testIssuer, "test-subject", 60, "test-target");
        validateTarget.setAny(responseToken);
        request.getAny().add(objectFactory.createValidateTarget(validateTarget));

        RequestSecurityTokenResponseType response = port.requestSecurityToken(request);

        // verify
        verify(mockObjects);
        assertNotNull(response);
        StatusType status = null;
        List<Object> results = response.getAny();
        for (Object result : results) {
            if (result instanceof JAXBElement) {
                JAXBElement<?> resultElement = (JAXBElement<?>) result;
                Object value = resultElement.getValue();
                if (value instanceof StatusType) {
                    status = (StatusType) value;
                }
            }
        }
        assertNotNull(status);
        String statusCode = status.getCode();
        assertEquals(SecurityTokenServiceConstants.STATUS_VALID, statusCode);
    }

    private Element createAuthnRequest(String issuerName, String applicationName, String assertionConsumerServiceURL, String destinationURL)
            throws Exception {

        Challenge<String> challenge = new Challenge<String>();
        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(issuerName, null, applicationName, nodeKeyPair,
                assertionConsumerServiceURL, destinationURL, challenge, Collections.singleton("test-device-name"), false, null);
        Document doc = DomUtils.parseDocument(encodedAuthnRequest);
        return doc.getDocumentElement();
    }

    private Element createAuthnResponse(String inResponseTo, String issuerName, String subjectName, int validity, String target)
            throws Exception {

        String encodedAuthnResponse = AuthnResponseFactory.createAuthResponse(inResponseTo, issuerName, issuerName, subjectName,
                SafeOnlineConstants.PKI_DEVICE_AUTH_CONTEXT_CLASS, nodeKeyPair, validity, target);
        Document doc = DomUtils.parseDocument(encodedAuthnResponse);
        return doc.getDocumentElement();
    }

    private Element createLogoutRequest(String subjectName, String issuerName, String destinationURL)
            throws Exception {

        Challenge<String> challenge = new Challenge<String>();
        String encodedLogoutRequest = LogoutRequestFactory.createLogoutRequest(subjectName, issuerName, nodeKeyPair, destinationURL,
                challenge, null);
        Document doc = DomUtils.parseDocument(encodedLogoutRequest);
        return doc.getDocumentElement();
    }

    private Element createLogoutResponse(String inResponseTo, String issuerName, String target)
            throws Exception {

        String encodedLogoutResponse = LogoutResponseFactory.createLogoutResponse(inResponseTo, issuerName, nodeKeyPair, target);
        Document doc = DomUtils.parseDocument(encodedLogoutResponse);
        return doc.getDocumentElement();
    }

    private Element createDeviceOperationRequest(String issuerName, String subjectName, String serviceURL, String destinationURL)
            throws Exception {

        Challenge<String> challenge = new Challenge<String>();
        String device = "test-device";
        String authenticatedDevice = "test-authenticated-device";
        String encodedRequest = DeviceOperationRequestFactory.createDeviceOperationRequest(issuerName, subjectName, nodeKeyPair,
                serviceURL, destinationURL, DeviceOperationType.REGISTER, challenge, device,
                Collections.singletonList(authenticatedDevice), null, null);
        Document doc = DomUtils.parseDocument(encodedRequest);
        return doc.getDocumentElement();
    }

    private Element createDeviceOperationResponse(String inResponseTo, String issuerName, String subjectName, int validity, String target)
            throws Exception {

        String device = "test-device";
        String encodedResponse = DeviceOperationResponseFactory.createDeviceOperationResponse(inResponseTo, DeviceOperationType.REGISTER,
                issuerName, subjectName, device, nodeKeyPair, validity, target);
        Document doc = DomUtils.parseDocument(encodedResponse);
        return doc.getDocumentElement();
    }

}
