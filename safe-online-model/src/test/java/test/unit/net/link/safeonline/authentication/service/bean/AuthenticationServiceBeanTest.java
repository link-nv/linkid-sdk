/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticDomain;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticName;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AuthenticationInitializationException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.DomUtils;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.core.AuthnRequest;
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

        EJBTestUtils.init(this.testedInstance);

        this.mockObjects = new Object[] { this.mockSubjectService, this.mockPasswordDeviceService,
                this.mockApplicationDAO, this.mockSubscriptionDAO, this.mockHistoryDAO, this.mockStatisticDAO,
                this.mockStatisticDataPointDAO, this.mockDeviceDAO, this.mockApplicationAuthenticationService,
                this.mockPkiValidator, this.mockDevicePolicyService };
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

        // stubs
        SubjectEntity subject = new SubjectEntity(login);
        expect(this.mockSubjectService.getSubjectFromUserName(login)).andStubReturn(subject);

        SubjectEntity adminSubject = new SubjectEntity("admin-login");
        ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity("test-application-owner", adminSubject);

        ApplicationEntity application = new ApplicationEntity(applicationName, null, applicationOwner, null, null,
                null, null, null);
        expect(this.mockApplicationDAO.findApplication(applicationName)).andStubReturn(application);

        SubscriptionEntity subscription = new SubscriptionEntity();
        expect(this.mockSubscriptionDAO.findSubscription(subject, application)).andStubReturn(subscription);

        expect(this.mockPasswordDeviceService.authenticate(login, password)).andStubReturn(subject);

        StatisticEntity statistic = new StatisticEntity();
        expect(
                this.mockStatisticDAO.findOrAddStatisticByNameDomainAndApplication(statisticName, statisticDomain,
                        application)).andStubReturn(statistic);
        StatisticDataPointEntity dataPoint = new StatisticDataPointEntity();
        expect(this.mockStatisticDataPointDAO.findOrAddStatisticDataPoint("Login counter", statistic)).andStubReturn(
                dataPoint);

        DeviceClassEntity deviceClass = new DeviceClassEntity(SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
                SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
        DeviceEntity device = new DeviceEntity(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, deviceClass, null,
                null, null, null, null, null);
        expect(this.mockDeviceDAO.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)).andReturn(device);

        // prepare
        replay(this.mockObjects);

        // operate
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
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null);
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
        this.testedInstance.initialize(authnRequest);

        // verify
        verify(this.mockObjects);

        String resultApplicationId = this.testedInstance.getExpectedApplicationId();
        assertEquals(applicationName, resultApplicationId);
        String target = this.testedInstance.getExpectedTarget();
        assertEquals(assertionConsumerService, target);
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
                applicationKeyPair, assertionConsumerService, destinationUrl, null, devices);
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
                        SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS), null, null, null, null, null, null);
        authnDevices.add(passwordDevice);
        expect(this.mockDevicePolicyService.listDevices(SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS))
                .andReturn(authnDevices);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.initialize(authnRequest);

        // verify
        verify(this.mockObjects);

        String resultApplicationId = this.testedInstance.getExpectedApplicationId();
        assertEquals(applicationName, resultApplicationId);
        String target = this.testedInstance.getExpectedTarget();
        assertEquals(assertionConsumerService, target);
        Set<DeviceEntity> resultRequiredDevices = this.testedInstance.getRequiredDevicePolicy();
        assertNotNull(resultRequiredDevices);
        assertTrue(resultRequiredDevices.contains(passwordDevice));
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
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null);
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
            this.testedInstance.initialize(authnRequest);
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
                applicationKeyPair, assertionConsumerService, destinationUrl, null, null);
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
            this.testedInstance.initialize(authnRequest);
        } catch (AuthenticationInitializationException e) {
            // expected
            return;
        }
        junit.framework.Assert.fail();
    }

    private AuthnRequest getAuthnRequest(String encodedAuthnRequest) throws Exception {

        Document doc = DomUtils.parseDocument(encodedAuthnRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        AuthnRequest authnRequest = (AuthnRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return authnRequest;
    }
}
