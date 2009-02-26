package test.unit.net.link.safeonline.model.otpoversms;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.UUID;

import javax.persistence.EntityManager;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.audit.bean.ResourceAuditLoggerBean;
import net.link.safeonline.audit.dao.bean.AccessAuditDAOBean;
import net.link.safeonline.audit.dao.bean.AuditAuditDAOBean;
import net.link.safeonline.audit.dao.bean.AuditContextDAOBean;
import net.link.safeonline.audit.dao.bean.ResourceAuditDAOBean;
import net.link.safeonline.audit.dao.bean.SecurityAuditDAOBean;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.service.bean.DevicePolicyServiceBean;
import net.link.safeonline.config.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.config.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.bean.AllowedDeviceDAOBean;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.ApplicationPoolDAOBean;
import net.link.safeonline.dao.bean.ApplicationScopeIdDAOBean;
import net.link.safeonline.dao.bean.AttributeCacheDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeProviderDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.DeviceClassDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.dao.bean.NodeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubjectIdentifierDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.UsageAgreementDAOBean;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.ApplicationScopeIdEntity;
import net.link.safeonline.entity.AttributeCacheEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.model.bean.ApplicationIdentityManagerBean;
import net.link.safeonline.model.bean.DevicesBean;
import net.link.safeonline.model.bean.IdGeneratorBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.model.bean.UsageAgreementManagerBean;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.bean.OtpOverSmsDeviceServiceBean;
import net.link.safeonline.model.otpoversms.bean.OtpOverSmsManagerBean;
import net.link.safeonline.model.otpoversms.bean.OtpOverSmsStartableBean;
import net.link.safeonline.notification.dao.bean.EndpointReferenceDAOBean;
import net.link.safeonline.notification.dao.bean.NotificationMessageDAOBean;
import net.link.safeonline.notification.dao.bean.NotificationProducerDAOBean;
import net.link.safeonline.notification.service.bean.NotificationProducerServiceBean;
import net.link.safeonline.osgi.OSGIService;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.OSGIConstants.OSGIServiceType;
import net.link.safeonline.osgi.sms.SmsService;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;
import net.link.safeonline.pkix.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.pkix.dao.bean.TrustPointDAOBean;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.tasks.dao.bean.SchedulingDAOBean;
import net.link.safeonline.tasks.dao.bean.TaskDAOBean;
import net.link.safeonline.tasks.dao.bean.TaskHistoryDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;


public class OtpOverSMSDeviceTest {

    protected static final Log          LOG       = LogFactory.getLog(OtpOverSMSDeviceTest.class);

    private OtpOverSmsDeviceServiceBean testedInstance;

    private Object[]                    mockObjects;

    private HistoryDAO                  mockHistoryDAO;

    private SecurityAuditLogger         mockSecurityAuditLogger;

    private EntityTestManager           entityTestManager;

    private String                      testUserId;

    private String                      testValidPIN;

    private OtpOverSmsManagerBean       otpOverSmsManager;

    private String                      testInvalidPIN;

    private NodeMappingService          mockNodeMappingService;

    private String                      testNode;

    private SubjectEntity               testSubject;

    private String                      testMobile;

    private OSGIStartable               mockOSGIStartable;

    private OSGIService                 mockOSGIService;

    private String                      testOsgiServiceName;

    private SmsService                  testSmsService;

    private static Class<?>[]           container = new Class[] { SubjectDAOBean.class, ApplicationDAOBean.class,
            SubscriptionDAOBean.class, AttributeDAOBean.class, TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class,
            AttributeTypeDAOBean.class, ApplicationIdentityDAOBean.class, ConfigGroupDAOBean.class, ConfigItemDAOBean.class,
            TaskDAOBean.class, SchedulingDAOBean.class, TaskHistoryDAOBean.class, ApplicationIdentityManagerBean.class,
            TrustPointDAOBean.class, AttributeProviderDAOBean.class, DeviceDAOBean.class, DeviceClassDAOBean.class,
            AllowedDeviceDAOBean.class, SubjectServiceBean.class, SubjectIdentifierDAOBean.class, IdGeneratorBean.class,
            UsageAgreementDAOBean.class, UsageAgreementManagerBean.class, NodeDAOBean.class, DevicePolicyServiceBean.class,
            ResourceAuditLoggerBean.class, AuditAuditDAOBean.class, AuditContextDAOBean.class, AccessAuditDAOBean.class,
            SecurityAuditDAOBean.class, ResourceAuditDAOBean.class, DevicesBean.class, NotificationProducerServiceBean.class,
            NotificationProducerDAOBean.class, EndpointReferenceDAOBean.class, ApplicationScopeIdDAOBean.class,
            AttributeCacheDAOBean.class, ApplicationPoolDAOBean.class, NotificationMessageDAOBean.class, OtpOverSmsManagerBean.class };


