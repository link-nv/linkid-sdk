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

import javax.smartcardio.ATR;

import junit.framework.TestCase;
import junitx.framework.ListAssert;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.impl.XmlSmartCardConfigFactory;
import net.link.safeonline.test.util.TestClassLoader;

public class XmlSmartCardConfigFactoryTest extends TestCase {

	private XmlSmartCardConfigFactory testedInstance;

	private TestClassLoader testClassLoader;

	private ClassLoader originalClassLoader;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Thread currentThread = Thread.currentThread();
		this.originalClassLoader = currentThread.getContextClassLoader();
		this.testClassLoader = new TestClassLoader();
		currentThread.setContextClassLoader(this.testClassLoader);

		this.testedInstance = new XmlSmartCardConfigFactory();
	}

	@Override
	protected void tearDown() throws Exception {
		Thread currentThread = Thread.currentThread();
		currentThread.setContextClassLoader(this.originalClassLoader);

		super.tearDown();
	}

	public void testDefaultGetSmartCardConfigsIsEmpty() throws Exception {
		// operate
		List<SmartCardConfig> results = this.testedInstance
				.getSmartCardConfigs();

		// verify
		assertNotNull(results);
		assertTrue(results.isEmpty());
	}

	public void testGetSmartCardConfigs() throws Exception {
		// setup
		URL testConfigResource = XmlSmartCardConfigFactory.class
				.getResource("/test-safe-online-pkcs11-sc-config.xml");

		this.testClassLoader
				.addResource("META-INF/safe-online-pkcs11-sc-config.xml",
						testConfigResource);

		// operate
		List<SmartCardConfig> results = this.testedInstance
				.getSmartCardConfigs();

		// verify
		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertEquals(1, results.size());
		SmartCardConfig resultConfig = results.get(0);
		assertEquals("test-alias", resultConfig.getCardAlias());

		assertTrue(resultConfig.isSupportedATR(new ATR(new byte[] { 0x01, 0x02,
				0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C,
				0x0D })));
		assertTrue(resultConfig.isSupportedATR(new ATR(
				new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD,
						(byte) 0xFC, (byte) 0xFB, (byte) 0xFA, (byte) 0xF9,
						(byte) 0xF8, (byte) 0xF7, (byte) 0xF6, (byte) 0xF5,
						(byte) 0xF4, (byte) 0xF3 })));
		assertFalse(resultConfig.isSupportedATR(new ATR(new byte[] {
				(byte) 0xbabe, (byte) 0x2bad })));

		assertEquals("test-auth-alias", resultConfig
				.getAuthenticationKeyAlias());
		assertEquals("test-sign-alias", resultConfig.getSignatureKeyAlias());

		List<File> resultDriverLocations = resultConfig
				.getPkcs11DriverLocations("test-platform");
		assertNotNull(resultDriverLocations);
		assertEquals(2, resultDriverLocations.size());

		List<File> expectedDriverLocations = new LinkedList<File>();
		expectedDriverLocations.add(new File("/test/location"));
		expectedDriverLocations.add(new File("/another/test/location"));
		ListAssert.assertEquals(expectedDriverLocations, resultDriverLocations);

		assertEquals("test.net.link.safeonline.IdentityExtractor", resultConfig
				.getIdentityExtractorClassname());
	}
}
