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
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.DomUtils;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.junit.After;
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

    private PasswordDeviceService            mockPasswordDeviceService;

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

    private static SecretKey                 ssoKey;


    @BeforeClass
    public static void oneTimeSetup() throws Exception {

        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.tearDown();
        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", bcp);
        keyGen.init(128, new SecureRandom());
        AuthenticationServiceBeanTest.ssoKey = keyGen.generateKey();

        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getSsoKey",
                new MBeanActionHandler() {

                    @SuppressWarnings("synthetic-access")
                    public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                        return ssoKey;
                    }
                });
    }

    @Before
    public void setUp() throws Exception {

        this.testedInstance = new AuthenticationServiceBean();

        this.mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(this.testedInstance, this.mockSubjectService);

        this.mockPasswordDeviceService = createMock(PasswordDeviceService.class);
        EJBTestUtils.inject(this.testedInstance, this.mockPasswordDeviceService);

        this.mockApplicationDAO = createMock(ApplicationDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockApplicationDAO);

        this.mockSubscriptionDAO = createMock(SubscriptionDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockSubscriptionDAO);

        this.mockHistoryDAO = createMock(HistoryDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockHistoryDAO);

        this.mockStatisticDAO = createMock(StatisticDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockStatisticDAO);

        this.mockStatisticDataPointDAO = createMock(StatisticDataPointDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockStatisticDataPointDAO);

        this.mockDeviceDAO = createMock(DeviceDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockDeviceDAO);

        this.mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        EJBTestUtils.inject(this.testedInstance, this.mockApplicationAuthenticationService);

        this.mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(this.testedInstance, this.mockPkiValidator);

        this.mockDevicePolicyService = createMock(DevicePolicyService.class);
        EJBTestUtils.inject(this.testedInstance, this.mockDevicePolicyService);

        this.mockApplicationPoolDAO = createMock(ApplicationPoolDAO.class);
        EJBTestUtils.inject(this.testedInstance, this.mockApplicationPoolDAO);

        this.mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(this.testedInstance, this.mockSecurityAuditLogger);

        this.mockUserIdMappingService = createMock(UserIdMappingService.class);
        EJBTestUtils.inject(this.testedInstance, this.mockUserIdMappingService);

        EJBTestUtils.init(this.testedInstance);

        this.mockObjects = new Object[] { this.mockSubjectService, this.mockPasswordDeviceService,
                this.mockApplicationDAO, this.mockSubscriptionDAO, this.mockHistoryDAO, this.mockStatisticDAO,
                this.mockStatisticDataPointDAO, this.mockDeviceDAO, this.mockApplicationAuthenticationService,
                this.mockPkiValidator, this.mockDevicePolicyService, this.mockApplicationPoolDAO,
                this.mockSecurityAuditLogger, this.mockUserIdMappingService };
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void authenticate() throws Exception {

        // setup
        String applicationName = "test-application";
        String login = "test-login";
        String password = "test-password";

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);

        // stubs
        SubjectEntity subject = new SubjectEntity(login);
        expect(this.mockSubjectService.getSubjectFromUserName(login)).andStubReturn(subject);
        expect(this.mockPasswordDeviceService.authenticate(login, password)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);

        DeviceClassEntity deviceClass = new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, deviceClass, null,
                null, null, null, null, null, null);
        expect(this.mockDeviceDAO.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)).andReturn(device);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initialize(null, authnRequest);

        boolean result = this.testedInstance.authenticate(login, password);

        // verify
        verify(this.mockObjects);
        assertTrue(result);
    }

    @Test
    public void initialize() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);

        // verify
        verify(this.mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
    }

    @Test
    public void initializeSaml2RequestedAuthnContextSetsRequiredDevices() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        Set<String> devices = new HashSet<String>();
        devices.add(SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, devices, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);

        List<DeviceEntity> authnDevices = new LinkedList<DeviceEntity>();
        DeviceEntity passwordDevice = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID,
                new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                        SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null,
                null);
        authnDevices.add(passwordDevice);
        expect(this.mockDevicePolicyService.listDevices(SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS))
                .andReturn(authnDevices);

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);

        // verify
        verify(this.mockObjects);

        assertEquals(applicationName, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        assertNotNull(protocolContext.getRequiredDevices());
        assertTrue(protocolContext.getRequiredDevices().contains(passwordDevice));
    }

    @Test
    public void initializeSaml2AuthenticationProtocolWrongSignatureKey() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";

        KeyPair foobarKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate foobarCert = PkiTestUtils.generateSelfSignedCertificate(foobarKeyPair, "CN=TestApplication");

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(foobarCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        foobarCert)).andReturn(PkiResult.VALID);

        // prepare
        replay(this.mockObjects);

        // operate
        try {
            this.testedInstance.initialize(null, authnRequest);
        } catch (AuthenticationInitializationException e) {
            // expected
            return;
        }
        junit.framework.Assert.fail();
    }

    @Test
    public void intializeSaml2AuthenticationProtocolNotTrustedApplication() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.INVALID);

        // prepare
        replay(this.mockObjects);

        // operate
        try {
            this.testedInstance.initialize(null, authnRequest);
        } catch (AuthenticationInitializationException e) {
            // expected
            return;
        }
        junit.framework.Assert.fail();
    }

    @Test
    public void checkSingleSignOnCookieSuccess() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(this.mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(this.mockDeviceDAO.findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)).andStubReturn(device);
        expect(this.mockDevicePolicyService.getDevicePolicy(applicationName, null)).andStubReturn(
                Collections.singletonList(device));

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);
        boolean result = this.testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(this.mockObjects);

        assertTrue(result);

        assertEquals(applicationName, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = this.testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.USER_AUTHENTICATED, resultState);
        String resultUserId = this.testedInstance.getUserId();
        assertEquals(userId, resultUserId);
        DeviceEntity resultDevice = this.testedInstance.getAuthenticationDevice();
        assertEquals(device, resultDevice);
    }

    @Test
    public void checkSingleSignOnCookieApplicationSsoDisabled() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(false);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);
        boolean result = this.testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(this.mockObjects);

        assertFalse(result);

        assertEquals(applicationName, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = this.testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieRequestSsoDisabled() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);
        boolean result = this.testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(this.mockObjects);

        assertFalse(result);
        assertEquals(applicationName, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = this.testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieInvalidCookie() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        Cookie ssoCookie = getInvalidSsoCookie(applicationName);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_COOKIE);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initialize(null, authnRequest);
        try {
            this.testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(this.mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieInvalidSubject() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String invalidUser = "foobar";
        SubjectEntity invalidSubject = new SubjectEntity(invalidUser);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(invalidSubject, application, device, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockSubjectService.findSubject(invalidUser)).andStubReturn(null);
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_USER + invalidUser);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initialize(null, authnRequest);
        try {
            this.testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(this.mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieInvalidApplication() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String invalidApplicationName = "foobar-application";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        ApplicationEntity invalidApplication = new ApplicationEntity(invalidApplicationName, null, null, null, null,
                null, null, applicationCert);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, invalidApplication, device, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(invalidApplicationName)).andStubReturn(null);
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_APPLICATION + invalidApplicationName);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initialize(null, authnRequest);
        try {
            this.testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(this.mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieNoCommonPool() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String cookieApplicationName = "cookie-test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));
        ApplicationEntity cookieApplication = new ApplicationEntity(cookieApplicationName, null, null, null, null,
                null, null, applicationCert);

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, cookieApplication, device, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(cookieApplicationName)).andStubReturn(cookieApplication);
        expect(this.mockDeviceDAO.findDevice(device.getName())).andStubReturn(device);
        expect(this.mockApplicationPoolDAO.listCommonApplicationPools(application, cookieApplication)).andStubReturn(
                new LinkedList<ApplicationPoolEntity>());

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);
        boolean result = this.testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(this.mockObjects);

        assertFalse(result);

        assertEquals(applicationName, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = this.testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    /**
     * Following situation is tested: 3 applications, 2 application pools, 1st and 3rd application have resp. 1st and
     * 3rd application pool. 2nd application has both application pools.
     * 
     * 1st application logs in, 2nd application used sso due to 1st.
     * 
     * Test that we cannot sso into the second application pool if we want to login for application 3.
     */
    @Test
    public void checkSingleSignOnCookie2CommonApplicationPools() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String application1Name = "test-application-id-1";
        String application2Name = "test-application-id-2";
        String application3Name = "test-application-id-3";
        String applicationPool1Name = "test-application-pool-1";
        String applicationPool2Name = "test-applicaiton-pool-2";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";

        ApplicationEntity application1 = new ApplicationEntity(application1Name, null, null, null, null, null, null,
                applicationCert);
        application1.setSsoEnabled(true);
        ApplicationEntity application2 = new ApplicationEntity(application2Name, null, null, null, null, null, null,
                applicationCert);
        application2.setSsoEnabled(true);
        ApplicationEntity application3 = new ApplicationEntity(application3Name, null, null, null, null, null, null,
                applicationCert);
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

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(application3Name, application3Name, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application1, device, Collections.singletonList(application2));

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(application3Name)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(application3Name)).andStubReturn(application3);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(application1Name)).andStubReturn(application1);
        expect(this.mockApplicationDAO.findApplication(application2Name)).andStubReturn(application2);
        expect(this.mockDeviceDAO.findDevice(device.getName())).andStubReturn(device);
        expect(this.mockApplicationPoolDAO.listCommonApplicationPools(application3, application1)).andStubReturn(
                new LinkedList<ApplicationPoolEntity>());

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);
        boolean result = this.testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(this.mockObjects);

        assertFalse(result);

        assertEquals(application3Name, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = this.testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieInvalidDevice() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        String invalidDeviceName = "foobar-device";
        DeviceEntity invalidDevice = new DeviceEntity(invalidDeviceName, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);

        Cookie ssoCookie = getSsoCookie(subject, application, invalidDevice, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(this.mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(this.mockDeviceDAO.findDevice(invalidDeviceName)).andStubReturn(null);
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                AuthenticationServiceBean.SECURITY_MESSAGE_INVALID_DEVICE + invalidDeviceName);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initialize(null, authnRequest);
        try {
            this.testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(this.mockObjects);
            return;
        }
        fail();
    }

    @Test
    public void checkSingleSignOnCookieInvalidDevicePolicy() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getSsoCookie(subject, application, device, null);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(this.mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(this.mockDeviceDAO.findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)).andStubReturn(device);
        expect(this.mockDevicePolicyService.getDevicePolicy(applicationName, null)).andStubReturn(
                new LinkedList<DeviceEntity>());

        // prepare
        replay(this.mockObjects);

        // operate
        ProtocolContext protocolContext = this.testedInstance.initialize(null, authnRequest);
        boolean result = this.testedInstance.checkSsoCookie(ssoCookie);

        // verify
        verify(this.mockObjects);

        assertFalse(result);

        assertEquals(applicationName, protocolContext.getApplicationId());
        assertEquals(assertionConsumerService, protocolContext.getTarget());
        AuthenticationState resultState = this.testedInstance.getAuthenticationState();
        assertEquals(AuthenticationState.INITIALIZED, resultState);
    }

    @Test
    public void checkSingleSignOnCookieExpired() throws Exception {

        // setup
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        String applicationName = "test-application-id";
        String applicationPoolName = "test-application-pool";
        String assertionConsumerService = "http://test.assertion.consumer.service";
        String destinationUrl = "http://test.destination.url";
        ApplicationEntity application = new ApplicationEntity(applicationName, null, null, null, null, null, null,
                applicationCert);
        application.setSsoEnabled(true);
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, 1000 * 60 * 5);
        applicationPool.setApplications(Collections.singletonList(application));
        application.setApplicationPools(Collections.singletonList(applicationPool));

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, applicationName, null,
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null, true);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, new DeviceClassEntity(
                SafeOnlineConstants.PASSWORD_DEVICE_CLASS, SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
                null, null, null, null, null, null, null);
        Cookie ssoCookie = getExpiredSsoCookie(subject, application, device);

        // expectations
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockSubjectService.findSubject(userId)).andStubReturn(subject);
        expect(this.mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);
        expect(this.mockApplicationPoolDAO.listCommonApplicationPools(application, application)).andStubReturn(
                Collections.singletonList(applicationPool));
        expect(this.mockDeviceDAO.findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)).andStubReturn(device);
        expect(this.mockDevicePolicyService.getDevicePolicy(applicationName, null)).andStubReturn(
                Collections.singletonList(device));

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initialize(null, authnRequest);
        try {
            this.testedInstance.checkSsoCookie(ssoCookie);
        } catch (InvalidCookieException e) {
            // expected
            verify(this.mockObjects);
            return;
        }
        fail();
    }

    private AuthnRequest getAuthnRequest(String encodedAuthnRequest) throws Exception {

        Document doc = DomUtils.parseDocument(encodedAuthnRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        AuthnRequest authnRequest = (AuthnRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return authnRequest;
    }

    private Cookie getSsoCookie(SubjectEntity subject, ApplicationEntity application, DeviceEntity device,
            List<ApplicationEntity> ssoApplications) throws Exception {

        DateTime now = new DateTime();
        SingleSignOn sso = new SingleSignOn(subject, application, device, now);
        if (null != ssoApplications) {
            sso.ssoApplications = ssoApplications;
        }
        String value = sso.getValue();

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        Cipher encryptCipher = Cipher.getInstance("AES", bcp);
        encryptCipher.init(Cipher.ENCRYPT_MODE, ssoKey);
        byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue = new sun.misc.BASE64Encoder().encode(encryptedBytes);
        Cookie ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + application.getName(),
                encryptedValue);
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
        encryptCipher.init(Cipher.ENCRYPT_MODE, ssoKey);
        byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue = new sun.misc.BASE64Encoder().encode(encryptedBytes);
        Cookie ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + application.getName(),
                encryptedValue);
        ssoCookie.setPath("/olas-auth/");
        ssoCookie.setMaxAge(-1);
        return ssoCookie;
    }

    private Cookie getInvalidSsoCookie(String applicationId) throws Exception {

        String value = "foobar";

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        Cipher encryptCipher = Cipher.getInstance("AES", bcp);
        encryptCipher.init(Cipher.ENCRYPT_MODE, ssoKey);
        byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue = new sun.misc.BASE64Encoder().encode(encryptedBytes);
        Cookie ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + applicationId,
                encryptedValue);
        ssoCookie.setPath("/olas-auth/");
        ssoCookie.setMaxAge(-1);
        return ssoCookie;
    }

    @Test
    public void initializeLogout() throws Exception {

        // setup
        String applicationName = "test-application-id";
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair,
                "CN=TestApplication");
        ApplicationEntity application = new ApplicationEntity(applicationName, null, new ApplicationOwnerEntity(),
                null, null, null, null, applicationCert);
        application.setSsoLogoutUrl(new URL("http", "test.host", "logout"));

        String applicationUserId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        String destinationUrl = "http://test.destination.url";

        String encodedLogoutRequest = LogoutRequestFactory.createLogoutRequest(applicationUserId, applicationName,
                applicationKeyPair, destinationUrl, null);
        LogoutRequest logoutRequest = getLogoutRequest(encodedLogoutRequest);

        // expectations
        expect(this.mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(this.mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(
                Collections.singletonList(applicationCert));
        expect(
                this.mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                        applicationCert)).andReturn(PkiResult.VALID);
        expect(this.mockUserIdMappingService.findUserId(applicationName, applicationUserId)).andStubReturn(userId);
        expect(this.mockSubjectService.getSubject(userId)).andStubReturn(subject);

        // prepare
        replay(this.mockObjects);

        // operate
        LogoutProtocolContext logoutProtocolContext = this.testedInstance.initialize(logoutRequest);

        // verify
        verify(this.mockObjects);

        assertEquals(applicationName, logoutProtocolContext.getApplicationId());
        assertEquals(application.getSsoLogoutUrl().toString(), logoutProtocolContext.getTarget());
    }

    private LogoutRequest getLogoutRequest(String encodedLogoutRequest) throws Exception {

        Document doc = DomUtils.parseDocument(encodedLogoutRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        LogoutRequest logoutRequest = (LogoutRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return logoutRequest;
    }
}
