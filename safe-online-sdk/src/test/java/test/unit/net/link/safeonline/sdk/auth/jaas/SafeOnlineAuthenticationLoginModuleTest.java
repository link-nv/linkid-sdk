/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.jaas;

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

import junit.framework.TestCase;
import net.link.safeonline.sdk.auth.AuthClient;
import net.link.safeonline.sdk.auth.jaas.SafeOnlineAuthenticationLoginModule;

import org.easymock.EasyMock;

public class SafeOnlineAuthenticationLoginModuleTest extends TestCase {

	private SafeOnlineAuthenticationLoginModule testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

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

	public void testLogin() throws Exception {
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
