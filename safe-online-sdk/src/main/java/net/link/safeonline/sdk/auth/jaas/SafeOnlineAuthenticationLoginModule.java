/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

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

import net.link.safeonline.sdk.ws.auth.AuthClient;
import net.link.safeonline.sdk.ws.auth.AuthClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JAAS Login Module using the SafeOnline Authentication service.
 * 
 * <p>
 * This login module can be used by J2EE application to delegate authentication
 * decisions to SafeOnline. This login module is using the SafeOnline
 * authentication web service.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineAuthenticationLoginModule implements LoginModule {

	public static final String OPTION_LOCATION = "location";

	public static final String DEFAULT_LOCATION = "localhost:8080";

	public static final String OPTION_APPLICATION_NAME = "application-name";

	public static final String DEFAULT_APPLICATION_NAME = "demo-application";

	private static final Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationLoginModule.class);

	private AuthClient authClient;

	private Subject subject;

	private CallbackHandler callbackHandler;

	private Map<String, Object> sharedState;

	private Principal authenticatedPrincipal;

	private String applicationName;

	private String getOptionValue(Map<?, ?> options, String optionName,
			String defaultOptionValue) {
		String optionValue = (String) options.get(optionName);
		if (null == optionValue) {
			optionValue = defaultOptionValue;
			LOG.debug("using default option value for " + optionName + " = "
					+ defaultOptionValue);
		}
		return optionValue;
	}

	@SuppressWarnings("unchecked")
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {
		LOG.debug("initialize");
		String location = getOptionValue(options, OPTION_LOCATION,
				DEFAULT_LOCATION);
		LOG.debug("location: " + location);
		try {
			this.authClient = new AuthClientImpl(location);
		} catch (Exception e) {
			LOG.error("auth client error: " + e.getMessage(), e);
		}
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;

		this.applicationName = getOptionValue(options, OPTION_APPLICATION_NAME,
				DEFAULT_APPLICATION_NAME);
		LOG.debug("application name: " + applicationName);
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

		boolean result;
		try {
			result = this.authClient.authenticate(this.applicationName,
					username, password);
		} catch (Exception e) {
			String msg = "error invoking authentication service: "
					+ e.getMessage();
			LOG.error(msg, e);
			throw new FailedLoginException(msg);
		}
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

		/*
		 * Despite the fact that JBoss AbstractServerLoginModule is not removing
		 * the roles on the subject, we clear here all data on the subject.
		 */
		this.subject.getPrincipals().clear();
		this.subject.getPublicCredentials().clear();
		this.subject.getPrivateCredentials().clear();

		return true;
	}
}
