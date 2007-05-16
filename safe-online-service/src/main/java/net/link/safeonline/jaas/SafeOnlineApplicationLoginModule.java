/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.jaas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

/**
 * JAAS login module that performs authentication and authorization for
 * applications. This module is used by the SafeOnline core application security
 * domain. The login module links an X509 certificate with an application
 * principal.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineApplicationLoginModule implements LoginModule {

	private static final Log LOG = LogFactory
			.getLog(SafeOnlineApplicationLoginModule.class);

	private Subject subject;

	private CallbackHandler callbackHandler;

	public static final String OPTION_AUTHENTICATION_SERVICE_JNDI_NAME = "authenticationServiceJndiName";

	public static final String DEFAULT_AUTHENTICATION_SERVICE_JNDI_NAME = "SafeOnline/ApplicationAuthenticationServiceBean/local";

	private String authenticationServiceJndiName;

	private Principal authenticatedPrincipal;

	public boolean abort() throws LoginException {
		LOG.debug("abort");

		this.authenticatedPrincipal = null;

		return true;
	}

	public boolean commit() throws LoginException {
		LOG.debug("commit: " + this);

		Set<Principal> principals = this.subject.getPrincipals();
		if (null == this.authenticatedPrincipal) {
			throw new LoginException(
					"authenticated principal should be not null");
		}
		// authenticate
		principals.add(this.authenticatedPrincipal);

		// authorize
		Group rolesGroup = getGroup("Roles", principals);
		rolesGroup.addMember(new SimplePrincipal("application"));

		LOG.debug("commit: " + this.authenticatedPrincipal.getName());

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
		LOG.debug("initialize: " + this);

		this.authenticationServiceJndiName = getOptionValue(options,
				OPTION_AUTHENTICATION_SERVICE_JNDI_NAME,
				DEFAULT_AUTHENTICATION_SERVICE_JNDI_NAME);

		this.subject = subject;
		this.callbackHandler = callbackHandler;
		LOG.debug("subject: " + subject);
		LOG.debug("callback handler type: "
				+ this.callbackHandler.getClass().getName());
	}

	private Group getGroup(String groupName, Set<Principal> principals) {
		for (Principal principal : principals) {
			if (false == principal instanceof Group) {
				continue;
			}
			Group group = (Group) principal;
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
		LOG.debug("login: " + this);
		// retrieve the certificate credential
		PasswordCallback passwordCallback = new PasswordCallback(
				"X509 application certificate in Hex", false);
		NameCallback nameCallback = new NameCallback("application name");
		Callback[] callbacks = new Callback[] { passwordCallback, nameCallback };

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

		char[] password = passwordCallback.getPassword();
		X509Certificate certificate;
		try {
			certificate = toX509Certificate(password);
		} catch (Exception e) {
			throw new LoginException("X509 decoding error: " + e.getMessage());
		}

		// authenticate
		ApplicationAuthenticationService applicationAuthenticationService = getApplicationAuthenticationService();
		String applicationName;
		try {
			applicationName = applicationAuthenticationService
					.authenticate(certificate);
		} catch (ApplicationNotFoundException e) {
			throw new FailedLoginException(
					"certificate is not an application certificate");
		}

		String expectedApplicationName = nameCallback.getName();
		if (false == applicationName.equals(expectedApplicationName)) {
			throw new FailedLoginException("application name not correct");
		}

		this.authenticatedPrincipal = new SimplePrincipal(applicationName);
		LOG.debug("login: " + applicationName);
		LOG.debug("login subject: " + this.subject);

		return true;
	}

	private ApplicationAuthenticationService getApplicationAuthenticationService()
			throws LoginException {
		try {
			ApplicationAuthenticationService applicationAuthenticationService = EjbUtils
					.getEJB(this.authenticationServiceJndiName,
							ApplicationAuthenticationService.class);
			return applicationAuthenticationService;
		} catch (RuntimeException e) {
			throw new LoginException("JNDI lookup error: " + e.getMessage());
		}
	}

	public boolean logout() throws LoginException {
		LOG.debug("logout: " + this);
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
		LOG.debug("logout: " + this.authenticatedPrincipal.getName());
		LOG.debug("logout subject: " + this.subject);
		return true;
	}

	private static X509Certificate toX509Certificate(char[] password)
			throws DecoderException, CertificateException {
		byte[] encodedCertificate = Hex.decodeHex(password);
		CertificateFactory certificateFactory = CertificateFactory
				.getInstance("X.509");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				encodedCertificate);
		X509Certificate certificate = (X509Certificate) certificateFactory
				.generateCertificate(inputStream);
		return certificate;
	}
}
