/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;

import net.link.safeonline.Startable;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class SystemInitializationStartableBeanTest {

    private KeyService    mockKeyService;
    private JndiTestUtils jndiTestUtils;


    @Test
    public void testPostStart()
            throws Exception {

        // setup
        EntityTestManager entityTestManager = new EntityTestManager();
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
        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }
}
