/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationGetInstanceRequestType;
import net.lin_k.safe_online.auth.AuthenticationGetInstanceResponseType;
import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.AuthenticationService;
import net.lin_k.safe_online.auth.GetAuthenticationPort;
import net.lin_k.safe_online.auth.GetAuthenticationService;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationMissingAttributesRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationMissingAttributesSaveRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementResponseType;
import net.link.safeonline.auth.ws.AuthenticationPortImpl;
import net.link.safeonline.auth.ws.AuthenticationServiceFactory;
import net.link.safeonline.auth.ws.AuthenticationStep;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.auth.ws.GetAuthenticationServiceFactory;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.saml.common.Saml2SubjectConfirmationMethod;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.sdk.ws.auth.AuthenticationUtil;
import net.link.safeonline.sdk.ws.auth.DataType;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.AuthnStatementType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.SubjectConfirmationType;
import oasis.names.tc.saml._2_0.assertion.SubjectType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3._2000._09.xmldsig_.KeyInfoType;


public class AuthenticationPortImplTest {

    private static final Log               LOG                           = LogFactory.getLog(AuthenticationPortImplTest.class);

    private WebServiceTestUtils            webServiceTestUtils;
    private WebServiceTestUtils            getWebServiceTestUtils;

    private AuthenticationPort             clientPort;

    private JndiTestUtils                  jndiTestUtils;

    private WSSecurityConfigurationService mockWSSecurityConfigurationService;
    private PkiValidator                   mockPkiValidator;
    private SamlAuthorityService           mockSamlAuthorityService;
    private DevicePolicyService            mockDevicePolicyService;
    private NodeAuthenticationService      mockNodeAuthenticationService;
    private NodeMappingService             mockNodeMappingService;
    private SubjectService                 mockSubjectService;
    private UsageAgreementService          mockUsageAgreementService;
    private SubscriptionService            mockSubscriptionService;
    private IdentityService                mockIdentityService;
    private UserIdMappingService           mockUserIdMappingService;

    private Object[]                       mockObjects;

    private PublicKey                      testpublicKey;

    private X509Certificate                olasCertificate;

    private PrivateKey                     olasPrivateKey;

    private String                         testLanguage                  = Locale.ENGLISH.getLanguage();

    private String                         testApplicationId             = "test-application-name";

    private String                         testIssuerName                = "test-issuer-name";

    private String                         testSingleStringAttributeName = "test-single-string-attribute";
    private String                         testMultiStringAttributeName  = "test-multi-string-attribute";
    private String                         testMultiDateAttributeName    = "test-multi-date-attribute";
    private String                         testCompoundAttributeName     = "test-compound-attribute";


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

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", true);

        this.mockWSSecurityConfigurationService = createMock(WSSecurityConfigurationService.class);
        this.mockPkiValidator = createMock(PkiValidator.class);
        this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        this.mockDevicePolicyService = createMock(DevicePolicyService.class);
        this.mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        this.mockNodeMappingService = createMock(NodeMappingService.class);
        this.mockSubjectService = createMock(SubjectService.class);
        this.mockUsageAgreementService = createMock(UsageAgreementService.class);
        this.mockSubscriptionService = createMock(SubscriptionService.class);
        this.mockIdentityService = createMock(IdentityService.class);
        this.mockUserIdMappingService = createMock(UserIdMappingService.class);

        this.mockObjects = new Object[] { this.mockWSSecurityConfigurationService, this.mockPkiValidator, this.mockSamlAuthorityService,
                this.mockDevicePolicyService, this.mockNodeAuthenticationService, this.mockNodeMappingService, this.mockSubjectService,
                this.mockUsageAgreementService, this.mockSubscriptionService, this.mockIdentityService, this.mockUserIdMappingService };

