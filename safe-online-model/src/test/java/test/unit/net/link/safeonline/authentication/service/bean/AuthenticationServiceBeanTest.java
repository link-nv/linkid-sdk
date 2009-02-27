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
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.AuthenticationInitializationException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean.SingleSignOn;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationPoolDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.w3c.dom.Document;


public class AuthenticationServiceBeanTest {

    private AuthenticationServiceBean        testedInstance;

    private SubjectService                   mockSubjectService;

    private ApplicationDAO                   mockApplicationDAO;

    private SubscriptionDAO                  mockSubscriptionDAO;

    private HistoryDAO                       mockHistoryDAO;

    private Object[]                         mockObjects;

    private StatisticDAO                     mockStatisticDAO;

    private StatisticDataPointDAO            mockStatisticDataPointDAO;

    private DeviceDAO                        mockDeviceDAO;

    private ApplicationAuthenticationService mockApplicationAuthenticationService;

    private PkiValidator                     mockPkiValidator;

    private DevicePolicyService              mockDevicePolicyService;

    private ApplicationPoolDAO               mockApplicationPoolDAO;

    private SecurityAuditLogger              mockSecurityAuditLogger;

    private UserIdMappingService             mockUserIdMappingService;


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

        mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(testedInstance, mockPkiValidator);

        mockDevicePolicyService = createMock(DevicePolicyService.class);
        EJBTestUtils.inject(testedInstance, mockDevicePolicyService);

