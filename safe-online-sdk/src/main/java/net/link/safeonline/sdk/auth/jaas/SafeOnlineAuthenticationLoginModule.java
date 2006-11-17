package net.link.safeonline.sdk.auth.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.link.safeonline.sdk.auth.AuthClient;
import net.link.safeonline.sdk.auth.AuthClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JAAS Login Module using the SafeOnline Authentication service.
 * 
 * This login module can be used by J2EE application to delegate authentication
 * decisions to SafeOnline.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineAuthenticationLoginModule implements LoginModule {

	public static final String OPTION_LOCATION = "location";

	public static final String DEFAULT_LOCATION = "localhost:8080";

	private static final Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationLoginModule.class);

	private AuthClient authClient;

	private Subject subject;

	private CallbackHandler callbackHandler;

	private Map<String, Object> sharedState;

	private Principal authenticatedPrincipal;

	@SuppressWarnings("unchecked")
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {
		LOG.debug("initialize");
		String location = (String) options.get(OPTION_LOCATION);
		if (null == location) {
			location = DEFAULT_LOCATION;
			LOG.debug("using default location");
		}
		LOG.debug("location: " + location);
		this.authClient = new AuthClientImpl(location);

		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
	}

	public boolean login() throws LoginException {
		LOG.debug("login");

		NameCallback nameCallback = new NameCallback("username");
		PasswordCallback passwordCallback = new PasswordCallback("password",
				true);
		Callback[] callbacks = new Callback[] { nameCallback, passwordCallback };

		try {
			this.callbackHandler.handle(callbacks);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			LOG.error(msg);
			throw new LoginException(msg);
		} catch (UnsupportedCallbackException e) {
			String msg = "unsupported callback: " + e.getMessage();
			LOG.error(msg);
			throw new LoginException(msg);
		} catch (Exception e) {
			String msg = "error: " + e.getMessage();
			LOG.error(msg);
			throw new LoginException(msg);
		}

		String username = nameCallback.getName();
		char[] passwd = passwordCallback.getPassword();
		if (null == passwd) {
			throw new FailedLoginException("password required");
		}
		String password = new String(passwd);
		LOG.debug("username: " + username);
		LOG.debug("password: " + password);

		boolean result = this.authClient.authenticate(username, password);
		if (!result) {
			throw new FailedLoginException("not authenticated");
		}

		this.authenticatedPrincipal = new SimplePrincipal(username);

		this.sharedState.put("javax.security.auth.login.name", username);
		this.sharedState.put("javax.security.auth.login.password",
				passwordCallback.getPassword());

		return true;
	}

	public boolean commit() throws LoginException {
		LOG.debug("commit");

		Set<Principal> principals = this.subject.getPrincipals();
		if (null == this.authenticatedPrincipal) {
			throw new LoginException(
					"authenticated principal should be not null");
		}
		principals.add(this.authenticatedPrincipal);

		return true;
	}

	public boolean abort() throws LoginException {
		LOG.debug("abort");

		this.authenticatedPrincipal = null;

		return true;
	}

	public boolean logout() throws LoginException {
		LOG.debug("logout");

		Set<Principal> principals = this.subject.getPrincipals();
		if (null == this.authenticatedPrincipal) {
			throw new LoginException(
					"authenticated principal should not be null");
		}
		boolean result = principals.remove(this.authenticatedPrincipal);
		if (!result) {
			throw new LoginException("could not remove authenticated principal");
		}

		return true;
	}
}