    @Before
    public void setUp()
            throws Exception {

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SubjectEntity.class, ApplicationEntity.class, ApplicationOwnerEntity.class, AttributeEntity.class,
                AttributeTypeEntity.class, SubscriptionEntity.class, TrustDomainEntity.class, ApplicationIdentityEntity.class,
                ConfigGroupEntity.class, ConfigItemEntity.class, ConfigItemValueEntity.class, SchedulingEntity.class, TaskEntity.class,
                TaskHistoryEntity.class, TrustPointEntity.class, ApplicationIdentityAttributeEntity.class,
                AttributeTypeDescriptionEntity.class, AttributeProviderEntity.class, DeviceEntity.class, DeviceClassEntity.class,
                DeviceDescriptionEntity.class, DevicePropertyEntity.class, DeviceClassDescriptionEntity.class, AllowedDeviceEntity.class,
                CompoundedAttributeTypeMemberEntity.class, SubjectIdentifierEntity.class, UsageAgreementEntity.class,
                UsageAgreementTextEntity.class, NodeEntity.class, EndpointReferenceEntity.class,
                NotificationProducerSubscriptionEntity.class, ApplicationScopeIdEntity.class, AttributeCacheEntity.class,
                ApplicationPoolEntity.class);

        EntityManager entityManager = entityTestManager.getEntityManager();

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return authCertificate;
            }
        });

        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return certificate;
            }
        });

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, container, entityManager);
        systemStartable.postStart();

        Startable otpOverSmsStartable = EJBTestUtils.newInstance(OtpOverSmsStartableBean.class, container, entityManager);
        otpOverSmsStartable.postStart();

        testedInstance = new OtpOverSmsDeviceServiceBean();
        EJBTestUtils.inject(testedInstance, entityManager);

        AttributeDAO attributeDAO = EJBTestUtils.newInstance(AttributeDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, attributeDAO);

        AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, attributeTypeDAO);

        SubjectIdentifierDAO subjectIdentifierDAO = EJBTestUtils.newInstance(SubjectIdentifierDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, subjectIdentifierDAO);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectService.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, subjectService);

        mockOSGIStartable = createMock(OSGIStartable.class);
        EJBTestUtils.inject(testedInstance, mockOSGIStartable);

        mockOSGIService = createMock(OSGIService.class);

        otpOverSmsManager = EJBTestUtils.newInstance(OtpOverSmsManagerBean.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, otpOverSmsManager);

        mockNodeMappingService = createMock(NodeMappingService.class);
        EJBTestUtils.inject(testedInstance, mockNodeMappingService);

        mockHistoryDAO = createMock(HistoryDAO.class);
        EJBTestUtils.inject(testedInstance, mockHistoryDAO);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockNodeMappingService, mockHistoryDAO, mockSecurityAuditLogger, mockOSGIStartable, mockOSGIService };

        // setup
        testUserId = UUID.randomUUID().toString();
        testMobile = "+3200000000";
        testValidPIN = "test-pin";
        testInvalidPIN = "test-invalid-pin";
        testNode = "test-node";
        testSubject = subjectService.addSubjectWithoutLogin(testUserId);
        testOsgiServiceName = "test-otp-service";
        testSmsService = new SmsService() {

            public void sendSms(String mobile, String message)
                    throws SmsServiceException {

                LOG.debug("sendSms to " + mobile + ": " + message);
            }
        };
        expect(mockNodeMappingService.getSubject(testUserId, testNode)).andReturn(testSubject);
        expect(mockOSGIStartable.getService(testOsgiServiceName, OSGIServiceType.SMS_SERVICE)).andStubReturn(mockOSGIService);

        Field smsServiceName = testedInstance.getClass().getDeclaredField("smsServiceName");
        smsServiceName.setAccessible(true);
        smsServiceName.set(testedInstance, testOsgiServiceName);
    }

    @Test
    public void testRegisterAuthenticateRemoveReRegisterAuthenticate()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOtp(testMobile);
        testedInstance.register(testNode, testUserId, testValidPIN, getValidOTP());

        assertFalse(testedInstance.isChallenged());
        testedInstance.requestOtp(testMobile);

        assertTrue(testedInstance.isChallenged());
        String resultUserId = testedInstance.authenticate(testValidPIN, getValidOTP());
        assertEquals(testUserId, resultUserId);

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockOSGIStartable.getService(testOsgiServiceName, OSGIServiceType.SMS_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.remove(testUserId, testMobile);

        assertFalse(testedInstance.isChallenged());
        testedInstance.requestOtp(testMobile);

        assertTrue(testedInstance.isChallenged());
        try {
            testedInstance.authenticate(testValidPIN, getValidOTP());
            fail("Device registration was still found after removing the device.");
        } catch (DeviceRegistrationNotFoundException e) {
        }

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockNodeMappingService.getSubject(testUserId, testNode)).andReturn(testSubject);
        expect(mockOSGIStartable.getService(testOsgiServiceName, OSGIServiceType.SMS_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOtp(testMobile);
        testedInstance.register(testNode, testUserId, testValidPIN, getValidOTP());

        assertFalse(testedInstance.isChallenged());
        testedInstance.requestOtp(testMobile);

        assertTrue(testedInstance.isChallenged());
        resultUserId = testedInstance.authenticate(testValidPIN, getValidOTP());
        assertEquals(testUserId, resultUserId);

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterAndAuthenticateWithWrongPIN()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, testUserId, "incorrect pin");
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOtp(testMobile);
        testedInstance.register(testNode, testUserId, testValidPIN, getValidOTP());

        testedInstance.requestOtp(testMobile);
        try {
            testedInstance.authenticate(testInvalidPIN, getValidOTP());
            fail("Authentication didn't fail, even though the PIN was incorrect.");
        } catch (DeviceAuthenticationException e) {
            // expected.
        }

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterAndAuthenticateWithWrongOTP()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, testUserId, "incorrect otp");
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOtp(testMobile);
        testedInstance.register(testNode, testUserId, testValidPIN, getValidOTP());

        testedInstance.requestOtp(testMobile);
        try {
            testedInstance.authenticate(testValidPIN, getInvalidOTP());
            fail("Authentication didn't fail, even though the OTP was incorrect.");
        } catch (DeviceAuthenticationException e) {
            // expected.
        }

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterUpdateAndAuthenticate()
            throws Exception {

        // setup
        String testNewPIN = "new-test-password";

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_UPDATE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOtp(testMobile);
        testedInstance.register(testNode, testUserId, testValidPIN, getValidOTP());

        testedInstance.requestOtp(testMobile);
        testedInstance.update(testUserId, testValidPIN, testNewPIN, getValidOTP());

        testedInstance.requestOtp(testMobile);
        String resultUserId = testedInstance.authenticate(testNewPIN, getValidOTP());
        assertEquals(testUserId, resultUserId);

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterDisableAuthenticateEnableAuthenticate()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOtp(testMobile);
        testedInstance.register(testNode, testUserId, testValidPIN, getValidOTP());
        testedInstance.disable(testUserId, testMobile);

        testedInstance.requestOtp(testMobile);
        try {
            testedInstance.authenticate(testValidPIN, getValidOTP());
            fail("Authentication didn't fail after disabling device.");
        } catch (DeviceDisabledException e) {
        }

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockOSGIStartable.getService(testOsgiServiceName, OSGIServiceType.SMS_SERVICE)).andStubReturn(mockOSGIService);
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();
        expect(mockOSGIService.getService()).andReturn(testSmsService);
        mockOSGIService.ungetService();

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOtp(testMobile);
        testedInstance.enable(testUserId, testValidPIN, getValidOTP());

        testedInstance.requestOtp(testMobile);
        String resultUserId = testedInstance.authenticate(testValidPIN, getValidOTP());
        assertEquals(testUserId, resultUserId);

        // verify
        verify(mockObjects);
    }

    private String getValidOTP()
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Field expectedOtp = testedInstance.getClass().getDeclaredField("expectedOtp");
        expectedOtp.setAccessible(true);

        return (String) expectedOtp.get(testedInstance);
    }

    private String getInvalidOTP()
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        return getValidOTP() + "-invalid";
    }
}
