/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.jaas;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import net.link.safeonline.sdk.auth.jaas.SafeOnlineAuthenticationLoginModule;
import net.link.safeonline.sdk.ws.auth.AuthClient;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class SafeOnlineAuthenticationLoginModuleTest {

	private SafeOnlineAuthenticationLoginModule testedInstance;

	@Before
	public void setUp() throws Exception {
		this.testedInstance = new SafeOnlineAuthenticationLoginModule();
	}

	private static class TestCallbackHandler implements CallbackHandler {

		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
			for (Callback callback : callbacks) {
				if (callback instanceof NameCallback) {
					NameCallback nameCallback = (NameCallback) callback;
					nameCallback.setName("test-name");
					continue;
				}
				if (callback instanceof PasswordCallback) {
					PasswordCallback passwordCallback = (PasswordCallback) callback;
					passwordCallback.setPassword("test-password".toCharArray());
					continue;
				}
			}
		}
	}

	@Test
	public void login() throws Exception {
		// setup
		CallbackHandler testCallbackHandler = new TestCallbackHandler();
		Map sharedState = new HashMap();
		Subject subject = new Subject();
		Map<String, String> options = new HashMap<String, String>();
		options.put("application-name", "test-application");
		AuthClient mockAuthClient = EasyMock.createMock(AuthClient.class);

		// expectations
		EasyMock.expect(
				mockAuthClient.authenticate("test-application", "test-name",
						"test-password")).andReturn(true);

		// prepare
		EasyMock.replay(mockAuthClient);

		// operate & verify
		this.testedInstance.initialize(subject, testCallbackHandler,
				sharedState, options);

		Field authClientField = SafeOnlineAuthenticationLoginModule.class
				.getDeclaredField("authClient");
		authClientField.setAccessible(true);
		authClientField.set(this.testedInstance, mockAuthClient);

		boolean loginResult = this.testedInstance.login();
		assertTrue(loginResult);

		boolean commitResult = this.testedInstance.commit();
		assertTrue(commitResult);

		boolean logoutResult = this.testedInstance.logout();
		assertTrue(logoutResult);

		EasyMock.verify(mockAuthClient);
	}
}