        this.jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, this.mockWSSecurityConfigurationService);
        this.jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, this.mockPkiValidator);
        this.jndiTestUtils.bindComponent(SamlAuthorityService.JNDI_BINDING, this.mockSamlAuthorityService);
        this.jndiTestUtils.bindComponent(DevicePolicyService.JNDI_BINDING, this.mockDevicePolicyService);
        this.jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, this.mockNodeAuthenticationService);
        this.jndiTestUtils.bindComponent(NodeMappingService.JNDI_BINDING, this.mockNodeMappingService);
        this.jndiTestUtils.bindComponent(SubjectService.JNDI_BINDING, this.mockSubjectService);
        this.jndiTestUtils.bindComponent(UsageAgreementService.JNDI_BINDING, this.mockUsageAgreementService);
        this.jndiTestUtils.bindComponent(SubscriptionService.JNDI_BINDING, this.mockSubscriptionService);
        this.jndiTestUtils.bindComponent(IdentityService.JNDI_BINDING, this.mockIdentityService);
        this.jndiTestUtils.bindComponent(UserIdMappingService.JNDI_BINDING, this.mockUserIdMappingService);

        // Init Authentication Port
        AuthenticationPort wsPort = new AuthenticationPortImpl();
        this.webServiceTestUtils = new WebServiceTestUtils();
        this.webServiceTestUtils.setUp(wsPort);

        // Get stateful Authentication Port instance
        this.getWebServiceTestUtils = new WebServiceTestUtils();
        GetAuthenticationService getService = GetAuthenticationServiceFactory.newInstance();
        // Use Test implementation of get authentication port, will set the device authentication client to the test device authentication
        // client
        GetAuthenticationPort wsGetPort = new GetTestAuthenticationPortImpl();
        this.getWebServiceTestUtils.setUp(wsGetPort);
        GetAuthenticationPort getPort = getService.getGetAuthenticationPort();
        this.getWebServiceTestUtils.setEndpointAddress(getPort);
        AuthenticationGetInstanceResponseType response = getPort.getInstance(new AuthenticationGetInstanceRequestType());
        W3CEndpointReference endpoint = response.getEndpoint();

        AuthenticationService service = AuthenticationServiceFactory.newInstance();
        this.clientPort = service.getPort(endpoint, AuthenticationPort.class, new AddressingFeature(true));
        this.webServiceTestUtils.setEndpointAddress(this.clientPort);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        this.testpublicKey = keyPair.getPublic();

        KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        this.olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=OLAS");
        this.olasPrivateKey = olasKeyPair.getPrivate();

        BindingProvider bindingProvider = (BindingProvider) this.clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(null, null);
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);
    }

    @After
    public void tearDown()
            throws Exception {

        LOG.debug("tearDown");
        this.webServiceTestUtils.tearDown();
        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // setup
        NodeEntity localNode = new NodeEntity();
        DeviceClassEntity testDeviceClass = new DeviceClassEntity("test-device-class", "test-device-auth-context-class");
        DeviceEntity testDevice = new DeviceEntity(DeviceTestAuthenticationClientImpl.testDeviceName, testDeviceClass, localNode, null,
                null, null, null, null, null, null, null);
        SubjectEntity testSubject = new SubjectEntity(DeviceTestAuthenticationClientImpl.testUserId);

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put("foo", "bar");

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(this.testApplicationId,
                DeviceTestAuthenticationClientImpl.testDeviceName, this.testLanguage, nameValuePairs, this.testpublicKey);

        // expectations
        expect(this.mockDevicePolicyService.getAuthenticationWSURL(DeviceTestAuthenticationClientImpl.testDeviceName)).andStubReturn("foo");
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockNodeAuthenticationService.getLocalNode()).andStubReturn(localNode);
        expect(this.mockDevicePolicyService.getDevice(DeviceTestAuthenticationClientImpl.testDeviceName)).andStubReturn(testDevice);
        expect(this.mockSubjectService.getSubject(DeviceTestAuthenticationClientImpl.testUserId)).andStubReturn(testSubject);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(false);
        expect(this.mockSubscriptionService.isSubscribed(this.testApplicationId)).andStubReturn(true);
        expect(this.mockUsageAgreementService.requiresUsageAgreementAcceptation(this.testApplicationId, this.testLanguage)).andStubReturn(
                false);
        expect(this.mockIdentityService.isConfirmationRequired(this.testApplicationId)).andStubReturn(false);
        expect(this.mockIdentityService.hasMissingAttributes(this.testApplicationId)).andStubReturn(false);
        expect(this.mockUserIdMappingService.getApplicationUserId(this.testApplicationId, DeviceTestAuthenticationClientImpl.testUserId))
                                                                                                                                         .andStubReturn(
                                                                                                                                                 DeviceTestAuthenticationClientImpl.testUserId);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.authenticate(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        verifySuccessFullAuthenticationResponse(response, DeviceTestAuthenticationClientImpl.testDeviceName, testSubject.getUserId(),
                this.testpublicKey);

        outputAuthenticationResponse(response);
    }

    @Test
    public void testRequestGlobalUsageAgreementNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationGlobalUsageAgreementRequestType request = AuthenticationUtil.getGlobalUsageAgreementRequest();

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationGlobalUsageAgreementResponseType response = this.clientPort.requestGlobalUsageAgreement(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testConfirmGlobalUsageAgreementNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationGlobalUsageAgreementConfirmationType request = AuthenticationUtil
                                                                                         .getGlobalUsageAgreementConfirmationRequest(Confirmation.CONFIRM);

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.confirmGlobalUsageAgreement(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testRequestUsageAgreementNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationUsageAgreementRequestType request = AuthenticationUtil.getUsageAgreementRequest();

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.requestUsageAgreement(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testConfirmUsageAgreementNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationUsageAgreementConfirmationType request = AuthenticationUtil
                                                                                   .getUsageAgreementConfirmationRequest(Confirmation.CONFIRM);

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.confirmUsageAgreement(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testRequestIdentityConfirmationNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationIdentityRequestType request = AuthenticationUtil.getIdentityRequest();

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.requestIdentity(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testConfirmIdentityNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationIdentityConfirmationType request = AuthenticationUtil.getIdentityConfirmationRequest(Confirmation.CONFIRM);

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.confirmIdentity(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testRequestMissingAttributesNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationMissingAttributesRequestType request = AuthenticationUtil.getMissingAttributesRequest();

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.requestMissingAttributes(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testSaveMissingAttributesNotAuthenticated()
            throws Exception {

        // setup
        WSAuthenticationMissingAttributesSaveRequestType request = AuthenticationUtil.getMissingAttributesSaveRequest(null);

        // expectations
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.saveMissingAttributes(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        assertEquals(WSAuthenticationErrorCode.NOT_AUTHENTICATED.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);
    }

    @Test
    public void testRequestConfirmGlobalUsageAgreement()
            throws Exception {

        /*
         * Authenticate
         */
        NodeEntity localNode = new NodeEntity();
        DeviceClassEntity testDeviceClass = new DeviceClassEntity("test-device-class", "test-device-auth-context-class");
        DeviceEntity testDevice = new DeviceEntity(DeviceTestAuthenticationClientImpl.testDeviceName, testDeviceClass, localNode, null,
                null, null, null, null, null, null, null);
        SubjectEntity testSubject = new SubjectEntity(DeviceTestAuthenticationClientImpl.testUserId);

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put("foo", "bar");

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(this.testApplicationId,
                DeviceTestAuthenticationClientImpl.testDeviceName, this.testLanguage, nameValuePairs, this.testpublicKey);

        // expectations
        expect(this.mockDevicePolicyService.getAuthenticationWSURL(DeviceTestAuthenticationClientImpl.testDeviceName)).andStubReturn("foo");
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockNodeAuthenticationService.getLocalNode()).andStubReturn(localNode);
        expect(this.mockDevicePolicyService.getDevice(DeviceTestAuthenticationClientImpl.testDeviceName)).andStubReturn(testDevice);
        expect(this.mockSubjectService.getSubject(DeviceTestAuthenticationClientImpl.testUserId)).andStubReturn(testSubject);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(true);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.authenticate(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        assertTrue(response.getAssertion().isEmpty());
        assertNotNull(response.getWSAuthenticationStep());
        assertEquals(1, response.getWSAuthenticationStep().size());
        assertEquals(AuthenticationStep.GLOBAL_USAGE_AGREEMENT.getValue(), response.getWSAuthenticationStep().get(0)
                                                                                   .getAuthenticationStep());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);

        /*
         * Request Global Usage Agreement
         */
        String testGlobalUsageAgreementText = "Test Global Usage Agreement Text";
        WSAuthenticationGlobalUsageAgreementRequestType globalUsageAgeementRequest = AuthenticationUtil.getGlobalUsageAgreementRequest();

        // reset
        reset(this.mockObjects);

        // expectations
        expect(this.mockUsageAgreementService.getGlobalUsageAgreementText(this.testLanguage)).andStubReturn(testGlobalUsageAgreementText);
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);

        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationGlobalUsageAgreementResponseType globalUsageAgreementResponse = this.clientPort
                                                                                                       .requestGlobalUsageAgreement(globalUsageAgeementRequest);

        // verify
        verify(this.mockObjects);
        assertNotNull(globalUsageAgreementResponse);
        assertTrue(globalUsageAgreementResponse.getAssertion().isEmpty());
        assertEquals(testGlobalUsageAgreementText, globalUsageAgreementResponse.getGlobalUsageAgreement());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), globalUsageAgreementResponse.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(globalUsageAgreementResponse);

        /*
         * Confirm Global Usage Agreement
         */
        WSAuthenticationGlobalUsageAgreementConfirmationType globalUsageAgreementConfirmation = AuthenticationUtil
                                                                                                                  .getGlobalUsageAgreementConfirmationRequest(Confirmation.CONFIRM);

        // reset
        reset(this.mockObjects);

        // expectations
        this.mockUsageAgreementService.confirmGlobalUsageAgreementVersion();
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(false);
        expect(this.mockSubscriptionService.isSubscribed(this.testApplicationId)).andStubReturn(true);
        expect(this.mockUsageAgreementService.requiresUsageAgreementAcceptation(this.testApplicationId, this.testLanguage)).andStubReturn(
                false);
        expect(this.mockIdentityService.isConfirmationRequired(this.testApplicationId)).andStubReturn(false);
        expect(this.mockIdentityService.hasMissingAttributes(this.testApplicationId)).andStubReturn(false);
        expect(this.mockUserIdMappingService.getApplicationUserId(this.testApplicationId, DeviceTestAuthenticationClientImpl.testUserId))
                                                                                                                                         .andStubReturn(
                                                                                                                                                 DeviceTestAuthenticationClientImpl.testUserId);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        response = this.clientPort.confirmGlobalUsageAgreement(globalUsageAgreementConfirmation);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        verifySuccessFullAuthenticationResponse(response, DeviceTestAuthenticationClientImpl.testDeviceName, testSubject.getUserId(),
                this.testpublicKey);

        outputAuthenticationResponse(globalUsageAgreementResponse);
    }

    @Test
    public void testRequiresAllAdditionalSteps()
            throws Exception {

        /*
         * Authenticate
         */
        NodeEntity localNode = new NodeEntity();
        DeviceClassEntity testDeviceClass = new DeviceClassEntity("test-device-class", "test-device-auth-context-class");
        DeviceEntity testDevice = new DeviceEntity(DeviceTestAuthenticationClientImpl.testDeviceName, testDeviceClass, localNode, null,
                null, null, null, null, null, null, null);
        SubjectEntity testSubject = new SubjectEntity(DeviceTestAuthenticationClientImpl.testUserId);

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put("foo", "bar");

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(this.testApplicationId,
                DeviceTestAuthenticationClientImpl.testDeviceName, this.testLanguage, nameValuePairs, this.testpublicKey);

        // expectations
        expect(this.mockDevicePolicyService.getAuthenticationWSURL(DeviceTestAuthenticationClientImpl.testDeviceName)).andStubReturn("foo");
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockNodeAuthenticationService.getLocalNode()).andStubReturn(localNode);
        expect(this.mockDevicePolicyService.getDevice(DeviceTestAuthenticationClientImpl.testDeviceName)).andStubReturn(testDevice);
        expect(this.mockSubjectService.getSubject(DeviceTestAuthenticationClientImpl.testUserId)).andStubReturn(testSubject);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(true);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.authenticate(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        assertTrue(response.getAssertion().isEmpty());
        assertNotNull(response.getWSAuthenticationStep());
        assertEquals(1, response.getWSAuthenticationStep().size());
        assertEquals(AuthenticationStep.GLOBAL_USAGE_AGREEMENT.getValue(), response.getWSAuthenticationStep().get(0)
                                                                                   .getAuthenticationStep());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);

        /*
         * Request Global Usage Agreement
         */
        String testGlobalUsageAgreementText = "Test Global Usage Agreement Text";
        WSAuthenticationGlobalUsageAgreementRequestType globalUsageAgeementRequest = AuthenticationUtil.getGlobalUsageAgreementRequest();

        // reset
        reset(this.mockObjects);

        // expectations
        expect(this.mockUsageAgreementService.getGlobalUsageAgreementText(this.testLanguage)).andStubReturn(testGlobalUsageAgreementText);
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);

        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationGlobalUsageAgreementResponseType globalUsageAgreementResponse = this.clientPort
                                                                                                       .requestGlobalUsageAgreement(globalUsageAgeementRequest);

        // verify
        verify(this.mockObjects);
        assertNotNull(globalUsageAgreementResponse);
        assertTrue(globalUsageAgreementResponse.getAssertion().isEmpty());
        assertEquals(testGlobalUsageAgreementText, globalUsageAgreementResponse.getGlobalUsageAgreement());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), globalUsageAgreementResponse.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(globalUsageAgreementResponse);

        /*
         * Confirm Global Usage Agreement
         */
        WSAuthenticationGlobalUsageAgreementConfirmationType globalUsageAgreementConfirmation = AuthenticationUtil
                                                                                                                  .getGlobalUsageAgreementConfirmationRequest(Confirmation.CONFIRM);

        // reset
        reset(this.mockObjects);

        // expectations
        this.mockUsageAgreementService.confirmGlobalUsageAgreementVersion();
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(false);
        expect(this.mockSubscriptionService.isSubscribed(this.testApplicationId)).andStubReturn(false);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        response = this.clientPort.confirmGlobalUsageAgreement(globalUsageAgreementConfirmation);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        assertTrue(response.getAssertion().isEmpty());
        assertNotNull(response.getWSAuthenticationStep());
        assertEquals(1, response.getWSAuthenticationStep().size());
        assertEquals(AuthenticationStep.USAGE_AGREEMENT.getValue(), response.getWSAuthenticationStep().get(0).getAuthenticationStep());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(globalUsageAgreementResponse);

        /*
         * Request Usage Agreement, in this case subscription, empty usage agreement
         */
        WSAuthenticationUsageAgreementRequestType usageAgeementRequest = AuthenticationUtil.getUsageAgreementRequest();

        // reset
        reset(this.mockObjects);

        // expectations
        expect(this.mockUsageAgreementService.getUsageAgreementText(this.testApplicationId, this.testLanguage)).andStubReturn(null);
        expect(this.mockSubscriptionService.isSubscribed(this.testApplicationId)).andStubReturn(false);
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);

        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationUsageAgreementResponseType usageAgreementResponse = this.clientPort.requestUsageAgreement(usageAgeementRequest);

        // verify
        verify(this.mockObjects);
        assertNotNull(usageAgreementResponse);
        assertTrue(usageAgreementResponse.getAssertion().isEmpty());
        assertEquals("", usageAgreementResponse.getUsageAgreement());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), usageAgreementResponse.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(usageAgreementResponse);

        /*
         * Confirm Usage Agreement / subscribe in this case
         */
        WSAuthenticationUsageAgreementConfirmationType usageAgreementConfirmation = AuthenticationUtil
                                                                                                      .getUsageAgreementConfirmationRequest(Confirmation.CONFIRM);

        // reset
        reset(this.mockObjects);

        // expectations
        expect(this.mockSubscriptionService.isSubscribed(this.testApplicationId)).andReturn(false).andReturn(true);
        this.mockSubscriptionService.subscribe(this.testApplicationId);
        expect(this.mockUsageAgreementService.requiresUsageAgreementAcceptation(this.testApplicationId, this.testLanguage))
                                                                                                                           .andReturn(false)
                                                                                                                           .times(2);
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(false);
        expect(this.mockIdentityService.isConfirmationRequired(this.testApplicationId)).andStubReturn(true);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        response = this.clientPort.confirmUsageAgreement(usageAgreementConfirmation);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        assertTrue(response.getAssertion().isEmpty());
        assertNotNull(response.getWSAuthenticationStep());
        assertEquals(1, response.getWSAuthenticationStep().size());
        assertEquals(AuthenticationStep.IDENTITY_CONFIRMATION.getValue(), response.getWSAuthenticationStep().get(0).getAuthenticationStep());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(usageAgreementResponse);

        /*
         * Request Identity to be confirmed
         */
        List<AttributeDO> confirmationList = new LinkedList<AttributeDO>();
        AttributeDO testSingleStringAttribute = new AttributeDO(this.testSingleStringAttributeName, DatatypeType.STRING, false, 0, null,
                null, true, true, null, null);
        AttributeDO testMultiStringAttribute = new AttributeDO(this.testMultiStringAttributeName, DatatypeType.STRING, true, 0, null, null,
                true, true, null, null);
        testMultiStringAttribute.setMember(true);
        AttributeDO testMultiDateAttribute = new AttributeDO(this.testMultiDateAttributeName, DatatypeType.DATE, true, 0, null, null, true,
                true, null, null);
        testMultiDateAttribute.setMember(true);
        AttributeDO testCompoundAttribute = new AttributeDO(this.testCompoundAttributeName, DatatypeType.COMPOUNDED, true, 0, null, null,
                true, true, null, null);
        testCompoundAttribute.setCompounded(true);
        confirmationList.add(testSingleStringAttribute);
        confirmationList.add(testCompoundAttribute);
        confirmationList.add(testMultiDateAttribute);
        confirmationList.add(testMultiStringAttribute);

        WSAuthenticationIdentityRequestType identityRequest = AuthenticationUtil.getIdentityRequest();

        // reset
        reset(this.mockObjects);

        // expectations
        expect(this.mockIdentityService.listIdentityAttributesToConfirm(this.testApplicationId, new Locale(this.testLanguage)))
                                                                                                                               .andStubReturn(
                                                                                                                                       confirmationList);
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);

        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType identityResponse = this.clientPort.requestIdentity(identityRequest);

        // verify
        verify(this.mockObjects);
        assertNotNull(identityResponse);

        verifyAttributeStatement(identityResponse);

        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), identityResponse.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(identityResponse);

        /*
         * Confirm Application Identity
         */
        WSAuthenticationIdentityConfirmationType identityConfirmation = AuthenticationUtil
                                                                                          .getIdentityConfirmationRequest(Confirmation.CONFIRM);

        // reset
        reset(this.mockObjects);

        // expectations
        this.mockIdentityService.confirmIdentity(this.testApplicationId);
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockSubscriptionService.isSubscribed(this.testApplicationId)).andReturn(true);
        expect(this.mockUsageAgreementService.requiresUsageAgreementAcceptation(this.testApplicationId, this.testLanguage))
                                                                                                                           .andReturn(false);
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(false);
        expect(this.mockIdentityService.isConfirmationRequired(this.testApplicationId)).andStubReturn(false);
        expect(this.mockIdentityService.hasMissingAttributes(this.testApplicationId)).andStubReturn(true);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        response = this.clientPort.confirmIdentity(identityConfirmation);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        assertTrue(response.getAssertion().isEmpty());
        assertNotNull(response.getWSAuthenticationStep());
        assertEquals(1, response.getWSAuthenticationStep().size());
        assertEquals(AuthenticationStep.MISSING_ATTRIBUTES.getValue(), response.getWSAuthenticationStep().get(0).getAuthenticationStep());
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(response);

        /*
         * Request Missing Attributes
         */
        WSAuthenticationMissingAttributesRequestType missingAttributesRequest = AuthenticationUtil.getMissingAttributesRequest();

        // reset
        reset(this.mockObjects);

        // expectations
        expect(this.mockIdentityService.listMissingAttributes(this.testApplicationId, new Locale(this.testLanguage))).andReturn(
                confirmationList);
        expect(this.mockIdentityService.listOptionalAttributes(this.testApplicationId, new Locale(this.testLanguage))).andReturn(null);

        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType missingAttributesResponse = this.clientPort.requestMissingAttributes(missingAttributesRequest);

        // verify
        verify(this.mockObjects);
        assertNotNull(missingAttributesResponse);

        List<Attribute> missingAttributes = verifyAttributeStatement(missingAttributesResponse);

        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), missingAttributesResponse.getStatus().getStatusCode().getValue());

        outputAuthenticationResponse(missingAttributesResponse);

        /*
         * Save missing attributes
         */
        for (Attribute missingAttribute : missingAttributes) {
            if (missingAttribute.getName().equals(this.testSingleStringAttributeName)) {
                missingAttribute.setValue("test");
            } else {
                for (Attribute missingMemberAttribute : missingAttribute.getMembers()) {
                    if (missingMemberAttribute.getName().equals(this.testMultiDateAttributeName)) {
                        missingMemberAttribute.setValue(new Date());
                    } else {
                        missingMemberAttribute.setValue("member-test");
                    }
                }
            }
        }
        WSAuthenticationMissingAttributesSaveRequestType missingAttributesSaveRequest = AuthenticationUtil
                                                                                                          .getMissingAttributesSaveRequest(missingAttributes);

        // reset
        reset(this.mockObjects);

        // expectations
        this.mockIdentityService.saveAttribute((AttributeType) EasyMock.anyObject());
        this.mockIdentityService.saveAttribute((AttributeType) EasyMock.anyObject());
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(this.testIssuerName);
        expect(this.mockDevicePolicyService.getDevicePolicy(this.testApplicationId, null)).andStubReturn(
                Collections.singletonList(testDevice));
        expect(this.mockSubscriptionService.isSubscribed(this.testApplicationId)).andReturn(true);
        expect(this.mockUsageAgreementService.requiresUsageAgreementAcceptation(this.testApplicationId, this.testLanguage))
                                                                                                                           .andReturn(false);
        expect(this.mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(this.testLanguage)).andStubReturn(false);
        expect(this.mockIdentityService.isConfirmationRequired(this.testApplicationId)).andStubReturn(false);
        expect(this.mockIdentityService.hasMissingAttributes(this.testApplicationId)).andStubReturn(false);
        expect(this.mockUserIdMappingService.getApplicationUserId(this.testApplicationId, DeviceTestAuthenticationClientImpl.testUserId))
                                                                                                                                         .andStubReturn(
                                                                                                                                                 DeviceTestAuthenticationClientImpl.testUserId);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(this.olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(this.olasPrivateKey);

        // prepare
        replay(this.mockObjects);

        // operate
        response = this.clientPort.saveMissingAttributes(missingAttributesSaveRequest);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);
        assertEquals(WSAuthenticationErrorCode.SUCCESS.getErrorCode(), response.getStatus().getStatusCode().getValue());

        verifySuccessFullAuthenticationResponse(response, DeviceTestAuthenticationClientImpl.testDeviceName, testSubject.getUserId(),
                this.testpublicKey);

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

    private void verifySuccessFullAuthenticationResponse(WSAuthenticationResponseType response, String deviceName, String userId,
                                                         PublicKey publicKey) {

        assertEquals(deviceName, response.getDeviceName());
        assertEquals(userId, response.getUserId());

        List<AssertionType> resultAssertions = response.getAssertion();
        assertEquals(1, resultAssertions.size());
        AssertionType resultAssertion = resultAssertions.get(0);
        SubjectType resultSubject = resultAssertion.getSubject();
        List<JAXBElement<?>> resultSubjectContent = resultSubject.getContent();
        assertEquals(2, resultSubjectContent.size());
        for (JAXBElement<?> element : resultSubjectContent) {
            if (element.getValue() instanceof NameIDType) {
                NameIDType resultSubjectName = (NameIDType) element.getValue();
                assertEquals(userId, resultSubjectName.getValue());
            } else {
                SubjectConfirmationType resultSubjectConfirmation = (SubjectConfirmationType) element.getValue();
                if (null != publicKey) {
                    assertEquals(Saml2SubjectConfirmationMethod.HOLDER_OF_KEY.getMethodURI(), resultSubjectConfirmation.getMethod());
                    assertEquals(1, resultSubjectConfirmation.getSubjectConfirmationData().getContent().size());
                    assertEquals(KeyInfoType.class, ((JAXBElement<?>) resultSubjectConfirmation.getSubjectConfirmationData().getContent()
                                                                                               .get(0)).getValue().getClass());
                } else {
                    assertEquals(Saml2SubjectConfirmationMethod.SENDER_VOUCHES.getMethodURI(), resultSubjectConfirmation.getMethod());
                    assertTrue(resultSubjectConfirmation.getSubjectConfirmationData().getContent().isEmpty());
                }
            }
        }
        assertEquals(1, resultAssertion.getStatementOrAuthnStatementOrAuthzDecisionStatement().size());
        AuthnStatementType resultAuthnStatement = (AuthnStatementType) resultAssertion
                                                                                      .getStatementOrAuthnStatementOrAuthzDecisionStatement()
                                                                                      .get(0);
        assertEquals(1, resultAuthnStatement.getAuthnContext().getContent().size());
        String resultDeviceName = (String) resultAuthnStatement.getAuthnContext().getContent().get(0).getValue();
        assertEquals(deviceName, resultDeviceName);

    }

    private List<Attribute> verifyAttributeStatement(WSAuthenticationResponseType response) {

        List<Attribute> attributeList = new LinkedList<Attribute>();

        List<AssertionType> resultAssertions = response.getAssertion();
        assertEquals(1, resultAssertions.size());
        AssertionType resultAssertion = resultAssertions.get(0);
        assertNull(resultAssertion.getSubject());
        assertEquals(1, resultAssertion.getStatementOrAuthnStatementOrAuthzDecisionStatement().size());
        AttributeStatementType resultAttributeStatement = (AttributeStatementType) resultAssertion
                                                                                                  .getStatementOrAuthnStatementOrAuthzDecisionStatement()
                                                                                                  .get(0);
        for (Object attributeOrEncryptedAttribute : resultAttributeStatement.getAttributeOrEncryptedAttribute()) {
            AttributeType attributeType = (AttributeType) attributeOrEncryptedAttribute;
            Attribute attribute = new Attribute(attributeType);
            attributeList.add(attribute);
            if (attribute.getName().equals(this.testSingleStringAttributeName)) {
                assertFalse(attribute.isAnonymous());
                assertFalse(attribute.isOptional());
                assertEquals(DataType.STRING, attribute.getDataType());
            } else if (attribute.getName().equals(this.testCompoundAttributeName)) {
                assertFalse(attribute.isAnonymous());
                assertFalse(attribute.isOptional());
                assertEquals(DataType.COMPOUNDED, attribute.getDataType());
                assertEquals(2, attribute.getMembers().size());
                for (Attribute memberAttribute : attribute.getMembers()) {
                    if (memberAttribute.getName().equals(this.testMultiDateAttributeName)) {
                        assertFalse(memberAttribute.isAnonymous());
                        assertFalse(memberAttribute.isOptional());
                        assertEquals(DataType.DATE, memberAttribute.getDataType());
                    } else if (memberAttribute.getName().equals(this.testMultiStringAttributeName)) {
                        assertFalse(memberAttribute.isAnonymous());
                        assertFalse(memberAttribute.isOptional());
                        assertEquals(DataType.STRING, memberAttribute.getDataType());
                    } else {
                        fail();
                    }
                }
            } else {
                fail();
            }
        }

        return attributeList;

    }

}
