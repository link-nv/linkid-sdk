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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class ApplicationServiceBeanTest {

    private static final Log    LOG            = LogFactory.getLog(ApplicationServiceBeanTest.class);

    private static final String NAME_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:name";

    private EntityTestManager   entityTestManager;

    private KeyService          mockKeyService;

    private JndiTestUtils       jndiTestUtils;


    @Before
    protected void setUp()
            throws Exception {

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        systemStartable.postStart();
        entityTestManager.refreshEntityManager();
    }

    @After
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testApplicationIdentityUseCase()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();

        // operate
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        ApplicationEntity application = applicationService.getApplication(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);
        Set<ApplicationIdentityAttributeEntity> result = applicationService.getCurrentApplicationIdentity(application.getId());

        // verify
        assertTrue(result.isEmpty());

        // operate
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", "global-operator");
        AttributeTypeEntity attributeType = new AttributeTypeEntity(NAME_ATTRIBUTE, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        IdentityAttributeTypeDO[] applicationIdentityAttributes = new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                NAME_ATTRIBUTE, false, false) };
        LOG.debug("---------- UPDATING APPLICATION IDENTITY ----------");
        applicationService.updateApplicationIdentity(application.getId(), Arrays.asList(applicationIdentityAttributes));
        result = applicationService.getCurrentApplicationIdentity(application.getId());

        // verify
        assertEquals(1, result.size());
        assertEquals(NAME_ATTRIBUTE, result.iterator().next().getAttributeTypeName());
        assertFalse(result.iterator().next().isRequired());

        // operate
        applicationIdentityAttributes = new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(NAME_ATTRIBUTE, true, false) };
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        applicationService.updateApplicationIdentity(application.getId(), Arrays.asList(applicationIdentityAttributes));
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        result = applicationService.getCurrentApplicationIdentity(application.getId());

        // verify
        assertEquals(1, result.size());
        assertEquals(NAME_ATTRIBUTE, result.iterator().next().getAttributeTypeName());
        assertTrue(result.iterator().next().isRequired());
    }

    @Test
    public void testRemoveApplication()
            throws Exception {

        EntityManager entityManager = entityTestManager.getEntityManager();
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);

        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testApplicationFriendlyName = "test-application-friendly-name" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        userRegistrationService.registerUser(testAdminLogin);
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", "global-operator");
        AttributeTypeEntity attributeType = new AttributeTypeEntity(NAME_ATTRIBUTE, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        List<IdentityAttributeTypeDO> initialIdentity = new LinkedList<IdentityAttributeTypeDO>();
        initialIdentity.add(new IdentityAttributeTypeDO(NAME_ATTRIBUTE));

        applicationService.addApplication(testApplicationName, testApplicationFriendlyName, testApplicationOwnerName, null, false,
                IdScopeType.USER, null, null, null, initialIdentity, false, false, false, null);
        ApplicationEntity application = applicationService.getApplication(testApplicationName);

        ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(ApplicationOwnerDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        ApplicationOwnerEntity applicationOwner = applicationOwnerDAO.getApplicationOwner(testApplicationOwnerName);
        List<ApplicationEntity> applications = new LinkedList<ApplicationEntity>();
        applications.add(application);
        applicationOwner.setApplications(applications);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();

        applicationService.removeApplication(application.getId());

        entityManager.getTransaction().commit();
    }

    @Test
    public void testRemoveApplicationOwnerFails()
            throws Exception {

        EntityManager entityManager = entityTestManager.getEntityManager();
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        userRegistrationService.registerUser(testAdminLogin);
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", "global-operator");
        AttributeTypeEntity attributeType = new AttributeTypeEntity(NAME_ATTRIBUTE, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        List<IdentityAttributeTypeDO> initialIdentity = new LinkedList<IdentityAttributeTypeDO>();
        initialIdentity.add(new IdentityAttributeTypeDO(NAME_ATTRIBUTE));

        applicationService.addApplication(testApplicationName, null, testApplicationOwnerName, null, false, IdScopeType.USER, null, null,
                null, initialIdentity, false, false, false, null);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();

        /*
         * Add the application manually to the application owner, at runtime hibernate takes care of this transparantly
         */
        ApplicationEntity application = applicationService.getApplication(testApplicationName);
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        assertNull(applicationOwner.getApplications());
        List<ApplicationEntity> ownerApplications = new LinkedList<ApplicationEntity>();
        ownerApplications.add(application);
        applicationOwner.setApplications(ownerApplications);

        try {
            applicationService.removeApplicationOwner(testApplicationOwnerName, testAdminLogin);
        } catch (PermissionDeniedException e) {
            // should fail, it still owns an application
            return;
        }
        fail();
    }

    @Test
    public void testRemoveApplicationOwner()
            throws Exception {

        EntityManager entityManager = entityTestManager.getEntityManager();
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        userRegistrationService.registerUser(testAdminLogin);
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-admin", "global-operator");
        AttributeTypeEntity attributeType = new AttributeTypeEntity(NAME_ATTRIBUTE, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        List<IdentityAttributeTypeDO> initialIdentity = new LinkedList<IdentityAttributeTypeDO>();
        initialIdentity.add(new IdentityAttributeTypeDO(NAME_ATTRIBUTE));

        applicationService.addApplication(testApplicationName, null, testApplicationOwnerName, null, false, IdScopeType.USER, null, null,
                null, initialIdentity, false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(ApplicationOwnerDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        ApplicationOwnerEntity applicationOwner = applicationOwnerDAO.getApplicationOwner(testApplicationOwnerName);
        List<ApplicationEntity> applications = new LinkedList<ApplicationEntity>();
        applications.add(testApplication);
        applicationOwner.setApplications(applications);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();

        applicationService.removeApplication(testApplication.getId());

        applicationService.removeApplicationOwner(testApplicationOwnerName, testAdminLogin);
    }
}
