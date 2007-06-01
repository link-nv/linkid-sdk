/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.auth;

import static test.integ.net.link.safeonline.IntegrationTestUtils.getApplicationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAttributeTypeService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAuthenticationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getCredentialService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getIdentityService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPkiService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubscriptionService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUserRegistrationService;

import java.security.KeyPair;
import java.security.PrivilegedExceptionAction;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingApplicationOwnerException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.sdk.auth.AuthClient;
import net.link.safeonline.sdk.auth.AuthClientImpl;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.PkiService;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import test.integ.net.link.safeonline.IntegrationTestUtils;

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

		String resultUserId = authenticationService.getUserId();
		assertEquals("fcorneli", resultUserId);

		/*
		 * A commitAuthentication can only take place when the user is already
		 * authenticated in the SafeOnline core.
		 */
		IntegrationTestUtils.setupLoginConfig();
		IntegrationTestUtils.login("fcorneli", "secret");
		authenticationService.commitAuthentication("safe-online-user");
	}

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

	public void testAuthenticationCannotCommitBeforeAuthenticate()
			throws Exception {
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		AuthenticationService authenticationService = getAuthenticationService(initialContext);

		// operate & verify
		try {
			authenticationService.commitAuthentication("safe-online-user");
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

	public void testRetrievingMultivaluedAttributes() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		IdentityService identityService = getIdentityService(initialContext);

		String testApplicationName = UUID.randomUUID().toString();

		String testAttributeName = "attr-" + UUID.randomUUID().toString();

		// operate: register user
		String login = "login-" + UUID.randomUUID().toString();
		String password = "pwd-" + UUID.randomUUID().toString();
		userRegistrationService.registerUser(login, password, null);

		// operate: register new multivalued attribute type
		AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, SafeOnlineConstants.STRING_TYPE, true, true);
		attributeType.setMultivalued(true);
		attributeTypeService.add(attributeType);

		// operate: add multivalued attributes
		IntegrationTestUtils.login(login, password);
		String attributeValue1 = "value 1";
		AttributeDO attribute1 = new AttributeDO(testAttributeName,
				SafeOnlineConstants.STRING_TYPE, true, -1, null, null, true,
				true, attributeValue1, null);
		identityService.addAttribute(attribute1);

		String attributeValue2 = "value 2";
		AttributeDO attribute2 = new AttributeDO(testAttributeName,
				SafeOnlineConstants.STRING_TYPE, true, -1, null, null, true,
				true, attributeValue2, null);
		identityService.addAttribute(attribute2);

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
		this.attributeClient.setCaptureMessages(true);
		Object[] result = (Object[]) this.attributeClient.getAttributeValue(
				login, testAttributeName);

		// verify
		Document resultDocument = this.attributeClient.getInboundMessage();
		LOG
				.debug("result message: "
						+ DomTestUtils.domToString(resultDocument));
		LOG.debug("result: " + result);

		// verify number of attribute values returned.
		Element nsElement = resultDocument.createElement("nsElement");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap",
				"http://schemas.xmlsoap.org/soap/envelope/");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp",
				"urn:oasis:names:tc:SAML:2.0:protocol");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml",
				"urn:oasis:names:tc:SAML:2.0:assertion");
		XObject xObject = XPathAPI
				.eval(
						resultDocument,
						"count(/soap:Envelope/soap:Body/samlp:Response/saml:Assertion/saml:AttributeStatement/saml:Attribute/saml:AttributeValue)",
						nsElement);
		double countResult = xObject.num();
		LOG.debug("count result: " + countResult);
		assertEquals(2.0, countResult);
		assertTrue(contains(attributeValue1, result));
		assertTrue(contains(attributeValue2, result));
		assertFalse(contains("foo-bar", result));
	}

	private boolean contains(String value, Object[] items) {
		for (Object item : items) {
			if (value.equals(item)) {
				return true;
			}
		}
		return false;
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
}
