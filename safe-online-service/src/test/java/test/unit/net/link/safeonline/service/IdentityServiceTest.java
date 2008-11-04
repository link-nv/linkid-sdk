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

import net.link.safeonline.service.IdentityService;
import net.link.safeonline.service.IdentityServiceMBean;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestClassLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class IdentityServiceTest {

    private TestClassLoader testClassLoader;

    private ClassLoader     origClassLoader;


    @Before
    public void setUp()
            throws Exception {

        Thread currentThread = Thread.currentThread();
        this.origClassLoader = currentThread.getContextClassLoader();

        this.testClassLoader = new TestClassLoader();
        currentThread.setContextClassLoader(this.testClassLoader);
    }

    @After
    public void tearDown()
            throws Exception {

        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(this.origClassLoader);
    }

    @Test
    public void testGetPrivateKeyFromResource()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        File tmpPkcs12KeyStore = File.createTempFile("test-keystore-", ".p12");
        tmpPkcs12KeyStore.deleteOnExit();
        PrivateKey privateKey = keyPair.getPrivate();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        String password = "secret";
        PkiTestUtils.persistKey(tmpPkcs12KeyStore, privateKey, certificate, password, password);
        String resourceName = "test-keystore-resource-name";
        this.testClassLoader.addResource(resourceName, tmpPkcs12KeyStore.toURI().toURL());

        // operate
        IdentityServiceMBean testedInstance = new IdentityService();
        testedInstance.setKeyStoreResource(resourceName);
        testedInstance.setKeyStorePassword(password);
        testedInstance.setKeyStoreType("pkcs12");
        testedInstance.loadKeyPair();
        PrivateKey resultPrivateKey = testedInstance.getPrivateKey();

        // verify
        assertEquals(privateKey, resultPrivateKey);
    }

    @Test
    public void testGetPrivateKeyFromFile()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        File tmpPkcs12KeyStore = File.createTempFile("test-keystore-", ".p12");
        tmpPkcs12KeyStore.deleteOnExit();
        PrivateKey privateKey = keyPair.getPrivate();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        String password = "secret";
        PkiTestUtils.persistKey(tmpPkcs12KeyStore, privateKey, certificate, password, password);

        // operate
        IdentityServiceMBean testedInstance = new IdentityService();
        testedInstance.setKeyStoreFile(tmpPkcs12KeyStore.getAbsolutePath());
        testedInstance.setKeyStorePassword(password);
        testedInstance.setKeyStoreType("pkcs12");
        testedInstance.loadKeyPair();
        PrivateKey resultPrivateKey = testedInstance.getPrivateKey();

        // verify
        assertEquals(privateKey, resultPrivateKey);
    }
}
