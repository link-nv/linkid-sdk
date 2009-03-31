/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.AttributeServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class AttributeServiceBeanTest {

    private static final Log  LOG = LogFactory.getLog(AttributeServiceBeanTest.class);

    private EntityTestManager entityTestManager;

    private KeyService        mockKeyService;

    private JndiTestUtils     jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);

        EntityManager entityManager = entityTestManager.getEntityManager();

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        SafeOnlineTestConfig.loadTestNode(new URL("http://127.0.0.1/"));
        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        systemStartable.postStart();

        verify(mockKeyService);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();
    }

    @After
    public void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testGetAttributeFailsIfUserNotSubscribed()
            throws Exception {

        // setup
        String testSubjectLogin = UUID.randomUUID().toString();
        String testAttributeName = UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();

        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName, true, false) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate & verify
        try {
            attributeService.getConfirmedAttributeValue(subject.getUserId(), testAttributeName);
            fail();
        } catch (PermissionDeniedException e) {
            // expected
        }
    }

    @Test
    public void testGetAttributeFailsIfUserNotConfirmed()
            throws Exception {

        // setup
        String testSubjectLogin = UUID.randomUUID().toString();
        String testAttributeName = UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();

        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName, true, false) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        subscriptionService.subscribe(testApplication.getId());

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate & verify
        try {
            attributeService.getConfirmedAttributeValue(subject.getUserId(), testAttributeName);
            fail();
        } catch (PermissionDeniedException e) {
            // expected
        }
    }

    @Test
    public void testGetAttribute()
            throws Exception {

        // setup
        String testSubjectLogin = UUID.randomUUID().toString();
        String testAttributeName = UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();
        String testAttributeValue = UUID.randomUUID().toString();

        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName, true, false) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        AttributeDO testAttribute = new AttributeDO(testAttributeName, DatatypeType.STRING);
        testAttribute.setStringValue(testAttributeValue);
        testAttribute.setEditable(true);
        identityService.saveAttribute(testAttribute);

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate
        Object result = attributeService.getConfirmedAttributeValue(subject.getUserId(), testAttributeName);

        // verify
        assertEquals(testAttributeValue.getClass(), String.class);
        assertEquals(testAttributeValue, result);
    }

    @Test
    public void testGetBooleanAttributeValue()
            throws Exception {

        // setup
        String testSubjectLogin = UUID.randomUUID().toString();
        String testAttributeName = UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();
        Boolean testAttributeValue = Boolean.TRUE;

        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.BOOLEAN, true, true);
        attributeTypeService.add(attributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName, true, false) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        AttributeDO testAttribute = new AttributeDO(testAttributeName, DatatypeType.BOOLEAN);
        testAttribute.setBooleanValue(testAttributeValue);
        testAttribute.setEditable(true);
        identityService.saveAttribute(testAttribute);

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), "application");

        // operate
        Object result = attributeService.getConfirmedAttributeValue(subject.getUserId(), testAttributeName);

        // verify
        assertEquals(result.getClass(), Boolean.class);
        assertEquals(testAttributeValue, result);
    }

    @Test
    public void testGetUnconfirmedAttributeFails()
            throws Exception {

        // setup
        String testSubjectLogin = UUID.randomUUID().toString();
        String testAttributeName = UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();
        String testAttributeValue = UUID.randomUUID().toString();
        String unconfirmedAttributeName = UUID.randomUUID().toString();

        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);
        AttributeTypeEntity unconfirmedAttributeType = new AttributeTypeEntity(unconfirmedAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(unconfirmedAttributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName, true, false) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        AttributeDO testAttribute = new AttributeDO(testAttributeName, DatatypeType.STRING);
        testAttribute.setStringValue(testAttributeValue);
        identityService.saveAttribute(testAttribute);

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate & verify
        try {
            attributeService.getConfirmedAttributeValue(subject.getUserId(), unconfirmedAttributeName);
            fail();
        } catch (PermissionDeniedException e) {
            // expected
        }
    }

    @Test
    public void testGetAttributeFailsIfDataMining()
            throws Exception {

        // setup
        String testSubjectLogin = UUID.randomUUID().toString();
        String testAttributeName = UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();
        String testAttributeValue = UUID.randomUUID().toString();

        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName, true, true) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        AttributeDO testAttribute = new AttributeDO(testAttributeName, DatatypeType.STRING);
        testAttribute.setStringValue(testAttributeValue);
        identityService.saveAttribute(testAttribute);

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate & verify
        try {
            attributeService.getConfirmedAttributeValue(subject.getUserId(), testAttributeName);
            fail();
        } catch (PermissionDeniedException e) {
            // expected
        }

    }

    @Test
    public void testGetMultivaluedAttribute()
            throws Exception {

        // setup
        String testSubjectLogin = UUID.randomUUID().toString();
        String testAttributeName = UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();
        String testAttributeValue1 = UUID.randomUUID().toString();
        String testAttributeValue2 = UUID.randomUUID().toString();

        EntityManager entityManager = entityTestManager.getEntityManager();

        // register test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        // register multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeType.setMultivalued(true);
        attributeTypeService.add(attributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName, true, false) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        AttributeDO testAttribute = new AttributeDO(testAttributeName, DatatypeType.STRING);
        testAttribute.setStringValue(testAttributeValue1);
        testAttribute.setEditable(true);
        identityService.saveAttribute(testAttribute);
        testAttribute.setStringValue(testAttributeValue2);
        identityService.addAttribute(Collections.singletonList(testAttribute));

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate
        Object result = attributeService.getConfirmedAttributeValue(subject.getUserId(), testAttributeName);

        // verify
        assertNotNull(result);
        assertEquals(String[].class, result.getClass());
        String[] stringArrayResult = (String[]) result;
        assertTrue(contains(stringArrayResult, testAttributeValue1));
        assertTrue(contains(stringArrayResult, testAttributeValue2));

        // operate
        LOG.debug("operate2");
        Map<String, Object> resultMap = attributeService.getConfirmedAttributeValues(subject.getUserId());

        // verify
        assertNotNull(resultMap);
        assertEquals(1, resultMap.size());
        assertTrue(resultMap.containsKey(testAttributeName));
        result = resultMap.get(testAttributeName);
        assertNotNull(result);
        assertEquals(String[].class, result.getClass());
        stringArrayResult = (String[]) result;
        assertTrue(contains(stringArrayResult, testAttributeValue1));
        assertTrue(contains(stringArrayResult, testAttributeValue2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getCompoundedAttribute()
            throws Exception {

        // setup
        String testSubjectLogin = "test-subject-login";
        String testApplicationName = "test-application-name";
        String compoundName = "compound-attrib-name";
        String firstMemberName = "member-0-name";
        String secondMemberName = "member-1-name";

        EntityManager entityManager = entityTestManager.getEntityManager();

        // register test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        // register multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);

        AttributeTypeEntity firstAttributeType = new AttributeTypeEntity(firstMemberName, DatatypeType.STRING, true, true);
        firstAttributeType.setMultivalued(true);
        attributeTypeService.add(firstAttributeType);

        AttributeTypeEntity secondAttributeType = new AttributeTypeEntity(secondMemberName, DatatypeType.STRING, true, true);
        secondAttributeType.setMultivalued(true);
        attributeTypeService.add(secondAttributeType);

        entityTestManager.newTransaction();

        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(compoundName, DatatypeType.COMPOUNDED, true, true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.addMember(firstAttributeType, 0, true);
        compoundedAttributeType.addMember(secondAttributeType, 1, true);
        attributeTypeService.add(compoundedAttributeType);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null, null,
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(compoundName, true, false) }), false, false,
                false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        AttributeDO compoundAttribute = new AttributeDO(compoundName, DatatypeType.COMPOUNDED);
        compoundAttribute.setCompounded(true);
        identityService.saveAttribute(compoundAttribute);

        AttributeDO attribute = new AttributeDO(firstMemberName, DatatypeType.STRING);
        attribute.setStringValue("value 0");
        attribute.setEditable(true);
        identityService.saveAttribute(attribute);

        compoundAttribute.setIndex(1);
        identityService.saveAttribute(compoundAttribute);

        attribute = new AttributeDO(secondMemberName, DatatypeType.STRING);
        attribute.setStringValue("value 1");
        attribute.setEditable(true);
        attribute.setIndex(1);
        identityService.saveAttribute(attribute);

        entityTestManager.newTransaction();

        AttributeService attributeService = EJBTestUtils.newInstance(AttributeServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, Long.toString(testApplication.getId()), SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate
        Object result = attributeService.getConfirmedAttributeValue(subject.getUserId(), compoundName);

        // verify
        assertNotNull(result);
        assertEquals(Map[].class, result.getClass());
        Map[] resultMapArray = (Map[]) result;
        assertEquals(2, resultMapArray.length);
        for (Map resultMap : resultMapArray) {
            LOG.debug("result map: " + resultMap);
        }

        Map<String, Object> firstResult = resultMapArray[0];
        assertEquals("value 0", firstResult.get(firstMemberName));
        assertTrue(firstResult.containsKey(secondMemberName));
        assertNull(firstResult.get(secondMemberName));

        Map<String, Object> secondResult = resultMapArray[1];
        assertTrue(secondResult.containsKey(firstMemberName));
        assertNull(secondResult.get(firstMemberName));
        assertEquals("value 1", secondResult.get(secondMemberName));

        // operate
        Map<String, Object> result2 = attributeService.getConfirmedAttributeValues(subject.getUserId());

        // verify
        LOG.debug("result2: " + result2);
        assertTrue(result2.containsKey(compoundName));
        resultMapArray = (Map[]) result2.get(compoundName);

        firstResult = resultMapArray[0];
        assertEquals("value 0", firstResult.get(firstMemberName));
        assertTrue(firstResult.containsKey(secondMemberName));
        assertNull(firstResult.get(secondMemberName));

        secondResult = resultMapArray[1];
        assertTrue(secondResult.containsKey(firstMemberName));
        assertNull(secondResult.get(firstMemberName));
        assertEquals("value 1", secondResult.get(secondMemberName));
    }

    private boolean contains(String[] list, String value) {

        for (String entry : list)
            if (value.equals(entry))
                return true;
        return false;
    }
}
