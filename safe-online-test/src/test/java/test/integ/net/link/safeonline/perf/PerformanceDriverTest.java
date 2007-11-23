/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.integ.net.link.safeonline.perf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.List;
import java.util.Map;

import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;

import org.junit.Before;
import org.junit.Test;

/**
 * @author mbillemo
 * 
 */
public class PerformanceDriverTest {

	private static final PrivateKeyEntry APPL = PerformanceKeyStoreUtils
			.getPrivateKeyEntry();
	// private static final String OLAS_HOSTNAME = "sebeco-dev-10:8443";
	private static final String OLAS_HOSTNAME = "localhost:8443";
	private static final String PASS = "admin";
	private static final String USER = "admin";
	private AttribDriver attribDriver;
	private AuthDriver authDriver;
	private IdMappingDriver idDriver;

	@Before
	public void setUp() {

		this.idDriver = new IdMappingDriver(OLAS_HOSTNAME);
		this.attribDriver = new AttribDriver(OLAS_HOSTNAME);
		this.authDriver = new AuthDriver(OLAS_HOSTNAME);
	}

	@Test
	public void testAttrib() throws Exception {

		// User needs to authenticate before we can get to the attributes.
		String uuid = this.authDriver.login(APPL, "performance-application",
				USER, PASS);

		getAttributes(APPL, uuid);
	}

	@Test
	public void testLogin() throws Exception {

		login(USER, PASS);
	}

	@Test
	public void testMapping() throws Exception {

		getUserId(APPL, USER);
	}

	private Map<String, Object> getAttributes(PrivateKeyEntry application,
			String uuid) throws Exception {

		// Get attributes for given UUID.
		Map<String, Object> attributes = this.attribDriver.getAttributes(
				application, uuid);

		// State assertions.
		assertNotNull(attributes);
		assertFalse(attributes.isEmpty());
		assertFalse(isEmptyOrOnlyNulls(this.attribDriver.getProfileData()));
		assertTrue(isEmptyOrOnlyNulls(this.attribDriver.getProfileError()));
		return attributes;

	}

	/**
	 * Get the UUID of the given username for the given application.
	 */
	private String getUserId(PrivateKeyEntry application, String username)
			throws Exception {

		String uuid = this.idDriver.getUserId(application, username);

		// State assertions.
		assertNotNull(uuid);
		assertNotSame("", uuid);
		assertFalse(isEmptyOrOnlyNulls(this.idDriver.getProfileData()));
		assertTrue(isEmptyOrOnlyNulls(this.idDriver.getProfileError()));

		return uuid;
	}

	private boolean isEmptyOrOnlyNulls(List<?> profileData) {

		if (profileData == null || profileData.isEmpty())
			return true;

		for (Object data : profileData)
			if (null != data)
				return false;

		return true;
	}

	private String login(String username, String password) throws Exception {

		// Authenticate User.
		String uuid = this.authDriver.login(APPL, "performance-application",
				username, password);

		// State assertions.
		assertNotNull(uuid);
		assertNotSame("", uuid);
		assertFalse(isEmptyOrOnlyNulls(this.authDriver.getProfileData()));
		assertTrue(isEmptyOrOnlyNulls(this.authDriver.getProfileError()));

		return uuid;

	}
}
