/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;

import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.entity.Type;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.keystore.service.bean.KeyServiceBean;
import net.link.safeonline.service.KeyConfig;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestClassLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class KeyConfigTest {

    private TestClassLoader   testClassLoader;

    private ClassLoader       origClassLoader;

    private JndiTestUtils     jndiTestUtils;

    private EntityTestManager testEntityManager;

    private EntityManager     em;


    @Before
    public void setUp()
            throws Exception {

        KeyServiceBean keyServiceBean = new KeyServiceBean();

        testEntityManager = new EntityTestManager();
        testEntityManager.setUp(net.link.safeonline.keystore.entity.KeyConfig.class);
        em = testEntityManager.getEntityManager();
        EJBTestUtils.inject(keyServiceBean, em);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, keyServiceBean);

        Thread currentThread = Thread.currentThread();
        origClassLoader = currentThread.getContextClassLoader();

        testClassLoader = new TestClassLoader();
        currentThread.setContextClassLoader(testClassLoader);
    }

    @After
    public void tearDown()
            throws Exception {

        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(origClassLoader);

        jndiTestUtils.tearDown();
    }

    @Test
    public void testConfigureJKS()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        File tmpPkcs12KeyStore = File.createTempFile("test-keystore-", ".jks");
        tmpPkcs12KeyStore.deleteOnExit();
        PrivateKey privateKey = keyPair.getPrivate();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        String password = "secret";
        PkiTestUtils.persistInJKSKeyStore(tmpPkcs12KeyStore, privateKey, certificate, password, password);
        String resourceName = "test-keystore-resource-name";
        testClassLoader.addResource(resourceName, tmpPkcs12KeyStore.toURI().toURL());

        // operate
        KeyConfig keyConfig = new KeyConfig();
        keyConfig.configure(SafeOnlineNodeKeyStore.class.getCanonicalName(), Type.JKS, String.format("%s:%s:%s", password, password,
                resourceName));
        PrivateKey resultPrivateKey = keyConfig.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class.getCanonicalName()).getPrivateKey();

        // verify
        assertEquals(privateKey, resultPrivateKey);
    }
}
