/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.Security;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.SingleSignOnService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.common.OlasNamingStrategy;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.device.sdk.auth.saml2.response.AuthnResponseFactory;
import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.operation.saml2.request.DeviceOperationRequest;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponse;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponseFactory;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.UsageStatisticTaskBean;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.w3c.dom.Document;


public class AuthenticationServiceBeanTest {

    private AuthenticationServiceBean        testedInstance;

    private Object[]                         mockObjects;

    private SubjectService                   mockSubjectService;
    private ApplicationDAO                   mockApplicationDAO;
    private SubscriptionDAO                  mockSubscriptionDAO;
    private HistoryDAO                       mockHistoryDAO;
    private StatisticDAO                     mockStatisticDAO;
    private StatisticDataPointDAO            mockStatisticDataPointDAO;
    private DeviceDAO                        mockDeviceDAO;
    private ApplicationAuthenticationService mockApplicationAuthenticationService;
    private NodeAuthenticationService        mockNodeAuthenticationService;
    private PkiValidator                     mockPkiValidator;
    private DevicePolicyService              mockDevicePolicyService;
    private SecurityAuditLogger              mockSecurityAuditLogger;
    private UserIdMappingService             mockUserIdMappingService;
    private IdentityService                  mockIdentityService;
    private UsageAgreementService            mockUsageAgreementService;
    private SamlAuthorityService             mockSamlAuthorityService;

    private SingleSignOnService              mockSingleSignOnService;
    private KeyService                       mockKeyService;

    private KeyPair                          nodeKeyPair;
    private X509Certificate                  nodeCertificate;

    private JndiTestUtils                    jndiTestUtils;


