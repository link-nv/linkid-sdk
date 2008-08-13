/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.p11sc.impl;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import junitx.framework.ListAssert;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.impl.SmartCardConfigFactoryImpl;
import net.link.safeonline.test.util.TestClassLoader;


public class SmartCardConfigFactoryImplTest extends TestCase {

    private SmartCardConfigFactoryImpl testedInstance;

    private TestClassLoader            testClassLoader;

    private ClassLoader                originalClassLoader;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        Thread currentThread = Thread.currentThread();
        this.originalClassLoader = currentThread.getContextClassLoader();
        this.testClassLoader = new TestClassLoader();
        currentThread.setContextClassLoader(this.testClassLoader);

        this.testedInstance = new SmartCardConfigFactoryImpl();
    }

    @Override
    protected void tearDown() throws Exception {

        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(this.originalClassLoader);

        super.tearDown();
    }

    public void testDefaultGetSmartCardConfigsIsEmpty() throws Exception {

        // operate
        List<SmartCardConfig> results = this.testedInstance.getSmartCardConfigs();

        // verify
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    public void testGetSmartCardConfigs() throws Exception {

        // setup
        URL testConfigResource = SmartCardConfigFactoryImpl.class
                .getResource("/test-safe-online-pkcs11-sc-config.properties");

        this.testClassLoader.addResource("META-INF/safe-online-pkcs11-sc-config.properties", testConfigResource);

        // operate
        List<SmartCardConfig> results = this.testedInstance.getSmartCardConfigs();

        // verify
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        SmartCardConfig resultConfig = results.get(0);
        assertEquals("test-alias", resultConfig.getCardAlias());

        assertEquals("test-auth-alias", resultConfig.getAuthenticationKeyAlias());
        assertEquals("test-sign-alias", resultConfig.getSignatureKeyAlias());

        List<File> resultDriverLocations = resultConfig.getPkcs11DriverLocations("test-platform");
        assertNotNull(resultDriverLocations);
        assertEquals(2, resultDriverLocations.size());

        List<File> expectedDriverLocations = new LinkedList<File>();
        expectedDriverLocations.add(new File("/test/location"));
        expectedDriverLocations.add(new File("C:\\another\\test\\location"));
        ListAssert.assertEquals(expectedDriverLocations, resultDriverLocations);

        assertEquals("test.net.link.safeonline.IdentityExtractor", resultConfig.getIdentityExtractorClassname());
    }
}
