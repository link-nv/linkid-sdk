/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getApplicationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAuthenticationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getCredentialService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getIdentityService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubjectService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubscriptionService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUserRegistrationService;

import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.security.auth.Subject;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import test.integ.net.link.safeonline.IntegrationTestUtils;

/**
 * Integration test for the SafeOnline authentication web service.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationTest {

	private static final Log LOG = LogFactory.getLog(AuthenticationTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAuthenticationOverRMI() throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		AuthenticationService authenticationService = getAuthenticationService(initialContext);

		boolean result = authenticationService.authenticate("fcorneli",
				"secret");
		assertTrue(result);

		String resultUserName = authenticationService.getUsername();
		assertEquals("fcorneli", resultUserName);

		String userId = authenticationService.getUserId();

		/*
		 * A commitAuthentication can only take place when the user is already
		 * authenticated in the SafeOnline core.
		 */
		IntegrationTestUtils.setupLoginConfig();
		IntegrationTestUtils.login(userId, "secret");
		authenticationService.commitAuthentication(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, null);
	}

	@Test
	public void testIncorrectPassword() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		AuthenticationService authenticationService = getAuthenticationService(initialContext);

		// operate
		boolean result = authenticationService.authenticate("fcorneli",
				"foobar-password");

		// verify
		assertFalse(result);
	}

	@Test
	public void testAuthenticationAbort() throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		AuthenticationService authenticationService = getAuthenticationService(initialContext);

		boolean result = authenticationService.authenticate("fcorneli",
				"secret");
		assertTrue(result);

		/*
		 * The abort method has the @Remove annotation on the bean instance.
		 */
		authenticationService.abort();

		// operate & verify
		try {
			/*
			 * We can only use a statefull session bean once.
			 */
			authenticationService.authenticate("fcorneli", "secret");
			fail();
		} catch (NoSuchEJBException e) {
			// expected
		}
	}

	@Test
	public void testAuthenticationCannotCommitBeforeAuthenticate()
			throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		AuthenticationService authenticationService = getAuthenticationService(initialContext);

		// operate & verify
		try {
			authenticationService
					.commitAuthentication("safe-online-user", null);
			fail();
		} catch (EJBException e) {
			// expected
			LOG.debug("expected exception: " + e.getMessage());
			LOG.debug("expected exception type: " + e.getClass().getName());
		}

		// operate & verify: cannot continue after system exception
		try {
			authenticationService.authenticate("fcorneli", "secret");
			fail();
		} catch (NoSuchEJBException e) {
			// expected
		}
	}

	@Test
	public void testAddApplication() throws Exception {

		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		final ApplicationService applicationService = getApplicationService(initialContext);

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		final SubjectService subjectService = getSubjectService(initialContext);

		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password);

		SubjectEntity loginSubject = subjectService
				.findSubjectFromUserName(login);

		SubjectEntity adminSubject = subjectService
				.findSubjectFromUserName("admin");

		Subject subject = IntegrationTestUtils.login(adminSubject.getUserId(),
				"admin");

		final String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, loginSubject
				.getUserId());

		final String applicationName = "application-"
				+ UUID.randomUUID().toString();

		Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
			public Object run() throws Exception {

				applicationService.addApplication(applicationName, null,
						appOwnerName, null, false, null, null, null);
				return null;
			}
		});

		applicationService.removeApplication(applicationName);
	}

	@Test
	public void testBigUseCase() throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		final ApplicationService applicationService = getApplicationService(initialContext);

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		final SubjectService subjectService = getSubjectService(initialContext);

		String ownerLogin = "login-" + UUID.randomUUID().toString();
		String ownerPassword = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(ownerLogin, ownerPassword);

		SubjectEntity ownerSubject = subjectService
				.findSubjectFromUserName(ownerLogin);
		SubjectEntity adminSubject = subjectService
				.findSubjectFromUserName("admin");

		IntegrationTestUtils.login(adminSubject.getUserId(), "admin");

		final String applicationName = "application-"
				+ UUID.randomUUID().toString();

		final String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, ownerSubject
				.getUserId());

		applicationService.addApplication(applicationName, null, appOwnerName,
				null, false, null, null, null);

		String userLogin = "login-" + UUID.randomUUID().toString();
		final String userPassword = "secret";

		userRegistrationService.registerUser(userLogin, userPassword);

		SubjectEntity userSubject = subjectService
				.findSubjectFromUserName(userLogin);

		final String userName = "name-" + UUID.randomUUID().toString();

		final IdentityService identityService = EjbUtils.getEJB(initialContext,
				"SafeOnline/IdentityServiceBean/remote", IdentityService.class);

		IntegrationTestUtils.login(userSubject.getUserId(), userPassword);

		AttributeDO attribute = new AttributeDO(
				SafeOnlineConstants.NAME_ATTRIBUTE, DatatypeType.STRING);
		attribute.setStringValue(userName);
		attribute.setEditable(true);
		/*
		 * If we don't mark the attribute as editable the identityService will
		 * skip the saveAttribute operation.
		 */
		identityService.saveAttribute(attribute);
		String resultName = identityService
				.findAttributeValue(SafeOnlineConstants.NAME_ATTRIBUTE);
		assertEquals(userName, resultName);

		final CredentialService credentialService = getCredentialService(initialContext);

		final String newPassword = "secret-" + UUID.randomUUID().toString();

		credentialService.changePassword(userPassword, newPassword);

		IntegrationTestUtils.login(userSubject.getUserId(), newPassword);
		resultName = identityService
				.findAttributeValue(SafeOnlineConstants.NAME_ATTRIBUTE);
		assertEquals(userName, resultName);

		final SubscriptionService subscriptionService = getSubscriptionService(initialContext);

		// JAAS caches the credentials...
		List<SubscriptionEntity> subscriptions = subscriptionService
				.listSubscriptions();
		for (SubscriptionEntity subscription : subscriptions) {
			LOG.debug("subscription: " + subscription);
		}
		assertEquals(1, subscriptions.size());

		subscriptionService.subscribe(applicationName);

		subscriptions = subscriptionService.listSubscriptions();
		assertEquals(2, subscriptions.size());
		for (SubscriptionEntity subscription : subscriptions) {
			LOG.debug("subscription: " + subscription);
		}

		IntegrationTestUtils.login(adminSubject.getUserId(), "admin");
		applicationService.removeApplication(applicationName);
	}

	@Test
	public void testCreateApplicationOwner() throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		final SubjectService subjectService = getSubjectService(initialContext);

		String login = "login-" + UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password);

		SubjectEntity loginSubject = subjectService
				.findSubjectFromUserName(login);

		SubjectEntity adminSubject = subjectService
				.findSubjectFromUserName("admin");

		ApplicationService applicationService = getApplicationService(initialContext);

		IntegrationTestUtils.login(adminSubject.getUserId(), "admin");
		String appOwnerName = "app-owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(appOwnerName, loginSubject
				.getUserId());

		String applicationName = "application-" + UUID.randomUUID().toString();
		applicationService.addApplication(applicationName, null, appOwnerName,
				null, false, null, null, null);

		IntegrationTestUtils.login(loginSubject.getUserId(), password);
		applicationService.setApplicationDescription(applicationName,
				"test application description");

		IntegrationTestUtils.login(adminSubject.getUserId(), "admin");
		try {
			applicationService.registerApplicationOwner(appOwnerName,
					loginSubject.getUserId());
			fail();
		} catch (ExistingApplicationOwnerException e) {
			// expected
		}
	}

	@Test
	public void testChangingApplicationDescriptionTriggersOwnershipCheck()
			throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		final SubjectService subjectService = getSubjectService(initialContext);

		// operate: register application owner admin user
		String ownerLogin = "owner-login-" + UUID.randomUUID().toString();
		String ownerPassword = "owner-password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(ownerLogin, ownerPassword);

		SubjectEntity ownerSubject = subjectService
				.findSubjectFromUserName(ownerLogin);
		SubjectEntity adminSubject = subjectService
				.findSubjectFromUserName("admin");

		// operate: create application owner
		IntegrationTestUtils.login(adminSubject.getUserId(), "admin");
		String applicationOwnerName = "app-owner-"
				+ UUID.randomUUID().toString();
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.registerApplicationOwner(applicationOwnerName,
				ownerSubject.getUserId());

		// operate: create application
		String applicationName = "application-" + UUID.randomUUID().toString();
		applicationService.addApplication(applicationName, null,
				applicationOwnerName, null, false, null, null, null);

		// operate: change application description via application owner
		IntegrationTestUtils.login(ownerSubject.getUserId(), ownerPassword);
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
		} catch (PermissionDeniedException e) {
			// expected
			LOG.debug("expected exception: " + e.getMessage());
		}
	}

	@Test
	public void testCredentialCacheFlushOnSubscription() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		final UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

		final SubjectService subjectService = getSubjectService(initialContext);

		// operate: register a new user
		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password);

		SubjectEntity loginSubject = subjectService
				.findSubjectFromUserName(login);

		SubjectEntity adminSubject = subjectService
				.findSubjectFromUserName("admin");

		// operate: trigger JAAS on the core
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);
		IntegrationTestUtils.login(loginSubject.getUserId(), password);
		subscriptionService.listSubscriptions();

		// operate: create application owner
		ApplicationService applicationService = getApplicationService(initialContext);
		IntegrationTestUtils.login(adminSubject.getUserId(), "admin");
		String applicationOwner = "owner-" + UUID.randomUUID().toString();
		applicationService.registerApplicationOwner(applicationOwner,
				loginSubject.getUserId());

		// operate: get owned applications
		IntegrationTestUtils.login(loginSubject.getUserId(), password);
		applicationService.getOwnedApplications();
	}

	@Test
	public void testUserCannotRetrieveThePasswordAttribute() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		// operate: register a new user
		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password);

		IdentityService identityService = getIdentityService(initialContext);

		// operate: cannot retrieve password attributes
		try {
			identityService
					.findAttributeValue(SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
		try {
			identityService
					.findAttributeValue(SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
		try {
			identityService
					.findAttributeValue(SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	@Test
	public void testUserCannotEditThePasswordAttribute() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();

		// operate: register a new user
		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		String login = "login-" + UUID.randomUUID().toString();
		String password = "password-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password);

		IdentityService identityService = getIdentityService(initialContext);

		// operate: cannot retrieve password attribute
		AttributeDO hashAttribute = new AttributeDO(
				SafeOnlineConstants.PASSWORD_HASH_ATTRIBUTE,
				DatatypeType.STRING);
		hashAttribute.setStringValue("test-hash");
		hashAttribute.setEditable(true);
		AttributeDO seedAttribute = new AttributeDO(
				SafeOnlineConstants.PASSWORD_SEED_ATTRIBUTE,
				DatatypeType.STRING);
		seedAttribute.setStringValue("test-seed");
		seedAttribute.setEditable(true);
		AttributeDO algorithmAttribute = new AttributeDO(
				SafeOnlineConstants.PASSWORD_ALGORITHM_ATTRIBUTE,
				DatatypeType.STRING);
		algorithmAttribute.setStringValue("test-algorithm");
		algorithmAttribute.setEditable(true);
		/*
		 * If we don't mark the attribute as editable the identity service will
		 * skip the saveAttribute operation.
		 */
		try {
			identityService.saveAttribute(hashAttribute);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
		try {
			identityService.saveAttribute(seedAttribute);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
		try {
			identityService.saveAttribute(algorithmAttribute);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	@Test
	public void testFindAttributeValue() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();
		final IdentityService identityService = getIdentityService(initialContext);

		final SubjectService subjectService = getSubjectService(initialContext);

		SubjectEntity userSubject = subjectService
				.findSubjectFromUserName("fcorneli");

		// operate
		IntegrationTestUtils.login(userSubject.getUserId(), "secret");

		String result = identityService
				.findAttributeValue(SafeOnlineConstants.NAME_ATTRIBUTE);

		// verify
		LOG.debug("result: " + result);
	}
}