        mockApplicationPoolDAO = createMock(ApplicationPoolDAO.class);
        EJBTestUtils.inject(testedInstance, mockApplicationPoolDAO);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        mockUserIdMappingService = createMock(UserIdMappingService.class);
        EJBTestUtils.inject(testedInstance, mockUserIdMappingService);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockSubjectService, mockApplicationDAO, mockSubscriptionDAO, mockHistoryDAO, mockStatisticDAO,
                mockStatisticDataPointDAO, mockDeviceDAO, mockApplicationAuthenticationService, mockPkiValidator, mockDevicePolicyService,
                mockApplicationPoolDAO, mockSecurityAuditLogger, mockUserIdMappingService };
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

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
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

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, devices, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);

        List<DeviceEntity> authnDevices = new LinkedList<DeviceEntity>();
        DeviceEntity passwordDevice = new DeviceEntity("test-password-device", new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null,
                null, null, null, null);
        authnDevices.add(passwordDevice);
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

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
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
        } catch (AuthenticationInitializationException e) {
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

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
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
        } catch (AuthenticationInitializationException e) {
            // expected
            return;
        }
        junit.framework.Assert.fail();
    }

    @Test
    public void checkSingleSignOnCookieSuccess()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(mockDeviceDAO.findDevice(passwordDeviceId)).andStubReturn(device);
        expect(mockDevicePolicyService.getDevicePolicy(applicationId, null)).andStubReturn(Collections.singletonList(device));

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);
        boolean result = testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(mockObjects);

        assertTrue(result);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.USER_AUTHENTICATED, resultState);
        String resultUserId = testedInstance.getUserId();
        assertEquals(userId, resultUserId);
        DeviceEntity resultDevice = testedInstance.getAuthenticationDevice();
        assertEquals(device, resultDevice);
    }

    @Test
    public void checkSingleSignOnCookieApplicationSsoDisabled()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(false);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);
        boolean result = testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(mockObjects);

        assertFalse(result);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieRequestSsoDisabled()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);
        boolean result = testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(mockObjects);

        assertFalse(result);
        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieInvalidCookie()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        Cookie ssoCookie = getInvalidSsoCookie(applicationName);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_COOKIE);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.initialize(null, null, null, authnRequest);
        try {
            testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieInvalidSubject()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String invalidUser = "foobar";
        SubjectEntity invalidSubject = new SubjectEntity(invalidUser);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(invalidSubject, application, device, null);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockSubjectService.findSubject(invalidUser)).andStubReturn(null);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_USER
                + invalidUser);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.initialize(null, null, null, authnRequest);
        try {
            testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieInvalidApplication()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String invalidApplicationName = "foobar-application";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        ApplicationEntity invalidApplication = new ApplicationEntity(invalidApplicationName, null, null, null, null, null, applicationCert);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, invalidApplication, device, null);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(mockApplicationDAO.findApplication(invalidApplicationName)).andStubReturn(null);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_APPLICATION + invalidApplicationName);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.initialize(null, null, null, authnRequest);
        try {
            testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieNoCommonPool()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String cookieApplicationName = "cookie-test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));
        ApplicationEntity cookieApplication = new ApplicationEntity(cookieApplicationName, null, null, null, null, null, applicationCert);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, cookieApplication, device, null);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(mockApplicationDAO.findApplication(cookieApplicationName)).andStubReturn(cookieApplication);
        expect(mockDeviceDAO.findDevice(device.getName())).andStubReturn(device);
        expect(mockApplicationPoolDAO.listCommonApplicationPools(application, cookieApplication)).andStubReturn(
                new LinkedList<ApplicationPoolEntity>());

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);
        boolean result = testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(mockObjects);

        assertFalse(result);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    /**
     * Following situation is tested: 3 applications, 2 application pools, 1st and 3rd application have resp. 1st and 3rd application pool.
     * 2nd application has both application pools.
     * 
     * 1st application logs in, 2nd application used sso due to 1st.
     * 
     * Test that we cannot sso into the second application pool if we want to login for application 3.
     */
    @Test
    public void checkSingleSignOnCookie2CommonApplicationPools()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String application1Name = "test-application-id-1";
        long application1Id = 1;
        String application2Name = "test-application-id-2";
        long application2Id = 2;
        String application3Name = "test-application-id-3";
        long application3Id = 3;
        String applicationPool1Name = "test-application-pool-1";
        String applicationPool2Name = "test-applicaiton-pool-2";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";

        ApplicationEntity application1 = new ApplicationEntity(application1Name, null, null, null, null, null, applicationCert);
        application1.setId(application1Id);
        application1.setSsoEnabled(true);
        ApplicationEntity application2 = new ApplicationEntity(application2Name, null, null, null, null, null, applicationCert);
        application2.setId(application2Id);
        application2.setSsoEnabled(true);
        ApplicationEntity application3 = new ApplicationEntity(application3Name, null, null, null, null, null, applicationCert);
        application3.setId(application3Id);
        application3.setSsoEnabled(true);

        List<ApplicationEntity> applicationPool1List = new LinkedList<ApplicationEntity>();
        applicationPool1List.add(application1);
        applicationPool1List.add(application2);
        List<ApplicationEntity> applicationPool2List = new LinkedList<ApplicationEntity>();
        applicationPool2List.add(application2);
        applicationPool2List.add(application3);

        ApplicationPoolEntity applicationPool1 = new ApplicationPoolEntity(applicationPool1Name, 1000 * 60 * 5);
        applicationPool1.setApplications(applicationPool1List);
        ApplicationPoolEntity applicationPool2 = new ApplicationPoolEntity(applicationPool2Name, 1000 * 60 * 5);
        applicationPool2.setApplications(applicationPool2List);

        List<ApplicationPoolEntity> application2PoolList = new LinkedList<ApplicationPoolEntity>();
        application2PoolList.add(applicationPool1);
        application2PoolList.add(applicationPool2);
        application1.setApplicationPools(Collections.singletonList(applicationPool1));
        application2.setApplicationPools(application2PoolList);
        application3.setApplicationPools(Collections.singletonList(applicationPool2));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(application3Name, application3Name, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application1, device, Collections.singletonList(application2));

        // expectations
        expect(mockApplicationDAO.findApplication(application3Id)).andStubReturn(application3);
        expect(mockApplicationAuthenticationService.getCertificates(application3Id)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(application3Name)).andStubReturn(application3);
        expect(mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(mockApplicationDAO.findApplication(application1Name)).andStubReturn(application1);
        expect(mockApplicationDAO.findApplication(application2Name)).andStubReturn(application2);
        expect(mockDeviceDAO.findDevice(device.getName())).andStubReturn(device);
        expect(mockApplicationPoolDAO.listCommonApplicationPools(application3, application1)).andStubReturn(
                new LinkedList<ApplicationPoolEntity>());

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);
        boolean result = testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(mockObjects);

        assertFalse(result);

        assertEquals(application3Name, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieInvalidDevice()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        String invalidDeviceName = "foobar-device";
        DeviceEntity invalidDevice = new DeviceEntity(invalidDeviceName, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);

        Cookie ssoCookie = getSsoCookie(subject, application, invalidDevice, null);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(mockDeviceDAO.findDevice(invalidDeviceName)).andStubReturn(null);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_DEVICE
                + invalidDeviceName);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.initialize(null, null, null, authnRequest);
        try {
            testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieInvalidDevicePolicy()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(mockDeviceDAO.findDevice(passwordDeviceId)).andStubReturn(device);
        expect(mockDevicePolicyService.getDevicePolicy(applicationId, null)).andStubReturn(new LinkedList<DeviceEntity>());

        // prepare
        replay(mockObjects);

        // operate
        ProtocolContext protocolContext = testedInstance.initialize(null, null, null, authnRequest);
        boolean result = testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(mockObjects);

        assertFalse(result);

        assertEquals(applicationName, protocolContext.getApplicationName());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieExpired()
            throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        String passwordDeviceId = "test-password-device-id";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, applicationCert);
        application.setId(applicationId);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(passwordDeviceId, new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null, null, null);
        Cookie ssoCookie = getExpiredSsoCookie(subject, application, device);

        // expectations
        expect(mockApplicationDAO.findApplication(applicationId)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationId)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(mockDeviceDAO.findDevice(passwordDeviceId)).andStubReturn(device);
        expect(mockDevicePolicyService.getDevicePolicy(applicationId, null)).andStubReturn(Collections.singletonList(device));

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.initialize(null, null, null, authnRequest);
        try {
            testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(mockObjects);
            return;
        }
        fail();
    }

    private AuthnRequest getAuthnRequest(String encodedAuthnRequest)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedAuthnRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        AuthnRequest authnRequest = (AuthnRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return authnRequest;
    }

    private Cookie getSsoCookie(SubjectEntity subject, ApplicationEntity application, DeviceEntity device,
                                List<ApplicationEntity> ssoApplications)
            throws Exception {

        DateTime now = new DateTime();
        SingleSignOn sso = new SingleSignOn(subject, application, device, now);
        if (null != ssoApplications) {
            sso.ssoApplications = ssoApplications;
        }
        String value = sso.getValue();

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        Cipher encryptCipher = Cipher.getInstance("AES", bcp);
        encryptCipher.init(Cipher.ENCRYPT_MODE, SafeOnlineNodeKeyStore.getSSOKey());
        byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue = new sun.misc.BASE64Encoder().encode(encryptedBytes);
        Cookie ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + application.getName(), encryptedValue);
        ssoCookie.setPath("/olas-auth/");
        ssoCookie.setMaxAge(-1);
        return ssoCookie;
    }

    private Cookie getExpiredSsoCookie(SubjectEntity subject, ApplicationEntity application, DeviceEntity device)
            throws Exception {

        DateTime now = new DateTime();
        DateTime authTime = now.minusDays(7);
        SingleSignOn sso = new SingleSignOn(subject, application, device, authTime);
        String value = sso.getValue();

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        Cipher encryptCipher = Cipher.getInstance("AES", bcp);
        encryptCipher.init(Cipher.ENCRYPT_MODE, SafeOnlineNodeKeyStore.getSSOKey());
        byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue = new sun.misc.BASE64Encoder().encode(encryptedBytes);
        Cookie ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + application.getName(), encryptedValue);
        ssoCookie.setPath("/olas-auth/");
        ssoCookie.setMaxAge(-1);
        return ssoCookie;
    }

    private Cookie getInvalidSsoCookie(String applicationId)
            throws Exception {

        String value = "foobar";

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        Cipher encryptCipher = Cipher.getInstance("AES", bcp);
        encryptCipher.init(Cipher.ENCRYPT_MODE, SafeOnlineNodeKeyStore.getSSOKey());
        byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue = new sun.misc.BASE64Encoder().encode(encryptedBytes);
        Cookie ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + applicationId, encryptedValue);
        ssoCookie.setPath("/olas-auth/");
        ssoCookie.setMaxAge(-1);
        return ssoCookie;
    }

    @Test
    public void initializeLogout()
            throws Exception {

        // setup
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        ApplicationEntity application = new ApplicationEntity(applicationName, null, new ApplicationOwnerEntity(), null, null, null,
                applicationCert);
        application.setId(applicationId);
        application.setSsoLogoutUrl(new URL("http", "test.host", "logout"));

        String applicationUserId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        String destinationUrl = "http://test.destination.url";

        String encodedLogoutRequest = LogoutRequestFactory.createLogoutRequest(applicationUserId, applicationName, applicationKeyPair,
                destinationUrl, null);
        LogoutRequest logoutRequest = getLogoutRequest(encodedLogoutRequest);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockUserIdMappingService.findUserId(applicationId, applicationUserId)).andStubReturn(userId);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(subject);

        // prepare
        replay(mockObjects);

        // operate
        LogoutProtocolContext logoutProtocolContext = testedInstance.initialize(logoutRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, logoutProtocolContext.getApplicationId());
        assertEquals(application.getSsoLogoutUrl().toString(), logoutProtocolContext.getTarget());
    }

    private LogoutRequest getLogoutRequest(String encodedLogoutRequest)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedLogoutRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        LogoutRequest logoutRequest = (LogoutRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return logoutRequest;
    }
}
