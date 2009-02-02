/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;

import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.AccountServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class AccountServiceBeanTest {

    private EntityTestManager entityTestManager;
    private KeyService        mockKeyService;
    private JndiTestUtils     jndiTestUtils;


    @Before
    protected void setUp()
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

        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        SystemInitializationStartableBean systemInit = EJBTestUtils.newInstance(SystemInitializationStartableBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        systemInit.postStart();
        entityTestManager.refreshEntityManager();
    }

    @After
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testRemoveAccount()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();

        String testLogin = "test-login";

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        // operate
        SubjectEntity resultSubject = userRegistrationService.registerUser(testLogin);

        AccountService accountService = EJBTestUtils.newInstance(AccountServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, testLogin, SafeOnlineRoles.USER_ROLE);
        accountService.removeAccount(resultSubject.getUserId());
    }
}
