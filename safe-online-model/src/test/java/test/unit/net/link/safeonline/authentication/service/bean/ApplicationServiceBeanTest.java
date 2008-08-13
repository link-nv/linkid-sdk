/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import junit.framework.TestCase;
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
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.bean.PasswordDeviceServiceBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class ApplicationServiceBeanTest extends TestCase {

    private static final Log  LOG = LogFactory.getLog(ApplicationServiceBeanTest.class);

    private EntityTestManager entityTestManager;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");

        this.entityTestManager = new EntityTestManager();
        this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = this.entityTestManager.getEntityManager();

        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate",
                new MBeanActionHandler() {

                    public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                        return authCertificate;
                    }
                });

        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate",
                new MBeanActionHandler() {

                    public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                        return certificate;
                    }
                });

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        systemStartable.postStart();
        this.entityTestManager.refreshEntityManager();
    }

    @Override
    protected void tearDown() throws Exception {

        this.entityTestManager.tearDown();
        super.tearDown();
    }

    public void testApplicationIdentityUseCase() throws Exception {

        // setup
        EntityManager entityManager = this.entityTestManager.getEntityManager();

        // operate
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        Set<ApplicationIdentityAttributeEntity> result = applicationService
                .getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

        // verify
        assertTrue(result.isEmpty());

        // operate
        IdentityAttributeTypeDO[] applicationIdentityAttributes = new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                SafeOnlineConstants.NAME_ATTRIBUTE, false, false) };
        LOG.debug("---------- UPDATING APPLICATION IDENTITY ----------");
        applicationService.updateApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, Arrays
                .asList(applicationIdentityAttributes));
        result = applicationService
                .getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

        // verify
        assertEquals(1, result.size());
        assertEquals(SafeOnlineConstants.NAME_ATTRIBUTE, result.iterator().next().getAttributeTypeName());
        assertFalse(result.iterator().next().isRequired());

        // operate
        applicationIdentityAttributes = new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                SafeOnlineConstants.NAME_ATTRIBUTE, true, false) };
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        applicationService.updateApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, Arrays
                .asList(applicationIdentityAttributes));
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        result = applicationService
                .getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

        // verify
        assertEquals(1, result.size());
        assertEquals(SafeOnlineConstants.NAME_ATTRIBUTE, result.iterator().next().getAttributeTypeName());
        assertTrue(result.iterator().next().isRequired());
    }

    public void testRemoveApplication() throws Exception {

        EntityManager entityManager = this.entityTestManager.getEntityManager();
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);

        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testApplicationFriendlyName = "test-application-friendly-name" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        PasswordDeviceService passwordDeviceService = EJBTestUtils.newInstance(PasswordDeviceServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        SubjectEntity testAdminSubject = userRegistrationService.registerUser(testAdminLogin);
        passwordDeviceService.register(testAdminSubject, "secret");
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        List<IdentityAttributeTypeDO> initialIdentity = new LinkedList<IdentityAttributeTypeDO>();
        initialIdentity.add(new IdentityAttributeTypeDO(SafeOnlineConstants.NAME_ATTRIBUTE));

        applicationService.addApplication(testApplicationName, testApplicationFriendlyName, testApplicationOwnerName,
                null, false, IdScopeType.USER, null, null, null, null, initialIdentity, false, false);
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

        applicationService.removeApplication(testApplicationName);

        entityManager.getTransaction().commit();
    }

    public void testRemoveApplicationOwnerFails() throws Exception {

        EntityManager entityManager = this.entityTestManager.getEntityManager();
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        PasswordDeviceService passwordDeviceService = EJBTestUtils.newInstance(PasswordDeviceServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        SubjectEntity testAdminSubject = userRegistrationService.registerUser(testAdminLogin);
        passwordDeviceService.register(testAdminSubject, "secret");
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        List<IdentityAttributeTypeDO> initialIdentity = new LinkedList<IdentityAttributeTypeDO>();
        initialIdentity.add(new IdentityAttributeTypeDO(SafeOnlineConstants.NAME_ATTRIBUTE));

        applicationService.addApplication(testApplicationName, null, testApplicationOwnerName, null, false,
                IdScopeType.USER, null, null, null, null, initialIdentity, false, false);

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

    public void testRemoveApplicationOwner() throws Exception {

        EntityManager entityManager = this.entityTestManager.getEntityManager();
        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        String testApplicationName = "test-application-name-" + UUID.randomUUID().toString();
        String testApplicationOwnerName = "test-application-owner-name-" + UUID.randomUUID().toString();
        String testAdminLogin = "test-admin-login-" + UUID.randomUUID().toString();
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        PasswordDeviceService passwordDeviceService = EJBTestUtils.newInstance(PasswordDeviceServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        SubjectEntity testAdminSubject = userRegistrationService.registerUser(testAdminLogin);
        passwordDeviceService.register(testAdminSubject, "secret");
        applicationService.registerApplicationOwner(testApplicationOwnerName, testAdminLogin);

        List<IdentityAttributeTypeDO> initialIdentity = new LinkedList<IdentityAttributeTypeDO>();
        initialIdentity.add(new IdentityAttributeTypeDO(SafeOnlineConstants.NAME_ATTRIBUTE));

        applicationService.addApplication(testApplicationName, null, testApplicationOwnerName, null, false,
                IdScopeType.USER, null, null, null, null, initialIdentity, false, false);
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

        applicationService.removeApplication(testApplicationName);

        applicationService.removeApplicationOwner(testApplicationOwnerName, testAdminLogin);
    }
}
