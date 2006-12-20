/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.auth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.sdk.auth.AuthClient;
import net.link.safeonline.sdk.auth.AuthClientImpl;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 * Integration test for the SafeOnline authentication web service.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(AuthenticationTest.class);

	private static final String SAFE_ONLINE_LOCATION = "localhost:8080";

	private AuthClient authClient;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.authClient = new AuthClientImpl(SAFE_ONLINE_LOCATION);
	}

	public void testAvailabilityViaEcho() throws Exception {
		// setup
		String message = "hello world";

		// operate
		String result = this.authClient.echo(message);

		// verify
		assertEquals(message, result);
	}

	public void testAuthenticateFcorneli() throws Exception {
		// setup
		String application = "demo-application";
		String username = "fcorneli";
		String password = "secret";

		// operate
		boolean result = this.authClient.authenticate(application, username,
				password);

		// verify
		assertTrue(result);
	}

	public void testFoobarNotAuthenticated() throws Exception {
		// setup
		String application = "demo-application";
		String username = "foobar";
		String password = "foobar";

		// operate
		boolean result = this.authClient.authenticate(application, username,
				password);

		// verify
		assertFalse(result);
	}

	public void testFcorneliNotAuthenticatedForFoobarApplication()
			throws Exception {
		// setup
		String application = "foobar";
		String username = "fcorneli";
		String password = "secret";

		// operate
		boolean result = this.authClient.authenticate(application, username,
				password);

		// verify
		assertFalse(result);
	}

	public void testAuthenticationOverRMI() throws Exception {
		InitialContext initialContext = getInitialContext();

		AuthenticationService authenticationService = getAuthenticationService(initialContext);

		boolean result = authenticationService.authenticate("fcorneli",
				"secret");
		assertTrue(result);
	}

	private AuthenticationService getAuthenticationService(
			InitialContext initialContext) {
		AuthenticationService authenticationService = EjbUtils.getEJB(
				initialContext, "SafeOnline/AuthenticationServiceBean/remote",
				AuthenticationService.class);
		return authenticationService;
	}

	private void setupLoginConfig() throws Exception {
		File tmpConfigFile = File.createTempFile("jaas-", ".conf");
		tmpConfigFile.deleteOnExit();
		PrintWriter configWriter = new PrintWriter(new FileOutputStream(
				tmpConfigFile), true);
		configWriter.println("client-login {");
		configWriter.println("org.jboss.security.ClientLoginModule required");
		configWriter.println(";");
		configWriter.println("};");
		configWriter.close();
		System.setProperty("java.security.auth.login.config", tmpConfigFile
				.getAbsolutePath());
	}

	@SuppressWarnings("unchecked")
	private InitialContext getInitialContext() throws Exception {
		Hashtable environment = new Hashtable();
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "localhost:1099");
		InitialContext initialContext = new InitialContext(environment);
		return initialContext;
	}

	private Subject login(String username, String password) throws Exception {
		LoginContext loginContext = new LoginContext("client-login",
				new UsernamePasswordHandler(username, password));
		loginContext.login();
		Subject subject = loginContext.getSubject();
		return subject;
	}

	public void testAddApplication() throws Exception {

		InitialContext initialContext = getInitialContext();

		setupLoginConfig();

		final ApplicationService applicationService = getApplicationService(initialContext);

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		Subject subject = login("admin", "admin");

		final String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, login);

		Subject.doAs(subject, new PrivilegedExceptionAction() {
			public Object run() throws Exception {
				String applicationName = "application-"
						+ UUID.randomUUID().toString();
				applicationService.addApplication(applicationName,
						appOwnerName, null);
				return null;
			}
		});
	}

	private UserRegistrationService getUserRegistrationService(
			InitialContext initialContext) {
		final UserRegistrationService userRegistrationService = EjbUtils
				.getEJB(initialContext,
						"SafeOnline/UserRegistrationServiceBean/remote",
						UserRegistrationService.class);
		return userRegistrationService;
	}

	private ApplicationService getApplicationService(
			InitialContext initialContext) {
		final ApplicationService applicationService = EjbUtils.getEJB(
				initialContext, "SafeOnline/ApplicationServiceBean/remote",
				ApplicationService.class);
		return applicationService;
	}

	public void testBigUseCase() throws Exception {
		InitialContext initialContext = getInitialContext();

		setupLoginConfig();

		final ApplicationService applicationService = getApplicationService(initialContext);

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		String ownerLogin = "login-" + UUID.randomUUID().toString();
		String ownerPassword = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(ownerLogin, ownerPassword, null);

		login("admin", "admin");

		final String applicationName = "application-"
				+ UUID.randomUUID().toString();

		final String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, ownerLogin);

		applicationService.addApplication(applicationName, appOwnerName, null);

		String userLogin = "login-" + UUID.randomUUID().toString();
		final String userPassword = "secret";

		userRegistrationService.registerUser(userLogin, userPassword, null);

		final String userName = "name-" + UUID.randomUUID().toString();

		final IdentityService identityService = EjbUtils.getEJB(initialContext,
				"SafeOnline/IdentityServiceBean/remote", IdentityService.class);

		login(userLogin, userPassword);

		identityService.saveName(userName);
		String resultName = identityService.getName();
		assertEquals(userName, resultName);

		final CredentialService credentialService = getCredentialService(initialContext);

		final String newPassword = "secret-" + UUID.randomUUID().toString();

		credentialService.changePassword(userPassword, newPassword);

		login(userLogin, newPassword);
		resultName = identityService.getName();
		assertEquals(userName, resultName);

		final SubscriptionService subscriptionService = getSubscriptionService(initialContext);

		// JAAS caches the credentials...
		List<SubscriptionEntity> subscriptions = subscriptionService
				.getSubscriptions();
		for (SubscriptionEntity subscription : subscriptions) {
			LOG.debug("subscription: " + subscription);
		}
		assertEquals(1, subscriptions.size());

		subscriptionService.subscribe(applicationName);

		subscriptions = subscriptionService.getSubscriptions();
		assertEquals(2, subscriptions.size());
		for (SubscriptionEntity subscription : subscriptions) {
			LOG.debug("subscription: " + subscription);
		}
	}

	private SubscriptionService getSubscriptionService(
			InitialContext initialContext) {
		final SubscriptionService subscriptionService = EjbUtils.getEJB(
				initialContext, "SafeOnline/SubscriptionServiceBean/remote",
				SubscriptionService.class);
		return subscriptionService;
	}

	private CredentialService getCredentialService(InitialContext initialContext) {
		final CredentialService credentialService = EjbUtils.getEJB(
				initialContext, "SafeOnline/CredentialServiceBean/remote",
				CredentialService.class);
		return credentialService;
	}

	public void testCreateApplicationOwner() throws Exception {
		InitialContext initialContext = getInitialContext();

		setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		ApplicationService applicationService = getApplicationService(initialContext);

		login("admin", "admin");
		String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, login);

		String applicationName = "application-" + UUID.randomUUID().toString();
		applicationService.addApplication(applicationName, appOwnerName, null);

		login(login, password);
		applicationService.setApplicationDescription(applicationName,
				"test application description");

		login("admin", "admin");
		try {
			applicationService.registerApplicationOwner(appOwnerName, login);
			fail();
		} catch (ExistingApplicationOwnerException e) {
			// expected
		}
	}

	public void testChangingApplicationDescriptionTriggersOwnershipCheck()
			throws Exception {
		// setup
		InitialContext initialContext = getInitialContext();
		setupLoginConfig();

		// operate: register application owner admin user
		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		String ownerLogin = "owner-login-" + UUID.randomUUID().toString();
		String ownerPassword = "owner-password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(ownerLogin, ownerPassword, null);

		// operate: create application owner
		login("admin", "admin");
		String applicationOwnerName = "app-owner-"
				+ UUID.randomUUID().toString();
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.registerApplicationOwner(applicationOwnerName,
				ownerLogin);

		// operate: create application
		String applicationName = "application-" + UUID.randomUUID().toString();
		applicationService.addApplication(applicationName,
				applicationOwnerName, null);

		// operate: change application description via application owner
		login(ownerLogin, ownerPassword);
		String applicationDescription = "An <b>application description</b>";
		applicationService.setApplicationDescription(applicationName,
				applicationDescription);

		// operate: cannot change application description of non-owned
		// application
		try {
			applicationService.setApplicationDescription(
					SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
					"foobar application description");
			fail();
		} catch (RuntimeException e) {
			// expected
			LOG.debug("expected exception: " + e.getMessage());
		}
	}
}
