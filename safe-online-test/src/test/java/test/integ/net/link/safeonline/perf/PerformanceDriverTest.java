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

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import net.link.safeonline.model.performance.PerformanceService;
import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mbillemo
 * 
 */
public class PerformanceDriverTest {

	static final Log LOG = LogFactory.getLog(PerformanceDriverTest.class);

	private static final String OLAS_HOSTNAME = "sebeco-dev-10:8443";
	// private static final String OLAS_HOSTNAME = "localhost:8443";

	private static final String username = "performance";
	private static final String password = "performance";
	private static PrivateKeyEntry applicationKey;

	static {

		Hashtable<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "jnp://" + OLAS_HOSTNAME
				+ ":1099");
		try {
			PerformanceService service = (PerformanceService) new InitialContext(
					environment).lookup(PerformanceService.JNDI_BINDING_NAME);
			applicationKey = new KeyStore.PrivateKeyEntry(service
					.getPrivateKey(), new Certificate[] { service
					.getCertificate() });
		} catch (Exception e) {
			Log.error("application keys unavailable; will try local keystore.",
					e);
			applicationKey = PerformanceKeyStoreUtils.getPrivateKeyEntry();
		}
	}

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
		String uuid = this.authDriver.login(applicationKey,
				"performance-application", username, password);

		getAttributes(applicationKey, uuid);
	}

	@Test
	public void testLogin() throws Exception {

		login(username, password);
	}

	@Test
	public void testMapping() throws Exception {

		getUserId(applicationKey, username);
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
		String uuid = this.authDriver.login(applicationKey,
				"performance-application", username, password);

		// State assertions.
		assertNotNull(uuid);
		assertNotSame("", uuid);
		assertFalse(isEmptyOrOnlyNulls(this.authDriver.getProfileData()));
		assertTrue(isEmptyOrOnlyNulls(this.authDriver.getProfileError()));

		return uuid;

	}
}
