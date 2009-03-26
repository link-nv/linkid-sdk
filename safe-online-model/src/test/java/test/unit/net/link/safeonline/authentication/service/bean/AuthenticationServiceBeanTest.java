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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.SingleSignOnService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.common.OlasNamingStrategy;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml2.core.AuthnRequest;
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

    private SecurityAuditLogger              mockSecurityAuditLogger;

    private UserIdMappingService             mockUserIdMappingService;

    private SingleSignOnService              mockSingleSignOnService;

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

        mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(testedInstance, mockPkiValidator);

        mockDevicePolicyService = createMock(DevicePolicyService.class);
        EJBTestUtils.inject(testedInstance, mockDevicePolicyService);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        mockUserIdMappingService = createMock(UserIdMappingService.class);
        EJBTestUtils.inject(testedInstance, mockUserIdMappingService);

        mockSingleSignOnService = createMock(SingleSignOnService.class);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockSubjectService, mockApplicationDAO, mockSubscriptionDAO, mockHistoryDAO, mockStatisticDAO,
                mockStatisticDataPointDAO, mockDeviceDAO, mockApplicationAuthenticationService, mockPkiValidator, mockDevicePolicyService,
                mockSecurityAuditLogger, mockUserIdMappingService };
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
        mockSingleSignOnService.initialize(false, null, application, null);
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

        String encodedAuthnRequest = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, applicationKeyPair,
                assertionConsumerService, destinationUrl, null, devices, false);
        AuthnRequest authnRequest = getAuthnRequest(encodedAuthnRequest);

        // expectations
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);
        mockSingleSignOnService.initialize(false, null, application, null);
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

    private AuthnRequest getAuthnRequest(String encodedAuthnRequest)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedAuthnRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        AuthnRequest authnRequest = (AuthnRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return authnRequest;
    }
}
