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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class IdentityServiceBeanTest {

    static final Log      LOG = LogFactory.getLog(IdentityServiceBeanTest.class);
    EntityTestManager     entityTestManager;
    private KeyService    mockKeyService;
    private JndiTestUtils jndiTestUtils;


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
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        final KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineKeyStore.class)).andReturn(
                new PrivateKeyEntry(olasKeyPair.getPrivate(), new Certificate[] { olasCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        systemStartable.postStart();
    }

    @After
    public void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void confirmation()
            throws Exception {

        // setup
        String login = "test-login";
        String applicationName = "test-application";
        String applicationOwnerLogin = "test-application-owner-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationServiceBean userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        SubjectEntity subject = userRegistrationService.registerUser(login);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", "operator");
        userRegistrationService.registerUser(applicationOwnerLogin);
        applicationService.registerApplicationOwner("test-application-owner-name", applicationOwnerLogin);
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-global-operator", "global-operator");
        attributeTypeService.add(new AttributeTypeEntity("test-attribute-type", DatatypeType.STRING, false, false));
        attributeTypeService.add(new AttributeTypeEntity("test-attribute-type-2", DatatypeType.STRING, false, false));
        attributeTypeService.add(new AttributeTypeEntity("test-attribute-type-3", DatatypeType.STRING, false, false));
        attributeTypeService.add(new AttributeTypeEntity("test-attribute-type-4", DatatypeType.STRING, false, false));
        List<IdentityAttributeTypeDO> identity = new LinkedList<IdentityAttributeTypeDO>();
        identity.add(new IdentityAttributeTypeDO("test-attribute-type", true, false));
        identity.add(new IdentityAttributeTypeDO("test-attribute-type-2", true, true));
        applicationService.addApplication(applicationName, null, "test-application-owner-name", null, false, IdScopeType.USER, null, null,
                null, identity, false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);
        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), "user");
        subscriptionService.subscribe(testApplication.getId());

        EJBTestUtils.setJBossPrincipal("test-application-owner-login", "owner");

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), "user");

        // operate
        boolean result = identityService.isConfirmationRequired(testApplication.getId());
        assertTrue(result);

        List<AttributeDO> attribsToConfirm = identityService.listIdentityAttributesToConfirm(testApplication.getId(), Locale.getDefault());
        assertEquals(2, attribsToConfirm.size());
        for (AttributeDO attribToConfirm : attribsToConfirm) {
            if (attribToConfirm.getName().equals("test-attribute-type")) {
                assertFalse(attribToConfirm.isDataMining());
            } else {
                assertEquals("test-attribute-type-2", attribToConfirm.getName());
                assertTrue(attribToConfirm.isDataMining());
            }
        }

        identityService.confirmIdentity(testApplication.getId());
        entityTestManager.getEntityManager().flush();
        assertFalse(identityService.isConfirmationRequired(testApplication.getId()));

        attribsToConfirm = identityService.listIdentityAttributesToConfirm(testApplication.getId(), Locale.getDefault());
        assertTrue(attribsToConfirm.isEmpty());

        Set<ApplicationIdentityAttributeEntity> currentIdentity = applicationService.getCurrentApplicationIdentity(testApplication.getId());
        assertEquals(2, currentIdentity.size());
        Iterator<ApplicationIdentityAttributeEntity> iter = currentIdentity.iterator();
        String attributeTypeName = iter.next().getAttributeTypeName();
        assertTrue(attributeTypeName.equals("test-attribute-type") || attributeTypeName.equals("test-attribute-type-2"));
        attributeTypeName = iter.next().getAttributeTypeName();
        assertTrue(attributeTypeName.equals("test-attribute-type") || attributeTypeName.equals("test-attribute-type-2"));

        identity.add(new IdentityAttributeTypeDO("test-attribute-type-3", true, false));
        identity.add(new IdentityAttributeTypeDO("test-attribute-type-4", true, true));

        applicationService.updateApplicationIdentity(testApplication.getId(), identity);
        assertTrue(identityService.isConfirmationRequired(testApplication.getId()));

        attribsToConfirm = identityService.listIdentityAttributesToConfirm(testApplication.getId(), Locale.getDefault());
        assertEquals(2, attribsToConfirm.size());
        for (AttributeDO attribToConfirm : attribsToConfirm) {
            if (attribToConfirm.getName().equals("test-attribute-type-3")) {
                assertFalse(attribToConfirm.isDataMining());
            } else {
                assertEquals("test-attribute-type-4", attribToConfirm.getName());
                assertTrue(attribToConfirm.isDataMining());
            }
        }

        identityService.confirmIdentity(testApplication.getId());
        assertFalse(identityService.isConfirmationRequired(testApplication.getId()));
    }

    @Test
    public void compoundedConfirmation()
            throws Exception {

        // setup
        String login = "test-login";
        String applicationName = "test-application";
        String applicationOwnerLogin = "test-application-owner-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationServiceBean userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", "operator");
        userRegistrationService.registerUser(applicationOwnerLogin);
        applicationService.registerApplicationOwner("test-application-owner-name", applicationOwnerLogin);
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-global-operator", "global-operator");

        attributeTypeService.add(new AttributeTypeEntity("test-attribute-type", DatatypeType.STRING, false, false));
        attributeTypeService.add(new AttributeTypeEntity("test-attribute-type-2", DatatypeType.STRING, false, false));

        attributeTypeService.add(new AttributeTypeEntity("test-compounded-type", DatatypeType.COMPOUNDED, false, false));

        applicationService.addApplication(applicationName, null, "test-application-owner-name", null, false, IdScopeType.USER, null, null,
                null, Collections.singletonList(new IdentityAttributeTypeDO("test-compounded-type", true, false)), false, false, false,
                null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);
        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), "user");
        subscriptionService.subscribe(testApplication.getId());

        EJBTestUtils.setJBossPrincipal("test-application-owner-login", "owner");

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), "user");

        // operate
        boolean result = identityService.isConfirmationRequired(testApplication.getId());
        assertTrue(result);

        List<AttributeDO> attribsToConfirm = identityService.listIdentityAttributesToConfirm(testApplication.getId(), Locale.getDefault());
        assertEquals(1, attribsToConfirm.size());
        assertEquals("test-compounded-type", attribsToConfirm.get(0).getName());
        identityService.confirmIdentity(testApplication.getId());
        entityTestManager.getEntityManager().flush();
        assertFalse(identityService.isConfirmationRequired(testApplication.getId()));

        attribsToConfirm = identityService.listIdentityAttributesToConfirm(testApplication.getId(), Locale.getDefault());
        assertTrue(attribsToConfirm.isEmpty());

        Set<ApplicationIdentityAttributeEntity> currentIdentity = applicationService.getCurrentApplicationIdentity(testApplication.getId());
        assertEquals(1, currentIdentity.size());
        assertEquals("test-compounded-type", currentIdentity.iterator().next().getAttributeTypeName());
    }

    @Test
    public void isConfirmationRequiredOnEmptyIdentityGivesFalse()
            throws Exception {

        // setup
        String login = "test-login";
        String applicationName = "test-application";
        String applicationOwnerLogin = "test-application-owner-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", "operator");
        userRegistrationService.registerUser(applicationOwnerLogin);
        applicationService.registerApplicationOwner("test-application-owner-name", applicationOwnerLogin);
        applicationService.addApplication(applicationName, null, "test-application-owner-name", null, false, IdScopeType.USER, null, null,
                null, new LinkedList<IdentityAttributeTypeDO>(), false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);

        EJBTestUtils.setJBossPrincipal("test-application-owner-login", "owner");

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), "user");

        // operate
        boolean result = identityService.isConfirmationRequired(testApplication.getId());
        assertFalse(result);
    }

    @Test
    public void removeMultivaluedAttribute()
            throws Exception {

        // setup
        String login = "test-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        // operate: register the test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        // operate
        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);

        // operate: add multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        String attributeName = "test-attribute-name";
        AttributeTypeEntity attributeType = new AttributeTypeEntity(attributeName, DatatypeType.STRING, true, true);
        attributeType.setMultivalued(true);
        attributeTypeService.add(attributeType);

        refreshTransaction(entityManager);

        // operate: save an attribute
        AttributeDO attribute = new AttributeDO(attributeName, DatatypeType.STRING, true, 0, null, null, true, true, "value 1", null);
        identityService.saveAttribute(attribute);

        refreshTransaction(entityManager);

        // operate: remove a single multi-valued attribute
        identityService.removeAttribute(attribute);

        refreshTransaction(entityManager);

        // operate: save 2 multivalued attributes
        identityService.saveAttribute(attribute);
        AttributeDO attribute2 = new AttributeDO(attributeName, DatatypeType.STRING, true, 1, null, null, true, true, "value 2", null);
        identityService.saveAttribute(attribute2);

        refreshTransaction(entityManager);

        // operate: remove first attribute
        identityService.removeAttribute(attribute);

        refreshTransaction(entityManager);

        // verify: the remaining attribute should have index 0 and value 'value
        // 2'.
        AttributeDAO attributeDAO = EJBTestUtils.newInstance(AttributeDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
        List<AttributeEntity> resultAttributes = attributeDAO.listAttributes(subject, attributeType);

        assertEquals(1, resultAttributes.size());
        AttributeEntity resultAttribute = resultAttributes.get(0);
        assertEquals(0, resultAttribute.getAttributeIndex());
        assertEquals("value 2", resultAttribute.getStringValue());
    }

    @Test
    public void removeMultivaluedCompoundedAttribute()
            throws Exception {

        // setup
        String login = "test-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        // operate: register the test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        // operate: add multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);

        String attributeName0 = "test-attribute-name-0";
        AttributeTypeEntity attributeType0 = new AttributeTypeEntity(attributeName0, DatatypeType.STRING, true, true);
        attributeType0.setMultivalued(true);
        attributeTypeService.add(attributeType0);

        String attributeName1 = "test-attribute-name-1";
        AttributeTypeEntity attributeType1 = new AttributeTypeEntity(attributeName1, DatatypeType.BOOLEAN, true, true);
        attributeType1.setMultivalued(true);
        attributeTypeService.add(attributeType1);

        refreshTransaction(entityManager);

        String compoundedAttributeName = "test-comp-attrib-name";
        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(compoundedAttributeName, DatatypeType.COMPOUNDED, true, true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.addMember(attributeType0, 0, true);
        compoundedAttributeType.addMember(attributeType1, 1, true);
        attributeTypeService.add(compoundedAttributeType);

        // operate: save attribute
        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);

        AttributeDO compoundedAttribute0 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 0, null, null, true,
                true, null, null);
        compoundedAttribute0.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute0);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 0, null, null, true, true, "value 0", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 0, null, null, true, true, null,
                Boolean.TRUE));

        AttributeDO compoundedAttribute1 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 1, null, null, true,
                true, null, null);
        compoundedAttribute1.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute1);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 1, null, null, true, true, "value 1", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 1, null, null, true, true, null,
                Boolean.FALSE));

        AttributeDO compoundedAttribute2 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 2, null, null, true,
                true, null, null);
        compoundedAttribute2.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute2);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 2, null, null, true, true, "value 2", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 2, null, null, true, true, null,
                Boolean.FALSE));

        refreshTransaction(entityManager);

        // operate: add application
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String applicationOwnerName = "test-application-owner-name";
        applicationService.registerApplicationOwner(applicationOwnerName, login);
        String applicationName = "test-application";
        List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
        initialApplicationIdentityAttributes.add(new IdentityAttributeTypeDO(compoundedAttributeName, true, false));
        applicationService.addApplication(applicationName, null, applicationOwnerName, null, false, IdScopeType.USER, null, null, null,
                initialApplicationIdentityAttributes, false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);

        // operate: subscribe user to application
        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), "user");
        subscriptionService.subscribe(testApplication.getId());

        identityService.confirmIdentity(testApplication.getId());

        // operate: remove a single multi-valued attribute
        identityService.removeAttribute(new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 1, null, null, true, true,
                null, null));

        refreshTransaction(entityManager);

        // verify
        List<AttributeDO> resultAttributes = identityService.listAttributes(null);
        LOG.debug("result attributes: " + resultAttributes);
        assertEquals(6, resultAttributes.size());

        assertEquals(compoundedAttributeName, resultAttributes.get(0).getName());

        assertEquals(attributeName0, resultAttributes.get(1).getName());
        assertEquals("value 0", resultAttributes.get(1).getStringValue());

        assertEquals(attributeName1, resultAttributes.get(2).getName());
        assertEquals(Boolean.TRUE, resultAttributes.get(2).getBooleanValue());

        assertEquals(compoundedAttributeName, resultAttributes.get(3).getName());

        assertEquals(attributeName0, resultAttributes.get(4).getName());
        assertEquals("value 2", resultAttributes.get(4).getStringValue());

        assertEquals(attributeName1, resultAttributes.get(5).getName());
        assertEquals(Boolean.FALSE, resultAttributes.get(5).getBooleanValue());
    }

    /**
     * Tests whether we can remove a compounded attribute record when some of the member attribute values are missing for the record to be
     * removed. Also checks whether we can remove all the member attribute values. Even if the member attribute types are marked as
     * non-user-editable it's possible for the user to remove the attribute record if the compounded attribute type is marked as
     * user-editable.
     * 
     * @throws Exception
     */
    @Test
    public void removeAllowMemberCompoundAttributesToBeEmpty()
            throws Exception {

        // setup
        String login = "test-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        // operate: register the test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        // operate: add multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);

        String attributeName0 = "editable-member-attribute-attribute-name-0";
        AttributeTypeEntity attributeType0 = new AttributeTypeEntity(attributeName0, DatatypeType.STRING, true, true);
        attributeType0.setMultivalued(true);
        attributeTypeService.add(attributeType0);

        String attributeName1 = "non-editable-member-attribute-name-1";
        AttributeTypeEntity attributeType1 = new AttributeTypeEntity(attributeName1, DatatypeType.BOOLEAN, true, false);
        attributeType1.setMultivalued(true);
        attributeTypeService.add(attributeType1);

        refreshTransaction(entityManager);

        String compoundedAttributeName = "test-comp-attrib-name";
        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(compoundedAttributeName, DatatypeType.COMPOUNDED, true, true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.addMember(attributeType0, 0, true);
        compoundedAttributeType.addMember(attributeType1, 1, true);
        attributeTypeService.add(compoundedAttributeType);

        // operate: save attribute
        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);

        AttributeDO compoundedAttribute0 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 0, null, null, true,
                true, null, null);
        compoundedAttribute0.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute0);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 0, null, null, true, true, "value 0", null));

        AttributeDO compoundedAttribute1 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 1, null, null, true,
                true, null, null);
        compoundedAttribute1.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute1);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 1, null, null, true, true, "value 1", null));

        AttributeDO compoundedAttribute2 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 2, null, null, true,
                true, null, null);
        compoundedAttribute2.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute2);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 2, null, null, true, true, "value 2", null));

        refreshTransaction(entityManager);

        // operate: add application
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String applicationOwnerName = "test-application-owner-name";
        applicationService.registerApplicationOwner(applicationOwnerName, login);
        String applicationName = "test-application";
        List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
        initialApplicationIdentityAttributes.add(new IdentityAttributeTypeDO(compoundedAttributeName, true, false));
        applicationService.addApplication(applicationName, null, applicationOwnerName, null, false, IdScopeType.USER, null, null, null,
                initialApplicationIdentityAttributes, false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);

        // operate: subscribe user to application
        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), "user");
        subscriptionService.subscribe(testApplication.getId());

        identityService.confirmIdentity(testApplication.getId());

        // operate: remove a single multi-valued attribute
        identityService.removeAttribute(new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 1, null, null, true, true,
                null, null));

        refreshTransaction(entityManager);

        // verify
        List<AttributeDO> resultAttributes = identityService.listAttributes(null);
        LOG.debug("result attributes: " + resultAttributes);
        assertEquals(6, resultAttributes.size());
    }


    static class RequiredCompoundedMissingAttributesScenario implements MissingAttributesScenario {

        private final String COMP_ATT_NAME = "test-compounded-attribute-type";
        private final String REQ_ATT_NAME  = "test-required-attribute-type";
        private final String OPT_ATT_NAME  = "test-optional-attribute-type";


        public void init(AttributeTypeDAO attributeTypeDAO, ApplicationIdentityDAO applicationIdentityDAO,
                         ApplicationIdentityEntity applicationIdentity, AttributeDAO attributeDAO, SubjectEntity subject) {

            AttributeTypeEntity requiredAttributeType = new AttributeTypeEntity(REQ_ATT_NAME, DatatypeType.STRING, true, true);
            attributeTypeDAO.addAttributeType(requiredAttributeType);

            AttributeTypeEntity optionalAttributeType = new AttributeTypeEntity(OPT_ATT_NAME, DatatypeType.STRING, true, false);
            attributeTypeDAO.addAttributeType(optionalAttributeType);

            AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(COMP_ATT_NAME, DatatypeType.COMPOUNDED, true, true);
            compoundedAttributeType.addMember(requiredAttributeType, 0, true);
            compoundedAttributeType.addMember(optionalAttributeType, 1, false);
            attributeTypeDAO.addAttributeType(compoundedAttributeType);

            applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, compoundedAttributeType, true, false);
        }

        public void verify(List<AttributeDO> result) {

            assertNotNull(result);
            LOG.debug("result attribute: " + result);
            assertEquals(3, result.size());

            assertEquals(COMP_ATT_NAME, result.get(0).getName());
            assertTrue(result.get(0).isCompounded());
            assertTrue(result.get(0).isEditable());

            assertEquals(REQ_ATT_NAME, result.get(1).getName());
            assertTrue(result.get(1).isMember());
            assertTrue(result.get(1).isEditable());

            assertEquals(OPT_ATT_NAME, result.get(2).getName());
            assertTrue(result.get(2).isMember());
            assertFalse(result.get(2).isEditable());

        }
    }

    static class OptionalCompoundedMissingAttributesScenario implements MissingAttributesScenario {

        private final String COMP_ATT_NAME = "test-compounded-attribute-type";
        private final String REQ_ATT_NAME  = "test-required-attribute-type";
        private final String OPT_ATT_NAME  = "test-optional-attribute-type";


        public void init(AttributeTypeDAO attributeTypeDAO, ApplicationIdentityDAO applicationIdentityDAO,
                         ApplicationIdentityEntity applicationIdentity, AttributeDAO attributeDAO, SubjectEntity subject) {

            AttributeTypeEntity requiredAttributeType = new AttributeTypeEntity(REQ_ATT_NAME, DatatypeType.STRING, true, true);
            attributeTypeDAO.addAttributeType(requiredAttributeType);

            AttributeTypeEntity optionalAttributeType = new AttributeTypeEntity(OPT_ATT_NAME, DatatypeType.STRING, true, true);
            attributeTypeDAO.addAttributeType(optionalAttributeType);

            AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(COMP_ATT_NAME, DatatypeType.COMPOUNDED, true, true);
            compoundedAttributeType.addMember(requiredAttributeType, 0, true);
            compoundedAttributeType.addMember(optionalAttributeType, 1, false);
            attributeTypeDAO.addAttributeType(compoundedAttributeType);

            applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, compoundedAttributeType, false, false);

            AttributeEntity optionalAttribute = attributeDAO.addAttribute(optionalAttributeType, subject);
            optionalAttribute.setValue("value");
        }

        public void verify(List<AttributeDO> result) {

            assertNotNull(result);
            LOG.debug("result attribute: " + result);
            assertEquals(0, result.size());
        }
    }


    @Test
    public void optionalCompoundedMissingAttribute()
            throws Exception {

        OptionalCompoundedMissingAttributesScenario scenario = new OptionalCompoundedMissingAttributesScenario();
        MissingAttributesScenarioRunner runner = new MissingAttributesScenarioRunner();
        runner.run(scenario);
    }

    @Test
    public void requiredCompoundedMissingAttribute()
            throws Exception {

        RequiredCompoundedMissingAttributesScenario scenario = new RequiredCompoundedMissingAttributesScenario();
        MissingAttributesScenarioRunner runner = new MissingAttributesScenarioRunner();
        runner.run(scenario);
    }

    @Test
    public void requiredEmptyMissingAttribute()
            throws Exception {

        RequiredEmptyMissingAttributesScenario scenario = new RequiredEmptyMissingAttributesScenario();
        MissingAttributesScenarioRunner runner = new MissingAttributesScenarioRunner();
        runner.run(scenario);
    }


    interface MissingAttributesScenario {

        void init(AttributeTypeDAO attributeTypeDAO, ApplicationIdentityDAO applicationIdentityDAO,
                  ApplicationIdentityEntity applicationIdentity, AttributeDAO attributeDAO, SubjectEntity subject);

        void verify(List<AttributeDO> result);
    }

    static class RequiredEmptyMissingAttributesScenario implements MissingAttributesScenario {

        private AttributeTypeEntity attributeType;


        public void init(AttributeTypeDAO attributeTypeDAO, ApplicationIdentityDAO applicationIdentityDAO,
                         ApplicationIdentityEntity applicationIdentity, AttributeDAO attributeDAO, SubjectEntity subject) {

            attributeType = new AttributeTypeEntity("attribute-type-" + UUID.randomUUID().toString(), DatatypeType.STRING, true, true);
            attributeTypeDAO.addAttributeType(attributeType);

            applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, attributeType, true, false);
        }

        public void verify(List<AttributeDO> result) {

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(attributeType.getName(), result.get(0).getName());
        }
    }

    static class RequiredFilledMissingAttributesScenario implements MissingAttributesScenario {

        private AttributeTypeEntity attributeType;


        public void init(AttributeTypeDAO attributeTypeDAO, ApplicationIdentityDAO applicationIdentityDAO,
                         ApplicationIdentityEntity applicationIdentity, AttributeDAO attributeDAO, SubjectEntity subject) {

            attributeType = new AttributeTypeEntity("attribute-type-" + UUID.randomUUID().toString(), DatatypeType.STRING, true, true);
            attributeTypeDAO.addAttributeType(attributeType);

            applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, attributeType, true, false);

            AttributeEntity attribute = attributeDAO.addAttribute(attributeType, subject);
            attribute.setValue("hello world");
        }

        public void verify(List<AttributeDO> result) {

            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }


    @Test
    public void requiredFilledMissingAttribute()
            throws Exception {

        RequiredFilledMissingAttributesScenario scenario = new RequiredFilledMissingAttributesScenario();
        MissingAttributesScenarioRunner runner = new MissingAttributesScenarioRunner();
        runner.run(scenario);
    }


    static class OptionalMissingAttributesScenario implements MissingAttributesScenario {

        private AttributeTypeEntity attributeType;


        public void init(AttributeTypeDAO attributeTypeDAO, ApplicationIdentityDAO applicationIdentityDAO,
                         ApplicationIdentityEntity applicationIdentity, AttributeDAO attributeDAO, SubjectEntity subject) {

            attributeType = new AttributeTypeEntity("attribute-type-" + UUID.randomUUID().toString(), DatatypeType.STRING, true, true);
            attributeTypeDAO.addAttributeType(attributeType);

            applicationIdentityDAO.addApplicationIdentityAttribute(applicationIdentity, attributeType, false, false);
        }

        public void verify(List<AttributeDO> result) {

            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }


    @Test
    public void optionalMissingAttribute()
            throws Exception {

        OptionalMissingAttributesScenario scenario = new OptionalMissingAttributesScenario();
        MissingAttributesScenarioRunner runner = new MissingAttributesScenarioRunner();
        runner.run(scenario);
    }


    class MissingAttributesScenarioRunner {

        public void run(MissingAttributesScenario scenario)
                throws Exception {

            // setup
            EntityManager entityManager = entityTestManager.getEntityManager();
            String login = "test-login-" + UUID.randomUUID().toString();
            String ownerLogin = "test-subject-login-" + UUID.randomUUID().toString();

            SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                    entityManager);
            SubjectEntity subject = subjectService.addSubject(login);
            SubjectEntity ownerSubject = subjectService.addSubject(ownerLogin);

            IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                    entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
            String applicationName = "test-application-name-" + UUID.randomUUID().toString();

            ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(ApplicationOwnerDAOBean.class,
                    SafeOnlineTestContainer.sessionBeans, entityManager);
            ApplicationOwnerEntity applicationOwner = applicationOwnerDAO.addApplicationOwner("test-application-owner", ownerSubject);

            ApplicationDAO applicationDAO = EJBTestUtils.newInstance(ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                    entityManager);
            ApplicationEntity application = applicationDAO.addApplication(applicationName, null, applicationOwner, null, null, null, null);

            AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                    entityManager);

            ApplicationIdentityDAO applicationIdentityDAO = EJBTestUtils.newInstance(ApplicationIdentityDAOBean.class,
                    SafeOnlineTestContainer.sessionBeans, entityManager);
            ApplicationIdentityEntity applicationIdentity = applicationIdentityDAO.addApplicationIdentity(application, 0);

            AttributeDAO attributeDAO = EJBTestUtils.newInstance(AttributeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                    entityManager);

            scenario.init(attributeTypeDAO, applicationIdentityDAO, applicationIdentity, attributeDAO, subject);

            // operate
            List<AttributeDO> result = identityService.listMissingAttributes(application.getId(), null);

            // verify
            scenario.verify(result);
        }
    }


    @Test
    public void listAttributes()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        String userLogin = "test-user-login-" + UUID.randomUUID().toString();

        // register user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity userSubject = userRegistrationService.registerUser(userLogin);

        // add attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "operator", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity attributeType = new AttributeTypeEntity();
        String attributeName = "test-attribute-type-name-" + UUID.randomUUID().toString();
        attributeType.setName(attributeName);
        attributeType.setType(DatatypeType.STRING);
        attributeType.setUserVisible(true);
        attributeType.setUserEditable(true);
        attributeTypeService.add(attributeType);

        // operate: add application
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String applicationOwnerName = "test-application-owner-name";
        applicationService.registerApplicationOwner(applicationOwnerName, userLogin);
        String applicationName = "test-application";
        List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
        initialApplicationIdentityAttributes.add(new IdentityAttributeTypeDO(attributeName, true, false));
        applicationService.addApplication(applicationName, null, applicationOwnerName, null, false, IdScopeType.USER, null, null, null,
                initialApplicationIdentityAttributes, false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);

        // operate: subscribe user to application
        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, userSubject.getUserId(), "user");
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, userSubject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        // add attribute value
        String attributeValue = "test-attribute-value-" + UUID.randomUUID().toString();
        AttributeDO attribute = new AttributeDO(attributeName, DatatypeType.STRING, false, 0, null, null, true, false, attributeValue, null);
        identityService.saveAttribute(attribute);

        // operate
        List<AttributeDO> resultAttributes = identityService.listAttributes(null);

        // verify
        assertNotNull(resultAttributes);
        assertEquals(1, resultAttributes.size());
        assertEquals(attributeName, resultAttributes.get(0).getName());
        assertEquals(attributeValue, resultAttributes.get(0).getStringValue());
    }

    @Test
    public void listCompoundedAttributes()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        String userLogin = "test-user-login-" + UUID.randomUUID().toString();

        // register user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity userSubject = userRegistrationService.registerUser(userLogin);

        // add attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "operator", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        AttributeTypeEntity requiredAttributeType = new AttributeTypeEntity();
        String requiredAttributeName = "test-required-attribute-type-name-" + UUID.randomUUID().toString();
        requiredAttributeType.setName(requiredAttributeName);
        requiredAttributeType.setType(DatatypeType.STRING);
        requiredAttributeType.setUserVisible(true);
        requiredAttributeType.setUserEditable(true);
        requiredAttributeType.setMultivalued(true);
        attributeTypeService.add(requiredAttributeType);

        AttributeTypeEntity optionalAttributeType = new AttributeTypeEntity();
        String optionalAttributeName = "test-optional-attribute-type-name-" + UUID.randomUUID().toString();
        optionalAttributeType.setName(optionalAttributeName);
        optionalAttributeType.setType(DatatypeType.STRING);
        optionalAttributeType.setUserVisible(true);
        optionalAttributeType.setUserEditable(true);
        optionalAttributeType.setMultivalued(true);
        attributeTypeService.add(optionalAttributeType);

        refreshTransaction(entityManager);

        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity();
        String compoundedAttributeName = "test-compounded-attribute-type-name-" + UUID.randomUUID().toString();
        compoundedAttributeType.setName(compoundedAttributeName);
        compoundedAttributeType.setType(DatatypeType.COMPOUNDED);
        compoundedAttributeType.setUserEditable(true);
        compoundedAttributeType.setUserVisible(true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.addMember(requiredAttributeType, 0, true);
        compoundedAttributeType.addMember(optionalAttributeType, 1, false);
        attributeTypeService.add(compoundedAttributeType);

        // operate: add application
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String applicationOwnerName = "test-application-owner-name";
        applicationService.registerApplicationOwner(applicationOwnerName, userLogin);
        String applicationName = "test-application";
        List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
        initialApplicationIdentityAttributes.add(new IdentityAttributeTypeDO(compoundedAttributeName, true, false));
        applicationService.addApplication(applicationName, null, applicationOwnerName, null, false, IdScopeType.USER, null, null, null,
                initialApplicationIdentityAttributes, false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);

        // operate: subscribe user to application
        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, userSubject.getUserId(), "user");
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, userSubject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        // add attribute value
        AttributeDO compoundedAttribute = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 0, null, null, true,
                true, null, null);
        compoundedAttribute.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute);

        String requiredAttributeValue = "test-attribute-value-" + UUID.randomUUID().toString();
        AttributeDO requiredAttribute = new AttributeDO(requiredAttributeName, DatatypeType.STRING, true, 0, null, null, true, false,
                requiredAttributeValue, null);
        identityService.saveAttribute(requiredAttribute);

        // operate
        List<AttributeDO> resultAttributes = identityService.listAttributes(null);

        // verify
        assertNotNull(resultAttributes);
        assertEquals(3, resultAttributes.size());

        assertEquals(compoundedAttributeName, resultAttributes.get(0).getName());
        assertTrue(resultAttributes.get(0).isCompounded());
        assertTrue(resultAttributes.get(0).isEditable());

        assertEquals(requiredAttributeName, resultAttributes.get(1).getName());
        assertTrue(resultAttributes.get(1).isMember());
        assertEquals(requiredAttributeValue, resultAttributes.get(1).getStringValue());
        assertFalse(resultAttributes.get(1).isEditable());

        assertEquals(optionalAttributeName, resultAttributes.get(2).getName());
        assertTrue(resultAttributes.get(2).isMember());
        assertFalse(resultAttributes.get(2).isEditable());
    }

    @Test
    public void getAttributeEditContext()
            throws Exception {

        // setup
        String login = "test-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        // operate: register the test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        // operate: add multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);

        String attributeName0 = "test-attribute-name-0";
        AttributeTypeEntity attributeType0 = new AttributeTypeEntity(attributeName0, DatatypeType.STRING, true, true);
        attributeType0.setMultivalued(true);
        attributeTypeService.add(attributeType0);

        String attributeName1 = "test-attribute-name-1";
        AttributeTypeEntity attributeType1 = new AttributeTypeEntity(attributeName1, DatatypeType.BOOLEAN, true, true);
        attributeType1.setMultivalued(true);
        attributeTypeService.add(attributeType1);

        refreshTransaction(entityManager);

        String compoundedAttributeName = "test-comp-attrib-name";
        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(compoundedAttributeName, DatatypeType.COMPOUNDED, true, true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.addMember(attributeType0, 0, true);
        compoundedAttributeType.addMember(attributeType1, 1, true);
        attributeTypeService.add(compoundedAttributeType);

        // operate: save attribute
        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);

        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 0, null, null, true, true, "value 0", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 0, null, null, true, true, null,
                Boolean.FALSE));
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 1, null, null, true, true, "value 1", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 1, null, null, true, true, null,
                Boolean.TRUE));

        refreshTransaction(entityManager);

        // operate
        AttributeDO queryAttribute = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 1, null, null, true, false,
                null, null);
        queryAttribute.setCompounded(true);
        List<AttributeDO> result = identityService.getAttributeEditContext(queryAttribute);

        // verify
        assertNotNull(result);
        LOG.debug("result edit context: " + result);
        assertEquals(3, result.size());

        assertEquals(compoundedAttributeName, result.get(0).getName());
        assertTrue(result.get(0).isCompounded());

        assertEquals(attributeName0, result.get(1).getName());
        assertTrue(result.get(1).isMember());
        assertEquals("value 1", result.get(1).getStringValue());

        assertEquals(attributeName1, result.get(2).getName());
        assertTrue(result.get(2).isMember());
        assertEquals(Boolean.TRUE, result.get(2).getBooleanValue());
    }

    @Test
    public void getAttributeEditContextWithNonVisibleMember()
            throws Exception {

        // setup
        String login = "test-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        // operate: register the test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        // operate: add multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);

        String attributeName0 = "test-attribute-name-0";
        AttributeTypeEntity attributeType0 = new AttributeTypeEntity(attributeName0, DatatypeType.STRING, true, true);
        attributeType0.setMultivalued(true);
        attributeTypeService.add(attributeType0);

        /*
         * Next one is not user visible.
         */
        String attributeName1 = "test-attribute-name-1";
        AttributeTypeEntity attributeType1 = new AttributeTypeEntity(attributeName1, DatatypeType.BOOLEAN, false, true);
        attributeType1.setMultivalued(true);
        attributeTypeService.add(attributeType1);

        refreshTransaction(entityManager);

        String compoundedAttributeName = "test-comp-attrib-name";
        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(compoundedAttributeName, DatatypeType.COMPOUNDED, true, true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.addMember(attributeType0, 0, true);
        compoundedAttributeType.addMember(attributeType1, 1, true);
        attributeTypeService.add(compoundedAttributeType);

        // operate: save attribute
        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);

        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 0, null, null, true, true, "value 0", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 0, null, null, true, true, null,
                Boolean.FALSE));
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 1, null, null, true, true, "value 1", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 1, null, null, true, true, null,
                Boolean.TRUE));

        refreshTransaction(entityManager);

        // operate
        AttributeDO queryAttribute = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 1, null, null, true, false,
                null, null);
        queryAttribute.setCompounded(true);
        List<AttributeDO> result = identityService.getAttributeEditContext(queryAttribute);

        // verify
        assertNotNull(result);
        LOG.debug("result edit context: " + result);
        assertEquals(2, result.size());

        assertEquals(compoundedAttributeName, result.get(0).getName());
        assertTrue(result.get(0).isCompounded());

        assertEquals(attributeName0, result.get(1).getName());
        assertTrue(result.get(1).isMember());
        assertEquals("value 1", result.get(1).getStringValue());
    }

    @Test
    public void compoundedAttributeScenario()
            throws Exception {

        // setup
        String login = "test-login";
        EntityManager entityManager = entityTestManager.getEntityManager();

        // operate: register the test user
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity subject = userRegistrationService.registerUser(login);

        // operate: add multivalued attribute type
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);

        String attributeName0 = "test-attribute-name-0";
        AttributeTypeEntity attributeType0 = new AttributeTypeEntity(attributeName0, DatatypeType.STRING, true, true);
        attributeType0.setMultivalued(true);
        attributeTypeService.add(attributeType0);

        String attributeName1 = "test-attribute-name-1";
        AttributeTypeEntity attributeType1 = new AttributeTypeEntity(attributeName1, DatatypeType.BOOLEAN, true, true);
        attributeType1.setMultivalued(true);
        attributeTypeService.add(attributeType1);

        refreshTransaction(entityManager);

        String compoundedAttributeName = "test-comp-attrib-name-aargh";
        AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(compoundedAttributeName, DatatypeType.COMPOUNDED, true, true);
        compoundedAttributeType.setMultivalued(true);
        compoundedAttributeType.addMember(attributeType0, 0, true);
        compoundedAttributeType.addMember(attributeType1, 1, true);
        attributeTypeService.add(compoundedAttributeType);

        // operate: add application
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String applicationOwnerName = "test-application-owner-name";
        applicationService.registerApplicationOwner(applicationOwnerName, login);
        String applicationName = "test-application";
        List<IdentityAttributeTypeDO> initialApplicationIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
        initialApplicationIdentityAttributes.add(new IdentityAttributeTypeDO(compoundedAttributeName, true, false));
        applicationService.addApplication(applicationName, null, applicationOwnerName, null, false, IdScopeType.USER, null, null, null,
                initialApplicationIdentityAttributes, false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(applicationName);

        // operate: subscribe user to application
        SubscriptionService subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, subject.getUserId(), "user");
        subscriptionService.subscribe(testApplication.getId());

        IdentityService identityService = EJBTestUtils.newInstance(IdentityServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, subject.getUserId(), SafeOnlineRoles.USER_ROLE);
        identityService.confirmIdentity(testApplication.getId());

        // operate: save attribute
        AttributeDO compoundedAttribute0 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 0, null, null, true,
                true, null, null);
        compoundedAttribute0.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute0);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 0, null, null, true, true, "value 0", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 0, null, null, true, true, null,
                Boolean.FALSE));

        AttributeDO compoundedAttribute1 = new AttributeDO(compoundedAttributeName, DatatypeType.COMPOUNDED, true, 1, null, null, true,
                true, null, null);
        compoundedAttribute1.setCompounded(true);
        identityService.saveAttribute(compoundedAttribute1);
        identityService
                       .saveAttribute(new AttributeDO(attributeName0, DatatypeType.STRING, true, 1, null, null, true, true, "value 1", null));
        identityService.saveAttribute(new AttributeDO(attributeName1, DatatypeType.BOOLEAN, true, 1, null, null, true, true, null,
                Boolean.TRUE));

        refreshTransaction(entityManager);

        // operate
        List<AttributeDO> resultMissingAttributes = identityService.listMissingAttributes(testApplication.getId(), null);

        // verify
        assertNotNull(resultMissingAttributes);
        assertTrue(resultMissingAttributes.isEmpty());

        // operate
        List<AttributeDO> resultAttributes = identityService.listAttributes(null);
        assertNotNull(resultAttributes);
        LOG.debug("result attributes: " + resultAttributes);
        assertEquals(6, resultAttributes.size());

    }

    private void refreshTransaction(EntityManager entityManager) {

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.commit();
        transaction.begin();
        /*
         * Also make sure that the existing entities are detached.
         */
        entityManager.clear();
    }
}
