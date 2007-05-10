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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AttributeProviderManagerService;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.sdk.auth.AuthClient;
import net.link.safeonline.sdk.auth.AuthClientImpl;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.data.DataValue;
import net.link.safeonline.service.AttributeTypeService;
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

	private DataClient dataClient;

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

		this.dataClient = new DataClientImpl("localhost", this.certificate,
				keyPair.getPrivate());
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

		boolean result = authenticationService.authenticate("safe-online-user",
				"fcorneli", "secret");
		assertTrue(result);
	}

	private AuthenticationService getAuthenticationService(
			InitialContext initialContext) {
		AuthenticationService authenticationService = EjbUtils.getEJB(
				initialContext, "SafeOnline/AuthenticationServiceBean/remote",
				AuthenticationServiceRemote.class);
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
						appOwnerName, null, null, null);
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

	private AttributeTypeService getAttributeTypeService(
			InitialContext initialContext) {
		final AttributeTypeService attributeTypeService = EjbUtils.getEJB(
				initialContext, "SafeOnline/AttributeTypeServiceBean/remote",
				AttributeTypeService.class);
		return attributeTypeService;
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
				null, null);

		String userLogin = "login-" + UUID.randomUUID().toString();
		final String userPassword = "secret";

		userRegistrationService.registerUser(userLogin, userPassword, null);

		final String userName = "name-" + UUID.randomUUID().toString();

		final IdentityService identityService = EjbUtils.getEJB(initialContext,
				"SafeOnline/IdentityServiceBean/remote", IdentityService.class);

		IntegrationTestUtils.login(userLogin, userPassword);

		AttributeDO attribute = new AttributeDO(
				SafeOnlineConstants.NAME_ATTRIBUTE, "string");
		attribute.setStringValue(userName);
		identityService.saveAttribute(attribute);
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
	}

	private SubscriptionService getSubscriptionService(
			InitialContext initialContext) {
		final SubscriptionService subscriptionService = EjbUtils.getEJB(
				initialContext, "SafeOnline/SubscriptionServiceBean/remote",
				SubscriptionService.class);
		return subscriptionService;
	}

	private AttributeProviderManagerService getAttributeProviderManagerService(
			InitialContext initialContext) {
		final AttributeProviderManagerService attributeProviderManagerService = EjbUtils
				.getEJB(
						initialContext,
						"SafeOnline/AttributeProviderManagerServiceBean/remote",
						AttributeProviderManagerService.class);
		return attributeProviderManagerService;
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
				null, null);

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
				applicationOwnerName, null, null, null);

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
		} catch (PermissionDeniedException e) {
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
		subscriptionService.listSubscriptions();

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
		AttributeDO attribute = new AttributeDO(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, "string");
		attribute.setStringValue("test-password");
		try {
			identityService.saveAttribute(attribute);
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

		String testName = "test-name-" + UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();

		String testAttributeName = "attr-" + UUID.randomUUID().toString();
		String testAttributeValue = "test-attribute-value";

		// operate: register user
		String login = "login-" + UUID.randomUUID().toString();
		String password = "pwd-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		// operate: save name attribute
		IntegrationTestUtils.login(login, password);
		AttributeDO attribute = new AttributeDO(
				SafeOnlineConstants.NAME_ATTRIBUTE, "string");
		attribute.setStringValue(testName);
		identityService.saveAttribute(attribute);

		// operate: register new attribute type
		AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, SafeOnlineConstants.STRING_TYPE, true, true);
		attributeTypeService.add(attributeType);

		// operate: register certificate as application trust point
		PkiService pkiService = getPkiService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		pkiService.addTrustPoint(
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
				this.certificate.getEncoded());

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.addApplication(testApplicationName, "owner", null,
				this.certificate.getEncoded(),
				Arrays.asList(new IdentityAttributeTypeDO[] {
						new IdentityAttributeTypeDO(
								SafeOnlineConstants.NAME_ATTRIBUTE),
						new IdentityAttributeTypeDO(testAttributeName) }));

		// operate: subscribe onto the application and confirm identity usage
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);
		IntegrationTestUtils.login(login, password);
		subscriptionService.subscribe(testApplicationName);
		identityService.confirmIdentity(testApplicationName);

		// operate: retrieve name attribute via web service
		String result = (String) this.attributeClient.getAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE);

		// verify
		LOG.debug("result attribute value: " + result);
		LOG.debug("application name: " + testApplicationName);
		assertEquals(testName, result);

		// operate: retrieve all accessible attributes.
		Map<String, Object> resultAttributes = this.attributeClient
				.getAttributeValues(login);

		// verify
		assertEquals(2, resultAttributes.size());
		LOG.info("resultAttributes: " + resultAttributes);
		result = (String) resultAttributes
				.get(SafeOnlineConstants.NAME_ATTRIBUTE);
		assertEquals(testName, result);
		assertNull(resultAttributes.get(testAttributeName));

		// operate: set attribute
		IntegrationTestUtils.login(login, password);
		AttributeDO attributeDO = new AttributeDO(testAttributeName,
				SafeOnlineConstants.STRING_TYPE);
		attributeDO.setStringValue(testAttributeValue);
		identityService.saveAttribute(attributeDO);

		String resultValue = (String) this.attributeClient.getAttributeValue(
				login, testAttributeName);
		assertEquals(testAttributeValue, resultValue);

		// operate: retrieve all attributes
		resultAttributes = this.attributeClient.getAttributeValues(login);
		LOG.info("resultAttributes: " + resultAttributes);
		assertEquals(2, resultAttributes.size());
		assertEquals(testAttributeValue, resultAttributes
				.get(testAttributeName));
	}

	public void testFindAttributeValue() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();
		IntegrationTestUtils.setupLoginConfig();
		IdentityService identityService = getIdentityService(initialContext);

		// operate
		IntegrationTestUtils.login("fcorneli", "secret");

		String result = identityService
				.findAttributeValue(SafeOnlineConstants.NAME_ATTRIBUTE);

		// verify
		LOG.debug("result: " + result);
	}

	public void testDataService() throws Exception {
		// setup
		String testName = "test-name";
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		IdentityService identityService = getIdentityService(initialContext);

		String testApplicationName = UUID.randomUUID().toString();

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

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						this.certificate.getEncoded(),
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										SafeOnlineConstants.NAME_ATTRIBUTE) }));

		// operate: subscribe onto the application and confirm identity usage
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);
		IntegrationTestUtils.login(login, password);
		subscriptionService.subscribe(testApplicationName);
		identityService.confirmIdentity(testApplicationName);

		// operate & verify
		try {
			this.dataClient.getAttributeValue(login,
					SafeOnlineConstants.NAME_ATTRIBUTE);
			fail();
		} catch (RequestDeniedException e) {
			// expected
		}

		// operate: add attribute provider
		AttributeProviderManagerService attributeProviderManagerService = getAttributeProviderManagerService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		attributeProviderManagerService.addAttributeProvider(
				testApplicationName, SafeOnlineConstants.NAME_ATTRIBUTE);

		DataValue result = this.dataClient.getAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE);
		LOG.debug("result: " + result);
		assertNotNull(result);
		assertNull(result.getValue());

		this.dataClient.setAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE, testName);

		result = this.dataClient.getAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE);
		LOG.debug("result: " + result);
		assertEquals(SafeOnlineConstants.NAME_ATTRIBUTE, result.getName());
		assertEquals(testName, result.getValue());
	}
}
