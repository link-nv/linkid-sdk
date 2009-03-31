package test.unit.net.link.safeonline.model.encap;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
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
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.service.bean.DevicePolicyServiceBean;
import net.link.safeonline.config.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.config.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
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
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.ApplicationIdentityManagerBean;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.bean.DevicesBean;
import net.link.safeonline.model.bean.IdGeneratorBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.model.bean.UsageAgreementManagerBean;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.encap.MobileManager;
import net.link.safeonline.model.encap.bean.EncapDeviceServiceBean;
import net.link.safeonline.model.encap.bean.EncapStartableBean;
import net.link.safeonline.model.encap.bean.MobileManagerBean;
import net.link.safeonline.notification.dao.bean.EndpointReferenceDAOBean;
import net.link.safeonline.notification.dao.bean.NotificationMessageDAOBean;
import net.link.safeonline.notification.dao.bean.NotificationProducerDAOBean;
import net.link.safeonline.notification.service.bean.NotificationProducerServiceBean;
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
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class EncapDeviceTest {

    private EncapDeviceServiceBean testedInstance;

    private Object[]               mockObjects;

    private HistoryDAO             mockHistoryDAO;

    private SecurityAuditLogger    mockSecurityAuditLogger;

    private EntityTestManager      entityTestManager;

    private String                 testUserId;

    private String                 testValidOTP;

    private MobileManager          mockMobileManager;

    private String                 testInvalidOTP;

    private NodeMappingService     mockNodeMappingService;

    private AttributeManagerLWBean attributeManager;

    private String                 testNode;

    private SubjectEntity          testSubject;

    private String                 testMobile;

    private String                 testChallenge;

    private String                 testActivationCode;

    private KeyService             mockKeyService;

    private static Class<?>[]      container = new Class[] { SubjectDAOBean.class, ApplicationDAOBean.class, SubscriptionDAOBean.class,
            AttributeDAOBean.class, TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class, AttributeTypeDAOBean.class,
            ApplicationIdentityDAOBean.class, ConfigGroupDAOBean.class, ConfigItemDAOBean.class, TaskDAOBean.class,
            SchedulingDAOBean.class, TaskHistoryDAOBean.class, ApplicationIdentityManagerBean.class, TrustPointDAOBean.class,
            AttributeProviderDAOBean.class, DeviceDAOBean.class, DeviceClassDAOBean.class, AllowedDeviceDAOBean.class,
            SubjectServiceBean.class, SubjectIdentifierDAOBean.class, IdGeneratorBean.class, UsageAgreementDAOBean.class,
            UsageAgreementManagerBean.class, NodeDAOBean.class, DevicePolicyServiceBean.class, ResourceAuditLoggerBean.class,
            AuditAuditDAOBean.class, AuditContextDAOBean.class, AccessAuditDAOBean.class, SecurityAuditDAOBean.class,
            ResourceAuditDAOBean.class, DevicesBean.class, NotificationProducerServiceBean.class, NotificationProducerDAOBean.class,
            EndpointReferenceDAOBean.class, ApplicationScopeIdDAOBean.class, AttributeCacheDAOBean.class, ApplicationPoolDAOBean.class,
            NotificationMessageDAOBean.class, MobileManagerBean.class };

    private static JndiTestUtils   jndiTestUtils;


    @BeforeClass
    public static void init() {

        jndiTestUtils = new JndiTestUtils();
    }

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

        mockKeyService = createMock(KeyService.class);
        checkOrder(mockKeyService, false);

        KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");

        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(4);
        replay(mockKeyService);

        SafeOnlineTestConfig.loadTestNode(new URL("http://127.0.0.1/"));
        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, container, entityManager);
        systemStartable.postStart();

        Startable encapStartable = EJBTestUtils.newInstance(EncapStartableBean.class, container, entityManager);
        encapStartable.postStart();

        verify(mockKeyService);
        reset(mockKeyService);

        testedInstance = new EncapDeviceServiceBean();
        EJBTestUtils.inject(testedInstance, entityManager);

        AttributeDAO attributeDAO = EJBTestUtils.newInstance(AttributeDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, attributeDAO);

        AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, attributeTypeDAO);

        DeviceDAO deviceDAO = EJBTestUtils.newInstance(DeviceDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, deviceDAO);

        SubjectIdentifierDAO subjectIdentifierDAO = EJBTestUtils.newInstance(SubjectIdentifierDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, subjectIdentifierDAO);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectService.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, subjectService);

        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);

        mockMobileManager = createMock(MobileManager.class);
        EJBTestUtils.inject(testedInstance, mockMobileManager);

        mockNodeMappingService = createMock(NodeMappingService.class);
        EJBTestUtils.inject(testedInstance, mockNodeMappingService);

        mockHistoryDAO = createMock(HistoryDAO.class);
        EJBTestUtils.inject(testedInstance, mockHistoryDAO);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockNodeMappingService, mockHistoryDAO, mockSecurityAuditLogger, mockMobileManager, mockKeyService };

        // setup
        testUserId = UUID.randomUUID().toString();
        testMobile = "+3200000000";
        testValidOTP = "test-otp";
        testInvalidOTP = "test-invalid-otp";
        testNode = "test-node";
        testSubject = subjectService.addSubjectWithoutLogin(testUserId);
        testChallenge = UUID.randomUUID().toString();
        testActivationCode = "activation-code";
        expect(mockNodeMappingService.getSubject(testUserId, testNode)).andReturn(testSubject);
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    @Test
    public void testRegisterAuthenticateRemoveAndReRegisterAuthenticate()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockMobileManager.activate(testMobile, null)).andReturn(testActivationCode);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);

        // prepare
        replay(mockObjects);

        // operate
        assertTrue(testedInstance.getMobiles(testUserId, null).isEmpty());
        testedInstance.register(testMobile);
        testedInstance.requestOTP(testMobile);
        testedInstance.commitRegistration(testNode, testUserId, testValidOTP);

        // get compound device attribute id for later on
        AttributeEntity parentAttribute = attributeManager.getCompoundWhere(testSubject, EncapConstants.ENCAP_DEVICE_ATTRIBUTE,
                EncapConstants.ENCAP_MOBILE_ATTRIBUTE, testMobile);

        assertFalse(testedInstance.getMobiles(testUserId, null).isEmpty());
        testedInstance.requestOTP(testMobile);
        String resultUserId = testedInstance.authenticate(testValidOTP);
        assertEquals(testUserId, resultUserId);

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID))).andReturn(new HistoryEntity());
        mockMobileManager.remove(testMobile);
        expect(mockMobileManager.requestOTP(testMobile)).andThrow(new MobileException(""));

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.remove(testUserId, parentAttribute.getStringValue());

        assertTrue(testedInstance.getMobiles(testUserId, null).isEmpty());
        try {
            testedInstance.requestOTP(testMobile);
            testedInstance.authenticate(testValidOTP);
            fail("Device registration was still found after removing the device.");
        } catch (DeviceRegistrationNotFoundException e) {
            fail("Encap service was expected to fail first.");
        } catch (MobileException e) {
            // expected.
        }

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockNodeMappingService.getSubject(testUserId, testNode)).andReturn(testSubject);
        expect(mockMobileManager.activate(testMobile, null)).andReturn(testActivationCode);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);

        // prepare
        replay(mockObjects);

        // operate
        assertTrue(testedInstance.getMobiles(testUserId, null).isEmpty());
        testedInstance.register(testMobile);
        testedInstance.requestOTP(testMobile);
        testedInstance.commitRegistration(testNode, testUserId, testValidOTP);

        assertFalse(testedInstance.getMobiles(testUserId, null).isEmpty());
        testedInstance.requestOTP(testMobile);
        resultUserId = testedInstance.authenticate(testValidOTP);
        assertEquals(testUserId, resultUserId);

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterAndAuthenticateWithWrongPassword()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID))).andReturn(new HistoryEntity());
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, testUserId, "incorrect mobile token");
        expect(mockMobileManager.activate(testMobile, null)).andReturn(testActivationCode);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testInvalidOTP)).andReturn(false);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.register(testMobile);
        testedInstance.requestOTP(testMobile);
        testedInstance.commitRegistration(testNode, testUserId, testValidOTP);

        testedInstance.requestOTP(testMobile);
        try {
            testedInstance.authenticate(testInvalidOTP);
            fail("Authentication didn't fail, even though the OTP was incorrect.");
        } catch (DeviceAuthenticationException e) {
            // expected.
        }

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterDisableAuthenticateEnableAuthenticate()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockMobileManager.activate(testMobile, null)).andReturn(testActivationCode);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.register(testMobile);
        testedInstance.requestOTP(testMobile);
        testedInstance.commitRegistration(testNode, testUserId, testValidOTP);

        // get compound device attribute id for later on
        AttributeEntity parentAttribute = attributeManager.getCompoundWhere(testSubject, EncapConstants.ENCAP_DEVICE_ATTRIBUTE,
                EncapConstants.ENCAP_MOBILE_ATTRIBUTE, testMobile);

        // operate
        testedInstance.disable(testUserId, parentAttribute.getStringValue());

        testedInstance.requestOTP(testMobile);
        try {
            testedInstance.authenticate(testValidOTP);
            fail("Authentication didn't fail after disabling device.");
        } catch (DeviceDisabledException e) {
            // expected.
        }

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, EncapConstants.ENCAP_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);
        expect(mockMobileManager.requestOTP(testMobile)).andReturn(testChallenge);
        expect(mockMobileManager.verifyOTP(testChallenge, testValidOTP)).andReturn(true);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.requestOTP(testMobile);
        testedInstance.enable(testUserId, testValidOTP);

        testedInstance.requestOTP(testMobile);
        String resultUserId = testedInstance.authenticate(testValidOTP);
        assertEquals(testUserId, resultUserId);

        // verify
        verify(mockObjects);
    }
}