    @BeforeClass
    public static void oneTimeSetup()
            throws Exception {

        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Before
    public void setUp()
            throws Exception {

        testedInstance = new AuthenticationServiceBean();

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new OlasNamingStrategy());

        mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(testedInstance, mockSubjectService);

        mockApplicationDAO = createMock(ApplicationDAO.class);
        EJBTestUtils.inject(testedInstance, mockApplicationDAO);

        mockSubscriptionDAO = createMock(SubscriptionDAO.class);
        EJBTestUtils.inject(testedInstance, mockSubscriptionDAO);

        mockHistoryDAO = createMock(HistoryDAO.class);
        EJBTestUtils.inject(testedInstance, mockHistoryDAO);

        mockStatisticDAO = createMock(StatisticDAO.class);
        EJBTestUtils.inject(testedInstance, mockStatisticDAO);

        mockStatisticDataPointDAO = createMock(StatisticDataPointDAO.class);
        EJBTestUtils.inject(testedInstance, mockStatisticDataPointDAO);

        mockDeviceDAO = createMock(DeviceDAO.class);
        EJBTestUtils.inject(testedInstance, mockDeviceDAO);

        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        EJBTestUtils.inject(testedInstance, mockApplicationAuthenticationService);

        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        EJBTestUtils.inject(testedInstance, mockNodeAuthenticationService);

        mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(testedInstance, mockPkiValidator);

        mockDevicePolicyService = createMock(DevicePolicyService.class);
        EJBTestUtils.inject(testedInstance, mockDevicePolicyService);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        mockUserIdMappingService = createMock(UserIdMappingService.class);
        EJBTestUtils.inject(testedInstance, mockUserIdMappingService);

        mockIdentityService = createMock(IdentityService.class);
        EJBTestUtils.inject(testedInstance, mockIdentityService);

        mockUsageAgreementService = createMock(UsageAgreementService.class);
        EJBTestUtils.inject(testedInstance, mockUsageAgreementService);

        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        EJBTestUtils.inject(testedInstance, mockSamlAuthorityService);

        mockSingleSignOnService = createMock(SingleSignOnService.class);

        nodeKeyPair = PkiTestUtils.generateKeyPair();
        nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        mockKeyService = createMock(KeyService.class);
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockSubjectService, mockApplicationDAO, mockSubscriptionDAO, mockHistoryDAO, mockStatisticDAO,
                mockStatisticDataPointDAO, mockDeviceDAO, mockApplicationAuthenticationService, mockNodeAuthenticationService,
                mockPkiValidator, mockDevicePolicyService, mockSecurityAuditLogger, mockUserIdMappingService, mockIdentityService,
                mockUsageAgreementService, mockSamlAuthorityService, mockKeyService, mockSingleSignOnService };
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();

    }

    @Test
    public void initialize()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);
        mockSingleSignOnService.initialize(true, new LinkedList<String>(), application, null);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
    }

    @Test
    public void initializeSaml2RequestedAuthnContextSetsRequiredDevices()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        Set<String> devices = new HashSet<String>();
        devices.add(SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        List<DeviceEntity> authnDevices = new LinkedList<DeviceEntity>();
        DeviceEntity passwordDevice = new DeviceEntity("test-password-device", new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null,
                null, null, null, null);
        authnDevices.add(passwordDevice);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, devices, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);
        mockSingleSignOnService.initialize(true, new LinkedList<String>(), application, Collections.singleton(passwordDevice));
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockDevicePolicyService.listDevices(SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS)).andReturn(authnDevices);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        assertNotNull(protocolContext.getRequiredDevices());
        assertTrue(protocolContext.getRequiredDevices().contains(passwordDevice));
    }

    @Test
    public void initializeSaml2AuthenticationProtocolWrongSignatureKey()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        KeyPair foobarKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate foobarCert = PkiTestUtils.generateSelfSignedCertificate(foobarKeyPair, "CN=TestApplication");

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(foobarCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, foobarCert)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        try {
            testedInstance.initialize(null, null, null, authnRequest);
        } catch (SignatureValidationException e) {
            // expected
            return;
        }
        junit.framework.Assert.fail();
    }

    @Test
    public void intializeSaml2AuthenticationProtocolNotTrustedApplication()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.INVALID);

        // prepare
        replay(mockObjects);

        // operate
        try {
            testedInstance.initialize(null, null, null, authnRequest);
        } catch (SignatureValidationException e) {
            // expected
            return;
        }
        junit.framework.Assert.fail();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSuccessFullAuthenticationProcess()
            throws Exception {

        /*
         * Initialize
         */
        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);
        mockSingleSignOnService.initialize(true, new LinkedList<String>(), application, null);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());

        // reset
        reset(mockObjects);

        /*
         * Redirect authentication
         */
        // setup
        NodeEntity localNode = new NodeEntity();
        localNode.setName("test-local-node");

        String deviceName = "test-device-name";
        DeviceEntity device = new DeviceEntity();
        device.setName(deviceName);
        device.setLocation(localNode);

        // expectations
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        // prepare
        replay(mockObjects);

        // operate
        String encodedDeviceAuthenticationRequest = testedInstance
                                                                  .redirectAuthentication("test-auth-service-url", "target-url", deviceName);
        AuthnRequest deviceAuthnRequest = getAuthnRequest(new String(Base64.decode(encodedDeviceAuthenticationRequest)));

        // verify
        verify(mockObjects);

        assertNotNull(encodedDeviceAuthenticationRequest);

        // reset
        reset(mockObjects);

        /*
         * Authenticate successful
         */
        // setup
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);
        DateTime authenticationTime = new DateTime();
        Map<DateTime, String> authentications = new HashMap<DateTime, String>();
        authentications.put(authenticationTime, deviceName);

        String encodedResponse = AuthnResponseFactory.createAuthResponse(deviceAuthnRequest.getID(), applicationName, localNode.getName(),
                userId, nodeKeyPair, Integer.MAX_VALUE, "test-target", authentications);
        Response response = getResponse(encodedResponse);

        // expectations
        expect(mockNodeAuthenticationService.getCertificates(localNode.getName())).andReturn(Collections.singletonList(nodeCertificate));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN, nodeCertificate)).andReturn(
                PkiResult.VALID);
        expect(mockDeviceDAO.getDevice(deviceName)).andReturn(device);
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockSubjectService.getSubject(userId)).andReturn(subject);
        mockSingleSignOnService.setCookies((SubjectEntity) EasyMock.anyObject(), (DeviceEntity) EasyMock.anyObject(),
                (DateTime) EasyMock.anyObject());

        // prepare
        replay(mockObjects);

        // operate
        AuthenticationAssertion assertion = testedInstance.authenticate(response);

        // verify
        verify(mockObjects);

        assertEquals(AuthenticationState.USER_AUTHENTICATED, testedInstance.getAuthenticationState());
        assertNotNull(assertion);
        assertEquals(subject, assertion.getSubject());
        assertEquals(1, assertion.getAuthentications().size());
        assertTrue(assertion.getAuthentications().values().contains(device));

        // reset
        reset(mockObjects);

        /*
         * Commit authentication
         */
        // setup
        String language = Locale.ENGLISH.getLanguage();
        SubscriptionEntity subscription = new SubscriptionEntity();
        StatisticEntity statisticEntity = new StatisticEntity();
        StatisticDataPointEntity statisticDataPointEntity = new StatisticDataPointEntity();
        statisticDataPointEntity.setX(1L);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationId)).andReturn(application);
        expect(mockIdentityService.isConfirmationRequired(applicationId)).andReturn(false);
        expect(mockIdentityService.hasMissingAttributes(applicationId)).andReturn(false);
        expect(mockDevicePolicyService.getDevicePolicy(applicationId, null)).andReturn(Collections.singletonList(device));
        expect(mockUsageAgreementService.requiresGlobalUsageAgreementAcceptation(language)).andReturn(false);
        expect(mockUsageAgreementService.requiresUsageAgreementAcceptation(applicationId, language)).andReturn(false);
        expect(mockSubscriptionDAO.findSubscription(subject, application)).andReturn(subscription);
        expect(
                mockHistoryDAO.addHistoryEntry((Date) EasyMock.anyObject(), (SubjectEntity) EasyMock.anyObject(),
                        (HistoryEventType) EasyMock.anyObject(), (Map<String, String>) EasyMock.anyObject())).andReturn(null);
        mockSubscriptionDAO.loggedIn(subscription);
        expect(
                mockStatisticDAO.findOrAddStatisticByNameDomainAndApplication(UsageStatisticTaskBean.statisticName,
                        UsageStatisticTaskBean.statisticDomain, application)).andReturn(statisticEntity);
        expect(mockStatisticDataPointDAO.findOrAddStatisticDataPoint(UsageStatisticTaskBean.loginCounter, statisticEntity)).andReturn(
                statisticDataPointEntity);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.commitAuthentication(language);

        // verify
        verify(mockObjects);

        // reset
        reset(mockObjects);

        /*
         * Finalize authentication
         */
        // setup
        // expectations
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andReturn(Integer.MAX_VALUE);
        expect(mockUserIdMappingService.getApplicationUserId(applicationId, userId)).andReturn(UUID.randomUUID().toString());
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        // prepare
        replay(mockObjects);

        // operate
        String encodedAuthnResponse = testedInstance.finalizeAuthentication();

        // verify
        verify(mockObjects);
        assertNotNull(encodedAuthnResponse);
    }

    @Test
    public void testAuthenticationProcessFailedTryAnotherDevice()
            throws Exception {

        /*
         * Initialize
         */
        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);
        mockSingleSignOnService.initialize(true, new LinkedList<String>(), application, null);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());

        // reset
        reset(mockObjects);

        /*
         * Redirect authentication
         */
        // setup
        NodeEntity localNode = new NodeEntity();
        localNode.setName("test-local-node");

        String deviceName = "test-device-name";
        DeviceEntity device = new DeviceEntity();
        device.setName(deviceName);
        device.setLocation(localNode);

        // expectations
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        // prepare
        replay(mockObjects);

        // operate
        String encodedDeviceAuthenticationRequest = testedInstance
                                                                  .redirectAuthentication("test-auth-service-url", "target-url", deviceName);
        AuthnRequest deviceAuthnRequest = getAuthnRequest(new String(Base64.decode(encodedDeviceAuthenticationRequest)));

        // verify
        verify(mockObjects);

        assertNotNull(encodedDeviceAuthenticationRequest);

        // reset
        reset(mockObjects);

        /*
         * Authenticate failed, try another device
         */
        // setup
        String encodedResponse = AuthnResponseFactory.createAuthResponseRequestRegistration(deviceAuthnRequest.getID(),
                localNode.getName(), nodeKeyPair, "test-target");
        Response response = getResponse(encodedResponse);

        // expectations
        expect(mockNodeAuthenticationService.getCertificates(localNode.getName())).andReturn(Collections.singletonList(nodeCertificate));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN, nodeCertificate)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        AuthenticationAssertion assertion = testedInstance.authenticate(response);

        // verify
        verify(mockObjects);

        assertEquals(AuthenticationState.REDIRECTED, testedInstance.getAuthenticationState());
        assertNull(assertion);
    }

    @Test
    public void testAuthenticationProcessFailed()
            throws Exception {

        /*
         * Initialize
         */
        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);
        mockSingleSignOnService.initialize(true, new LinkedList<String>(), application, null);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());

        // reset
        reset(mockObjects);

        /*
         * Redirect authentication
         */
        // setup
        NodeEntity localNode = new NodeEntity();
        localNode.setName("test-local-node");

        String deviceName = "test-device-name";
        DeviceEntity device = new DeviceEntity();
        device.setName(deviceName);
        device.setLocation(localNode);

        // expectations
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        // prepare
        replay(mockObjects);

        // operate
        String encodedDeviceAuthenticationRequest = testedInstance
                                                                  .redirectAuthentication("test-auth-service-url", "target-url", deviceName);
        AuthnRequest deviceAuthnRequest = getAuthnRequest(new String(Base64.decode(encodedDeviceAuthenticationRequest)));

        // verify
        verify(mockObjects);

        assertNotNull(encodedDeviceAuthenticationRequest);

        // reset
        reset(mockObjects);

        /*
         * Authenticate failed, try another device
         */
        // setup
        String encodedResponse = AuthnResponseFactory.createAuthResponseFailed(deviceAuthnRequest.getID(), localNode.getName(),
                nodeKeyPair, "test-target");
        Response response = getResponse(encodedResponse);

        // expectations
        expect(mockNodeAuthenticationService.getCertificates(localNode.getName())).andReturn(Collections.singletonList(nodeCertificate));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN, nodeCertificate)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        AuthenticationAssertion assertion = testedInstance.authenticate(response);

        // verify
        verify(mockObjects);

        assertEquals(AuthenticationState.INITIALIZED, testedInstance.getAuthenticationState());
        assertNull(assertion);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRegistration()
            throws Exception {

        /*
         * Initialize
         */
        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        SubjectEntity ownerSubject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("owner", ownerSubject);
        ApplicationEntity application = new ApplicationEntity(applicationName, null, owner, null, null, null, null);
        application.setId(applicationId);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);
        mockSingleSignOnService.initialize(true, new LinkedList<String>(), application, null);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());

        // reset
        reset(mockObjects);

        /*
         * Redirect registration
         */
        // setup
        String userId = UUID.randomUUID().toString();

        NodeEntity localNode = new NodeEntity();
        localNode.setName("test-local-node");

        String deviceName = "test-device-name";
        DeviceEntity device = new DeviceEntity();
        device.setName(deviceName);
        device.setLocation(localNode);

        // expectations
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockDeviceDAO.getDevice(deviceName)).andReturn(device);
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        // prepare
        replay(mockObjects);

        // operate
        String encodedDeviceRegistrationRequest = testedInstance.redirectRegistration("test-reg-service-url", "target-url", deviceName,
                userId);
        DeviceOperationRequest deviceRegistrationRequest = getDeviceRequest(new String(Base64.decode(encodedDeviceRegistrationRequest)));

        // verify
        verify(mockObjects);

        assertNotNull(encodedDeviceRegistrationRequest);

        // reset
        reset(mockObjects);

        /*
         * Registration successful
         */
        // setup
        SubjectEntity subject = new SubjectEntity(userId);
        DateTime authenticationTime = new DateTime();
        Map<DateTime, String> authentications = new HashMap<DateTime, String>();
        authentications.put(authenticationTime, deviceName);

        String encodedResponse = DeviceOperationResponseFactory.createDeviceOperationResponse(deviceRegistrationRequest.getID(),
                DeviceOperationType.NEW_ACCOUNT_REGISTER, localNode.getName(), userId, device.getName(), nodeKeyPair, Integer.MAX_VALUE,
                "test-target");
        DeviceOperationResponse response = getDeviceResponse(encodedResponse);

        // expectations
        expect(mockNodeAuthenticationService.getCertificates(localNode.getName())).andReturn(Collections.singletonList(nodeCertificate));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN, nodeCertificate)).andReturn(
                PkiResult.VALID);
        expect(mockDeviceDAO.getDevice(deviceName)).andReturn(device);
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockSubjectService.getSubject(userId)).andReturn(subject);
        mockSingleSignOnService.setCookies((SubjectEntity) EasyMock.anyObject(), (DeviceEntity) EasyMock.anyObject(),
                (DateTime) EasyMock.anyObject());
        expect(
                mockHistoryDAO.addHistoryEntry((Date) EasyMock.anyObject(), (SubjectEntity) EasyMock.anyObject(),
                        (HistoryEventType) EasyMock.anyObject(), (Map<String, String>) EasyMock.anyObject())).andReturn(null);

        // prepare
        replay(mockObjects);

        // operate
        AuthenticationAssertion assertion = testedInstance.register(response);

        // verify
        verify(mockObjects);

        assertEquals(AuthenticationState.USER_AUTHENTICATED, testedInstance.getAuthenticationState());
        assertNotNull(assertion);
        assertEquals(subject, assertion.getSubject());
        assertEquals(1, assertion.getAuthentications().size());
        assertTrue(assertion.getAuthentications().values().contains(device));

        // reset
        reset(mockObjects);

    }

    private AuthnRequest getAuthnRequest(String encodedAuthnRequest)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedAuthnRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        AuthnRequest authnRequest = (AuthnRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return authnRequest;
    }

    private Response getResponse(String encodedResponse)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedResponse);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        Response response = (Response) unmarshaller.unmarshall(doc.getDocumentElement());
        return response;
    }

    private DeviceOperationRequest getDeviceRequest(String encodedRequest)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        DeviceOperationRequest request = (DeviceOperationRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return request;
    }

    private DeviceOperationResponse getDeviceResponse(String encodedResponse)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedResponse);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        DeviceOperationResponse response = (DeviceOperationResponse) unmarshaller.unmarshall(doc.getDocumentElement());
        return response;
    }
}
