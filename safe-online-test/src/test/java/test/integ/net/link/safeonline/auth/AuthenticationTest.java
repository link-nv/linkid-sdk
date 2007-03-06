/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.auth;

import java.security.KeyPair;
import java.security.PrivilegedExceptionAction;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.sdk.attrib.AttributeClient;
import net.link.safeonline.sdk.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.attrib.AttributeNotFoundException;
import net.link.safeonline.sdk.auth.AuthClient;
import net.link.safeonline.sdk.auth.AuthClientImpl;
import net.link.safeonline.service.PkiService;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.accept.net.link.safeonline.IntegrationTestUtils;

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

	private AttributeClient attributeClient;

	private X509Certificate certificate;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.authClient = new AuthClientImpl(SAFE_ONLINE_LOCATION);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");

		this.attributeClient = new AttributeClientImpl("localhost",
				this.certificate, keyPair.getPrivate());
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
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

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

	public void testAddApplication() throws Exception {

		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		final ApplicationService applicationService = getApplicationService(initialContext);

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		Subject subject = IntegrationTestUtils.login("admin", "admin");

		final String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, login);

		final String applicationName = "application-"
				+ UUID.randomUUID().toString();

		Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
			public Object run() throws Exception {

				applicationService.addApplication(applicationName,
						appOwnerName, null, null, new String[] {});
				return null;
			}
		});

		applicationService.removeApplication(applicationName);
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

	private IdentityService getIdentityService(InitialContext initialContext) {
		IdentityService identityService = EjbUtils.getEJB(initialContext,
				"SafeOnline/IdentityServiceBean/remote", IdentityService.class);
		return identityService;
	}

	public void testBigUseCase() throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		final ApplicationService applicationService = getApplicationService(initialContext);

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		String ownerLogin = "login-" + UUID.randomUUID().toString();
		String ownerPassword = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(ownerLogin, ownerPassword, null);

		IntegrationTestUtils.login("admin", "admin");

		final String applicationName = "application-"
				+ UUID.randomUUID().toString();

		final String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, ownerLogin);

		applicationService.addApplication(applicationName, appOwnerName, null,
				null, new String[] {});

		String userLogin = "login-" + UUID.randomUUID().toString();
		final String userPassword = "secret";

		userRegistrationService.registerUser(userLogin, userPassword, null);

		final String userName = "name-" + UUID.randomUUID().toString();

		final IdentityService identityService = EjbUtils.getEJB(initialContext,
				"SafeOnline/IdentityServiceBean/remote", IdentityService.class);

		IntegrationTestUtils.login(userLogin, userPassword);

		identityService.saveAttribute(SafeOnlineConstants.NAME_ATTRIBUTE,
				userName);
		String resultName = identityService
				.findAttributeValue(SafeOnlineConstants.NAME_ATTRIBUTE);
		assertEquals(userName, resultName);

		final CredentialService credentialService = getCredentialService(initialContext);

		final String newPassword = "secret-" + UUID.randomUUID().toString();

		credentialService.changePassword(userPassword, newPassword);

		IntegrationTestUtils.login(userLogin, newPassword);
		resultName = identityService
				.findAttributeValue(SafeOnlineConstants.NAME_ATTRIBUTE);
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

	private PkiService getPkiService(InitialContext initialContext) {
		final PkiService pkiService = EjbUtils.getEJB(initialContext,
				"SafeOnline/PkiServiceBean/remote", PkiService.class);
		return pkiService;
	}

	public void testCreateApplicationOwner() throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		ApplicationService applicationService = getApplicationService(initialContext);

		IntegrationTestUtils.login("admin", "admin");
		String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, login);

		String applicationName = "application-" + UUID.randomUUID().toString();
		applicationService.addApplication(applicationName, appOwnerName, null,
				null, new String[] {});

		IntegrationTestUtils.login(login, password);
		applicationService.setApplicationDescription(applicationName,
				"test application description");

		IntegrationTestUtils.login("admin", "admin");
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
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		// operate: register application owner admin user
		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		String ownerLogin = "owner-login-" + UUID.randomUUID().toString();
		String ownerPassword = "owner-password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(ownerLogin, ownerPassword, null);

		// operate: create application owner
		IntegrationTestUtils.login("admin", "admin");
		String applicationOwnerName = "app-owner-"
				+ UUID.randomUUID().toString();
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.registerApplicationOwner(applicationOwnerName,
				ownerLogin);

		// operate: create application
		String applicationName = "application-" + UUID.randomUUID().toString();
		applicationService.addApplication(applicationName,
				applicationOwnerName, null, null, new String[] {});

		// operate: change application description via application owner
		IntegrationTestUtils.login(ownerLogin, ownerPassword);
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

	public void testCredentialCacheFlushOnSubscription() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		// operate: register a new user
		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		// operate: trigger JAAS on the core
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);
		IntegrationTestUtils.login(login, password);
		subscriptionService.getSubscriptions();

		// operate: create application owner
		ApplicationService applicationService = getApplicationService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		String applicationOwner = "owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(applicationOwner, login);

		// operate: get owned applications
		IntegrationTestUtils.login(login, password);
		applicationService.getOwnedApplications();
	}

	public void testUserCannotRetrieveThePasswordAttribute() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		// operate: register a new user
		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		IdentityService identityService = getIdentityService(initialContext);

		// operate: cannot retrieve password attribute
		try {
			identityService
					.findAttributeValue(SafeOnlineConstants.PASSWORD_ATTRIBUTE);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	public void testUserCannotEditThePasswordAttribute() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		// operate: register a new user
		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		IdentityService identityService = getIdentityService(initialContext);

		// operate: cannot retrieve password attribute
		try {
			identityService.saveAttribute(
					SafeOnlineConstants.PASSWORD_ATTRIBUTE, "test-password");
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	public void testAttributeService() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		IdentityService identityService = getIdentityService(initialContext);

		String testName = "test-name";

		// operate: register user
		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		// operate: save name attribute
		IntegrationTestUtils.login(login, password);
		identityService.saveAttribute(SafeOnlineConstants.NAME_ATTRIBUTE,
				testName);

		// operate: register certificate as application trust point
		PkiService pkiService = getPkiService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		pkiService.addTrustPoint(
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
				this.certificate.getEncoded());

		// operate: retrieve name attribute via web service
		String result = this.attributeClient.getAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE);

		// verify
		LOG.debug("result attribute value: " + result);
		assertEquals(testName, result);
	}

	public void testRetrieveNonExistingAttribute() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		// operate: register user
		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		// operate: register certificate as application trust point
		PkiService pkiService = getPkiService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		pkiService.addTrustPoint(
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
				this.certificate.getEncoded());

		// operate & verify: retrieve name attribute via web service
		try {
			this.attributeClient.getAttributeValue(login,
					"foo-bar-attribute-name");
			fail();
		} catch (AttributeNotFoundException e) {
			// expected
		}
	}
}
