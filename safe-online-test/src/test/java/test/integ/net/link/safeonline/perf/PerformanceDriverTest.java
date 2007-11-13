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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.List;
import java.util.Map;

import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;

import org.junit.Before;
import org.junit.Test;

/**
 * @author mbillemo
 * 
 */
public class PerformanceDriverTest {

	private static final String OLAS_HOSTNAME = "localhost:8443";
	private PrivateKeyEntry application;
	private AttribDriver attribDriver;
	private AuthDriver authDriver;
	private IdMappingDriver idDriver;

	@Before
	public void setUp() {

		this.application = DemoLawyerKeyStoreUtils.getPrivateKeyEntry();
		this.idDriver = new IdMappingDriver(OLAS_HOSTNAME);
		this.attribDriver = new AttribDriver(OLAS_HOSTNAME);
		this.authDriver = new AuthDriver(OLAS_HOSTNAME);
	}

	@Test
	public void testDrivers() throws Exception {

		String username = "admin", password = "admin";

		String loginUuid = login(username, password);
		String idmappingUuid = getUserId(username);
		assertSame(loginUuid, idmappingUuid);

		getAttributes(loginUuid);
	}

	private Map<String, Object> getAttributes(String uuid) throws Exception {

		// Map the User 'admin' to its UUID.
		Map<String, Object> attributes = this.attribDriver.getAttributes(
				this.application, uuid);

		// State assertions.
		assertNotNull(attributes);
		assertFalse(attributes.isEmpty());
		assertFalse(isEmptyOrOnlyNulls(this.idDriver.getProfileData()));
		assertTrue(isEmptyOrOnlyNulls(this.idDriver.getProfileError()));
		return attributes;

	}

	private String getUserId(String username) throws Exception {

		// Map the User 'admin' to its UUID.
		String uuid = this.idDriver.getUserId(DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry(), username);

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
		String uuid = this.authDriver.login(username, password);

		// State assertions.
		assertNotNull(uuid);
		assertNotSame("", uuid);
		assertFalse(isEmptyOrOnlyNulls(this.authDriver.getProfileData()));
		assertTrue(isEmptyOrOnlyNulls(this.authDriver.getProfileError()));

		return uuid;

	}
}
