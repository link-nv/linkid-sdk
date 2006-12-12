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
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
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

		AuthenticationService authenticationService = EjbUtils.getEJB(
				initialContext, "SafeOnline/AuthenticationServiceBean/remote",
				AuthenticationService.class);

		boolean result = authenticationService.authenticate("fcorneli",
				"secret");
		assertTrue(result);
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

		final ApplicationService applicationService = EjbUtils.getEJB(
				initialContext, "SafeOnline/ApplicationServiceBean/remote",
				ApplicationService.class);

		Subject subject = login("admin", "admin");

		Subject.doAs(subject, new PrivilegedExceptionAction() {
			public Object run() throws Exception {
				String applicationName = "application-"
						+ UUID.randomUUID().toString();
				applicationService.addApplication(applicationName, null);
				return null;
			}
		});
	}

	public void testBigUseCase() throws Exception {
		InitialContext initialContext = getInitialContext();

		setupLoginConfig();

		final ApplicationService applicationService = EjbUtils.getEJB(
				initialContext, "SafeOnline/ApplicationServiceBean/remote",
				ApplicationService.class);

		Subject adminSubject = login("admin", "admin");

		final String applicationName = "application-"
				+ UUID.randomUUID().toString();

		applicationService.addApplication(applicationName, null);
		/*
		 * Subject.doAs(adminSubject, new PrivilegedExceptionAction() { public
		 * Object run() throws Exception {
		 * applicationService.addApplication(applicationName, null); return
		 * null; } });
		 */

		final UserRegistrationService userRegistrationService = EjbUtils
				.getEJB(initialContext,
						"SafeOnline/UserRegistrationServiceBean/remote",
						UserRegistrationService.class);
		String login = "login-" + UUID.randomUUID().toString();
		final String password = "secret";

		userRegistrationService.registerUser(login, password, null);

		final String name = "name-" + UUID.randomUUID().toString();

		final IdentityService identityService = EjbUtils.getEJB(initialContext,
				"SafeOnline/IdentityServiceBean/remote", IdentityService.class);

		Subject subject = login(login, password);
		identityService.saveName(name);
		String resultName = identityService.getName();
		/*
		 * String resultName = (String) Subject.doAs(subject, new
		 * PrivilegedExceptionAction<String>() { public String run() throws
		 * Exception { identityService.saveName(name); return
		 * identityService.getName(); } });
		 */
		assertEquals(name, resultName);

		final CredentialService credentialService = EjbUtils.getEJB(
				initialContext, "SafeOnline/CredentialServiceBean/remote",
				CredentialService.class);

		final String newPassword = "secret-" + UUID.randomUUID().toString();

		/*
		 * Next is not really required.
		 */
		Subject.doAs(subject, new PrivilegedExceptionAction() {
			public Object run() throws Exception {
				credentialService.changePassword(password, newPassword);
				return null;
			}
		});

		subject = login(login, newPassword);
		resultName = (String) Subject.doAs(subject,
				new PrivilegedExceptionAction<String>() {
					public String run() throws Exception {
						return identityService.getName();
					}
				});
		assertEquals(name, resultName);

		final SubscriptionService subscriptionService = EjbUtils.getEJB(
				initialContext, "SafeOnline/SubscriptionServiceBean/remote",
				SubscriptionService.class);

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

	public void testCreateApplicationOwner() throws Exception {
		InitialContext initialContext = getInitialContext();

		setupLoginConfig();

		UserRegistrationService userRegistrationService = EjbUtils.getEJB(
				initialContext,
				"SafeOnline/UserRegistrationServiceBean/remote",
				UserRegistrationService.class);

		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		ApplicationService applicationService = EjbUtils.getEJB(initialContext,
				"SafeOnline/ApplicationServiceBean/remote",
				ApplicationService.class);

		login("admin", "admin");
		applicationService.registerApplicationOwner(login);

		String applicationName = "application-" + UUID.randomUUID().toString();
		applicationService.addApplication(applicationName, null);

		login(login, password);
		applicationService.setApplicationDescription(applicationName,
				"test application description");

		login("admin", "admin");
		try {
			applicationService.registerApplicationOwner(login);
			fail();
		} catch (AlreadySubscribedException e) {
			// expected
		}
	}
}
