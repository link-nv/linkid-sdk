/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;

import net.link.safeonline.keystore.AbstractServiceBasedKeyStore;
import net.link.safeonline.keystore.entity.KeyConfig;
import net.link.safeonline.keystore.entity.Type;
import net.link.safeonline.keystore.service.bean.KeyServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestClassLoader;

import org.junit.Before;
import org.junit.Test;


/**
 * <h2>{@link KeyServiceTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 30, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class KeyServiceTest {

    private KeyServiceBean  keyService;
    private TestClassLoader testClassLoader;


    class TestKeyStore extends AbstractServiceBasedKeyStore {

    }


    @Before
    public void setUp()
            throws Exception {

        EntityTestManager entityTestManager = new EntityTestManager();
        entityTestManager.setUp(KeyConfig.class);
        EntityManager entityManager = entityTestManager.getEntityManager();

        keyService = new KeyServiceBean();
        EJBTestUtils.inject(keyService, entityManager);

        testClassLoader = new TestClassLoader();
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }

    @Test
    public void testKeyConfig()
            throws Exception {

        // Givens
        String keyStorePassword = "test";
        String keyEntryPassword = "test";
        KeyPair testKeyPair = PkiTestUtils.generateKeyPair();
        PrivateKey testPrivateKey = testKeyPair.getPrivate();
        X509Certificate testCertificate = PkiTestUtils.generateSelfSignedCertificate(testKeyPair, "CN=Test");

        // Setup
        File keyStoreFile = File.createTempFile("safe-online-test-keystore-", ".jks");
        keyStoreFile.deleteOnExit();

        PkiTestUtils.persistInJKSKeyStore(keyStoreFile, testPrivateKey, testCertificate, keyStorePassword, keyEntryPassword);

        String resourceName = "safe-online-test-keystore.jks";
        testClassLoader.addResource(resourceName, keyStoreFile.toURI().toURL());

        // Test
        Type testType = Type.JKS;
        String testConfig = "test:test:" + resourceName;
        keyService.configure(TestKeyStore.class, testType, testConfig);

        Type sampleType = keyService.getType(TestKeyStore.class);
        assertEquals(String.format("Keystore type mismatch:\n\texpected: %s\n\tgot     : %s", testType, sampleType) //
                , testType, sampleType);

        String sampleConfig = keyService.getConfig(TestKeyStore.class);
        assertEquals(String.format("Keystore config mismatch:\n\texpected: %s\n\tgot     : %s", testConfig, sampleConfig) //
                , testConfig, sampleConfig);

        PrivateKeyEntry samplePrivateKeyEntry = keyService.getPrivateKeyEntry(TestKeyStore.class);
        samplePrivateKeyEntry.getCertificate().verify(testKeyPair.getPublic());
        testCertificate.verify(samplePrivateKeyEntry.getCertificate().getPublicKey());
        assertEquals(samplePrivateKeyEntry.getPrivateKey(), testKeyPair.getPrivate());
    }
}
