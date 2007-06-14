/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static test.integ.net.link.safeonline.IntegrationTestUtils.getApplicationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAttributeProviderManagerService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAttributeTypeService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getIdentityService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPkiService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubscriptionService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUserRegistrationService;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

import javax.naming.InitialContext;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AttributeProviderManagerService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.sdk.DomUtils;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import test.integ.net.link.safeonline.IntegrationTestUtils;

/**
 * Integration tests for the SafeOnline Data Web Service.
 * 
 * <p>
 * We implemented the integration tests using the JUnit unit test framework.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class DataWebServiceTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(DataWebServiceTest.class);

	private DataClient dataClient;

	private X509Certificate certificate;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");

		this.dataClient = new DataClientImpl("localhost", this.certificate,
				keyPair.getPrivate());
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
					SafeOnlineConstants.NAME_ATTRIBUTE, String.class);
			fail();
		} catch (RequestDeniedException e) {
			// expected
		}

		// operate: add attribute provider
		AttributeProviderManagerService attributeProviderManagerService = getAttributeProviderManagerService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		attributeProviderManagerService.addAttributeProvider(
				testApplicationName, SafeOnlineConstants.NAME_ATTRIBUTE);

		Attribute<String> result = this.dataClient.getAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE, String.class);
		LOG.debug("result: " + result);
		assertNotNull(result);
		assertNull(result.getValue());

		this.dataClient.setCaptureMessages(true);
		this.dataClient.setAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE, testName);

		/*
		 * Verify the message logger facility.
		 */
		assertNotNull(this.dataClient.getInboundMessage());
		assertNotNull(this.dataClient.getOutboundMessage());
		LOG.debug("OUTBOUND message: "
				+ DomUtils.domToString(this.dataClient.getOutboundMessage()));
		LOG.debug("INBOUND message: "
				+ DomUtils.domToString(this.dataClient.getInboundMessage()));

		result = this.dataClient.getAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE, String.class);
		LOG.debug("result: " + result);
		assertEquals(SafeOnlineConstants.NAME_ATTRIBUTE, result.getName());
		assertEquals(testName, result.getValue());

		// check if we can set a string attribute to null
		this.dataClient.setAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE, null);
		result = this.dataClient.getAttributeValue(login,
				SafeOnlineConstants.NAME_ATTRIBUTE, String.class);
		assertNull(result.getValue());
	}

	public void testDataServiceBooleanAttribute() throws Exception {
		// setup
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

		// operate: add boolean attribute type
		AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
		String attributeName = "test-attribute-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, DatatypeType.BOOLEAN, true, true);
		attributeTypeService.add(attributeType);

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.addApplication(testApplicationName, "owner", null,
				this.certificate.getEncoded(), Arrays
						.asList(new IdentityAttributeTypeDO[] {
								new IdentityAttributeTypeDO(
										SafeOnlineConstants.NAME_ATTRIBUTE),
								new IdentityAttributeTypeDO(attributeName) }));

		// operate: subscribe onto the application and confirm identity usage
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);
		IntegrationTestUtils.login(login, password);
		subscriptionService.subscribe(testApplicationName);
		identityService.confirmIdentity(testApplicationName);

		// operate: add attribute provider
		AttributeProviderManagerService attributeProviderManagerService = getAttributeProviderManagerService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		attributeProviderManagerService.addAttributeProvider(
				testApplicationName, attributeName);

		Attribute<Boolean> result = this.dataClient.getAttributeValue(login,
				attributeName, Boolean.class);
		LOG.debug("result: " + result.getValue());
		/*
		 * Because of the identity confirmation the system created an empty
		 * attribute.
		 */
		assertNotNull(result);
		assertNull(result.getValue());

		try {
			this.dataClient.setAttributeValue(login, attributeName,
					"test-value");
			fail();
		} catch (IllegalArgumentException e) {
			// expected: Boolean is required, not a String.
		}

		// set boolean attribute value to true + verify
		this.dataClient.setAttributeValue(login, attributeName, Boolean.TRUE);

		result = this.dataClient.getAttributeValue(login, attributeName,
				Boolean.class);
		LOG.debug("result: " + result.getValue());
		assertEquals(attributeName, result.getName());
		assertEquals(Boolean.TRUE, result.getValue());

		// operate & verify: setting boolean attribute to false
		this.dataClient.setAttributeValue(login, attributeName, Boolean.FALSE);
		result = this.dataClient.getAttributeValue(login, attributeName,
				Boolean.class);
		assertEquals(Boolean.FALSE, result.getValue());

		// operate & verify: setting boolean attribute to null
		this.dataClient.setAttributeValue(login, attributeName, null);
		result = this.dataClient.getAttributeValue(login, attributeName,
				Boolean.class);
		assertNull(result.getValue());
	}

	public void testDataServiceMultivaluedAttribute() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
		IdentityService identityService = getIdentityService(initialContext);

		String testApplicationName = "application-"
				+ UUID.randomUUID().toString();

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

		// operate: add multivalued attribute type
		AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
		String attributeName = "test-attribute-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, DatatypeType.STRING, true, true);
		attributeType.setMultivalued(true);
		attributeTypeService.add(attributeType);

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
										attributeName) }));

		// operate: subscribe onto the application and confirm identity usage
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);
		IntegrationTestUtils.login(login, password);
		subscriptionService.subscribe(testApplicationName);
		identityService.confirmIdentity(testApplicationName);

		// operate: add attribute provider
		AttributeProviderManagerService attributeProviderManagerService = getAttributeProviderManagerService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		attributeProviderManagerService.addAttributeProvider(
				testApplicationName, attributeName);

		Attribute<String[]> result = this.dataClient.getAttributeValue(login,
				attributeName, String[].class);
		LOG.debug("result: " + result.getValue());
		assertNotNull(result);
		assertNull(result.getValue());

		try {
			this.dataClient.setAttributeValue(login, attributeName,
					Boolean.TRUE);
			fail();
		} catch (IllegalArgumentException e) {
			// expected: String is required, not a Boolean.
		}

		// set attribute value & verify
		String attributeValue1 = "test-attribute-value-1";
		this.dataClient.setAttributeValue(login, attributeName,
				new String[] { attributeValue1 });

		result = this.dataClient.getAttributeValue(login, attributeName,
				String[].class);
		LOG.debug("result: " + result.getValue());
		assertEquals(attributeName, result.getName());
		assertEquals(attributeValue1, result.getValue()[0]);

		IntegrationTestUtils.login(login, password);
		AttributeDO attribute2 = new AttributeDO(attributeName,
				DatatypeType.STRING);
		String attributeValue2 = "test-attribute-value-2";
		attribute2.setStringValue(attributeValue2);
		identityService.addAttribute(attribute2);

		this.dataClient.setCaptureMessages(true);
		result = this.dataClient.getAttributeValue(login, attributeName,
				String[].class);
		// assertNotNull(result.getValue());
		LOG.debug("result: " + result.getValue());
		Document resultMessage = this.dataClient.getInboundMessage();
		LOG.debug("Request SOAP message: "
				+ DomTestUtils
						.domToString(this.dataClient.getOutboundMessage()));
		LOG.debug("Response SOAP message: "
				+ DomTestUtils.domToString(resultMessage));
		LOG.debug("result values: " + result.getValue());
		for (String value : result.getValue()) {
			LOG.debug("result value: " + value);
		}
		assertEquals(attributeValue1, result.getValue()[0]);
		assertEquals(attributeValue2, result.getValue()[1]);
	}

	public void testCreateAttribute() throws Exception {
		// setup
		InitialContext initialContext = IntegrationTestUtils
				.getInitialContext();

		IntegrationTestUtils.setupLoginConfig();

		UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);

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

		// operate: add boolean attribute type
		AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
		String attributeName = "test-attribute-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, DatatypeType.BOOLEAN, true, true);
		attributeTypeService.add(attributeType);

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.addApplication(testApplicationName, "owner", null,
				this.certificate.getEncoded(), Arrays
						.asList(new IdentityAttributeTypeDO[] {
								new IdentityAttributeTypeDO(
										SafeOnlineConstants.NAME_ATTRIBUTE),
								new IdentityAttributeTypeDO(attributeName) }));

		// operate: add attribute provider
		AttributeProviderManagerService attributeProviderManagerService = getAttributeProviderManagerService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		attributeProviderManagerService.addAttributeProvider(
				testApplicationName, attributeName);

		Attribute<Boolean> result = this.dataClient.getAttributeValue(login,
				attributeName, Boolean.class);
		assertNull(result);

		try {
			this.dataClient.setAttributeValue(login, attributeName,
					Boolean.TRUE);
			fail();
		} catch (AttributeNotFoundException e) {
			// expected
		}

		this.dataClient.createAttribute(login, attributeName);
	}
}
