/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.bean.AccountServiceBean;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.bean.AuthorizationManagerServiceBean;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;

import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class SystemInitializationStartableBeanTest {

    private KeyService    mockKeyService;
    private JndiTestUtils jndiTestUtils;
    private JmxTestUtils  jmxTestUtils;


    @Test
    public void testPostStart()
            throws Exception {

        // setup
        EntityTestManager entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(4);

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        SafeOnlineTestConfig.loadTestNode(new URL("http://127.0.0.1/"));
        Startable testedInstance = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);

        // operate
        testedInstance.postStart();
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();

        /*
         * We run postStart twice since the system must be capable of rebooting using an persistent database.
         */
        testedInstance.postStart();

        verify(mockKeyService);

        // verify admin and owner user
        SubjectServiceBean subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity adminSubject = subjectService.findSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN);
        SubjectEntity ownerSubject = subjectService.findSubjectFromUserName(SafeOnlineConstants.OWNER_LOGIN);
        assertNotNull(adminSubject);
        assertNotNull(ownerSubject);

        // verify core applications
        ApplicationServiceBean applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, SafeOnlineConstants.ADMIN_LOGIN, SafeOnlineRoles.OPERATOR_ROLE);
        ApplicationEntity helpdeskApplication = applicationService
                                                                  .getApplication(SafeOnlineConstants.SAFE_ONLINE_HELPDESK_APPLICATION_NAME);
        ApplicationEntity operatorApplication = applicationService
                                                                  .getApplication(SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME);
        ApplicationEntity userApplication = applicationService.getApplication(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);
        ApplicationEntity ownerApplication = applicationService.getApplication(SafeOnlineConstants.SAFE_ONLINE_OWNER_APPLICATION_NAME);

        // verify admin and owner subscriptions
        SubscriptionServiceBean subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, adminSubject.getUserId(), SafeOnlineRoles.USER_ROLE);
        assertTrue(subscriptionService.isSubscribed(operatorApplication.getId()));
        assertTrue(subscriptionService.isSubscribed(helpdeskApplication.getId()));
        assertTrue(subscriptionService.isSubscribed(userApplication.getId()));

        subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class, SafeOnlineTestContainer.sessionBeans, entityManager,
                ownerSubject.getUserId(), SafeOnlineRoles.USER_ROLE);
        assertTrue(subscriptionService.isSubscribed(ownerApplication.getId()));
        assertTrue(subscriptionService.isSubscribed(userApplication.getId()));

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testPostStartNewAdmin()
            throws Exception {

        // setup
        String newAdminLogin = "new-admin";

        EntityTestManager entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(4);

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        SafeOnlineTestConfig.loadTestNode(new URL("http://127.0.0.1/"));
        Startable testedInstance = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);

        // operate
        testedInstance.postStart();
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();

        // verify admin and owner user
        SubjectServiceBean subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity adminSubject = subjectService.findSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN);
        SubjectEntity ownerSubject = subjectService.findSubjectFromUserName(SafeOnlineConstants.OWNER_LOGIN);
        assertNotNull(adminSubject);
        assertNotNull(ownerSubject);

        // operate : add new admin
        subjectService.addSubject(newAdminLogin);

        AuthorizationManagerServiceBean authorizationManagerService = EJBTestUtils.newInstance(AuthorizationManagerServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, adminSubject.getUserId(), SafeOnlineRoles.OPERATOR_ROLE);
        authorizationManagerService.setRoles(newAdminLogin, Collections.singleton(SafeOnlineRoles.OPERATOR_ROLE));

        // operate : remove current admin
        AccountServiceBean accountService = EJBTestUtils.newInstance(AccountServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, adminSubject.getUserId(), SafeOnlineRoles.USER_ROLE);
        accountService.removeAccount();

        refreshTransaction(entityManager);

        /*
         * We run postStart twice since the system must be capable of rebooting using an persistent database.
         */
        testedInstance = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        testedInstance.postStart();
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();

        verify(mockKeyService);

        // verify new admin
        subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity newAdminSubject = subjectService.findSubjectFromUserName(newAdminLogin);
        assertNotNull(newAdminSubject);

        // verify default admin not re-added
        adminSubject = subjectService.findSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN);
        assertNull(adminSubject);

        // verify core applications
        ApplicationServiceBean applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, newAdminLogin, SafeOnlineRoles.OPERATOR_ROLE);
        ApplicationEntity operatorApplication = applicationService
                                                                  .getApplication(SafeOnlineConstants.SAFE_ONLINE_OPERATOR_APPLICATION_NAME);

        // verify admin and owner subscriptions
        SubscriptionServiceBean subscriptionService = EJBTestUtils.newInstance(SubscriptionServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, newAdminSubject.getUserId(), SafeOnlineRoles.USER_ROLE);
        assertTrue(subscriptionService.isSubscribed(operatorApplication.getId()));

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
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
