package net.link.safeonline.jaas;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.service.AuthorizationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

public class SafeOnlineLoginModule implements LoginModule {

	private static final Log LOG = LogFactory
			.getLog(SafeOnlineLoginModule.class);

	private Subject subject;

	private CallbackHandler callbackHandler;

	public static final String OPTION_AUTHENTICATION_SERVICE_JNDI_NAME = "authenticationServiceJndiName";

	public static final String DEFAULT_AUTHENTICATION_SERVICE_JNDI_NAME = "SafeOnline/AuthenticationServiceBean/local";

	private String authenticationServiceJndiName;

	public static final String OPTION_AUTHORIZATION_SERVICE_JNDI_NAME = "authorizationServiceJndiName";

	public static final String DEFAULT_AUTHORIZATION_SERVICE_JNDI_NAME = "SafeOnline/AuthorizationServiceBean/local";

	private String authorizationServiceJndiName;

	private Principal authenticatedPrincipal;

	private Set<String> roles;

	public boolean abort() throws LoginException {
		LOG.debug("abort");

		this.authenticatedPrincipal = null;
		this.roles = null;

		return true;
	}

	public boolean commit() throws LoginException {
		LOG.debug("commit");

		Set<Principal> principals = this.subject.getPrincipals();
		if (null == this.authenticatedPrincipal) {
			throw new LoginException(
					"authenticated principal should be not null");
		}
		// authenticate
		principals.add(this.authenticatedPrincipal);

		// authorize
		Group rolesGroup = getGroup("Roles", principals);

		if (null == this.roles) {
			return true;
		}

		for (String role : this.roles) {
			rolesGroup.addMember(new SimplePrincipal(role));
		}

		return true;
	}

	private String getOptionValue(Map options, String optionName,
			String defaultOptionValue) {
		String optionValue = (String) options.get(optionName);
		if (null == optionValue) {
			optionValue = defaultOptionValue;
			LOG.debug("using default option value for " + optionName + " = "
					+ defaultOptionValue);
		}
		return optionValue;
	}

	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {
		LOG.debug("initialize");

		this.authenticationServiceJndiName = getOptionValue(options,
				OPTION_AUTHENTICATION_SERVICE_JNDI_NAME,
				DEFAULT_AUTHENTICATION_SERVICE_JNDI_NAME);

		this.authorizationServiceJndiName = getOptionValue(options,
				OPTION_AUTHORIZATION_SERVICE_JNDI_NAME,
				DEFAULT_AUTHORIZATION_SERVICE_JNDI_NAME);

		this.subject = subject;
		this.callbackHandler = callbackHandler;
	}

	private Group getGroup(String groupName, Set<Principal> principals) {
		Iterator iter = principals.iterator();
		while (iter.hasNext()) {
			Object next = iter.next();
			if ((next instanceof Group) == false)
				continue;
			Group group = (Group) next;
			if (group.getName().equals(groupName)) {
				return group;
			}
		}
		// If we did not find a group create one
		Group group = new SimpleGroup(groupName);
		principals.add(group);
		return group;
	}

	public boolean login() throws LoginException {
		LOG.debug("login");
		// retrieve credentials
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
		}

		String username = nameCallback.getName();
		char[] passwd = passwordCallback.getPassword();
		if (null == passwd) {
			throw new FailedLoginException("password required");
		}
		String password = new String(passwd);
		LOG.debug("username: " + username);
		LOG.debug("password: " + password);

		// authenticate
		AuthenticationService authenticationService = getAuthenticationService();

		boolean authenticated = authenticationService.authenticate(username,
				password);
		if (!authenticated) {
			throw new LoginException("not authenticated");
		}
		LOG.debug("authenticated");

		this.authenticatedPrincipal = new SimplePrincipal(username);

		// authorize
		AuthorizationService authorizationService = getAuthorizationService();

		this.roles = authorizationService.getRoles(username);

		return true;
	}

	private AuthenticationService getAuthenticationService()
			throws LoginException {
		try {
			InitialContext initialContext = new InitialContext();
			AuthenticationService authenticationService = (AuthenticationService) initialContext
					.lookup(this.authenticationServiceJndiName);
			return authenticationService;
		} catch (NamingException e) {
			LOG.error("naming error: " + e.getMessage(), e);
			throw new LoginException("naming error: " + e.getMessage());
		}
	}

	private AuthorizationService getAuthorizationService()
			throws LoginException {
		try {
			InitialContext initialContext = new InitialContext();
			AuthorizationService authorizationService = (AuthorizationService) initialContext
					.lookup(this.authorizationServiceJndiName);
			return authorizationService;
		} catch (NamingException e) {
			LOG.error("naming error: " + e.getMessage(), e);
			throw new LoginException("naming error: " + e.getMessage());
		}
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
		 * maybe we should also remove the "Roles" group. JBoss
		 * AbstractServerLoginModule is also not doing this.
		 */
		return true;
	}
}
