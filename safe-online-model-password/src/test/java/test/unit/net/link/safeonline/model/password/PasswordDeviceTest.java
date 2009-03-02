package test.unit.net.link.safeonline.model.password;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.bean.PasswordDeviceServiceBean;
import net.link.safeonline.model.password.bean.PasswordManagerBean;
import net.link.safeonline.model.password.bean.PasswordStartableBean;
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

import org.junit.Before;
import org.junit.Test;


public class PasswordDeviceTest {

    private PasswordDeviceServiceBean testedInstance;

    private Object[]                  mockObjects;

    private HistoryDAO                mockHistoryDAO;

    private SecurityAuditLogger       mockSecurityAuditLogger;

    private EntityTestManager         entityTestManager;

    private String                    testUserId;

    private String                    testPassword;

    private PasswordManagerBean       passwordManager;

    private String                    testWrongPassword;

    private NodeMappingService        mockNodeMappingService;

    private String                    testNode;

    private SubjectEntity             testSubject;

    private static Class<?>[]         container = new Class[] { SubjectDAOBean.class, ApplicationDAOBean.class, SubscriptionDAOBean.class,
            AttributeDAOBean.class, TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class, AttributeTypeDAOBean.class,
            ApplicationIdentityDAOBean.class, ConfigGroupDAOBean.class, ConfigItemDAOBean.class, TaskDAOBean.class,
            SchedulingDAOBean.class, TaskHistoryDAOBean.class, ApplicationIdentityManagerBean.class, TrustPointDAOBean.class,
            AttributeProviderDAOBean.class, DeviceDAOBean.class, DeviceClassDAOBean.class, AllowedDeviceDAOBean.class,
            SubjectServiceBean.class, SubjectIdentifierDAOBean.class, IdGeneratorBean.class, UsageAgreementDAOBean.class,
            UsageAgreementManagerBean.class, NodeDAOBean.class, DevicePolicyServiceBean.class, ResourceAuditLoggerBean.class,
            AuditAuditDAOBean.class, AuditContextDAOBean.class, AccessAuditDAOBean.class, SecurityAuditDAOBean.class,
            ResourceAuditDAOBean.class, DevicesBean.class, NotificationProducerServiceBean.class, NotificationProducerDAOBean.class,
            EndpointReferenceDAOBean.class, ApplicationScopeIdDAOBean.class, AttributeCacheDAOBean.class, ApplicationPoolDAOBean.class,
            NotificationMessageDAOBean.class, PasswordManagerBean.class };


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

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, container, entityManager);
        systemStartable.postStart();

        Startable passwordStartable = EJBTestUtils.newInstance(PasswordStartableBean.class, container, entityManager);
        passwordStartable.postStart();

        testedInstance = new PasswordDeviceServiceBean();
        EJBTestUtils.inject(testedInstance, entityManager);

        AttributeDAO attributeDAO = EJBTestUtils.newInstance(AttributeDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, attributeDAO);

        AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAO.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, attributeTypeDAO);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectService.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, subjectService);

        passwordManager = EJBTestUtils.newInstance(PasswordManagerBean.class, container, entityManager);
        EJBTestUtils.inject(testedInstance, passwordManager);

        mockNodeMappingService = createMock(NodeMappingService.class);
        EJBTestUtils.inject(testedInstance, mockNodeMappingService);

        mockHistoryDAO = createMock(HistoryDAO.class);
        EJBTestUtils.inject(testedInstance, mockHistoryDAO);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockNodeMappingService, mockHistoryDAO, mockSecurityAuditLogger };

        // setup
        testUserId = UUID.randomUUID().toString();
        testPassword = "test-password";
        testWrongPassword = "foobar";
        testNode = "test-node";
        testSubject = subjectService.addSubjectWithoutLogin(testUserId);
        expect(mockNodeMappingService.getSubject(testUserId, testNode)).andReturn(testSubject);
    }

    @Test
    public void testRegisterAuthenticateRemoveAndReRegisterAuthenticate()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());

        // prepare
        replay(mockObjects);

        // operate
        assertFalse(testedInstance.isPasswordConfigured(testUserId));
        testedInstance.register(testNode, testUserId, testPassword);

        assertTrue(testedInstance.isPasswordConfigured(testUserId));
        testedInstance.authenticate(testUserId, testPassword);

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.remove(testUserId);

        assertFalse(testedInstance.isPasswordConfigured(testUserId));
        try {
            testedInstance.authenticate(testUserId, testPassword);
            fail("Device registration was still found after removing the device.");
        } catch (DeviceRegistrationNotFoundException e) {
        }

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(mockNodeMappingService.getSubject(testUserId, testNode)).andReturn(testSubject);

        // prepare
        replay(mockObjects);

        // operate
        assertFalse(testedInstance.isPasswordConfigured(testUserId));
        testedInstance.register(testNode, testUserId, testPassword);

        assertTrue(testedInstance.isPasswordConfigured(testUserId));
        testedInstance.authenticate(testUserId, testPassword);

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterAndAuthenticateWithWrongPassword()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, testUserId, "incorrect password");

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.register(testNode, testUserId, testPassword);

        try {
            testedInstance.authenticate(testUserId, testWrongPassword);
            fail("Authentication didn't fail, even though the password was incorrect.");
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
        String testNewPassword = "new-test-password";

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());

        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_UPDATE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());
        // prepare
        replay(mockObjects);

        // operate
        testedInstance.register(testNode, testUserId, testPassword);
        testedInstance.update(testUserId, testPassword, testNewPassword);

        testedInstance.authenticate(testUserId, testNewPassword);

        // verify
        verify(mockObjects);
    }

    @Test
    public void testRegisterDisableAuthenticateEnableAuthenticate()
            throws Exception {

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.register(testNode, testUserId, testPassword);
        testedInstance.disable(testUserId);

        try {
            testedInstance.authenticate(testUserId, testPassword);
            fail("Authentication didn't fail after disabling device.");
        } catch (DeviceDisabledException e) {
        }

        // verify
        verify(mockObjects);
        reset(mockObjects);

        // expectations
        expect(
                mockHistoryDAO.addHistoryEntry(testSubject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(
                        SafeOnlineConstants.DEVICE_PROPERTY, PasswordConstants.PASSWORD_DEVICE_ID))).andReturn(new HistoryEntity());

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.enable(testUserId, testPassword);

        testedInstance.authenticate(testUserId, testPassword);

        // verify
        verify(mockObjects);
    }
}
