/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.InitialContext;

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
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.sdk.DomUtils;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.CompoundBuilder;
import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundId;
import net.link.safeonline.sdk.ws.annotation.CompoundMember;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
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
public class DataWebServiceTest {

	private static final Log LOG = LogFactory.getLog(DataWebServiceTest.class);

	private DataClient dataClient;

	private X509Certificate certificate;

	@Before
	public void setUp() throws Exception {
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");

		this.dataClient = new DataClientImpl("localhost", this.certificate,
				keyPair.getPrivate());
	}

	@Test
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
		userRegistrationService.registerUser(login, password);

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
						null,
						"owner",
						null,
						false,
						IdScopeType.USER,
						null,
						null,
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

	@SuppressWarnings("unchecked")
	@Test
	public void dataServiceCompoundedAttribute() throws Exception {
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
		userRegistrationService.registerUser(login, password);

		// operate: register certificate as application trust point
		PkiService pkiService = getPkiService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		pkiService.addTrustPoint(
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
				this.certificate.getEncoded());

		// operate: add attribute type
		AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
		List<AttributeTypeEntity> existingAttributeTypes = attributeTypeService
				.listAttributeTypes();
		AttributeTypeEntity firstMemberAttributeType = new AttributeTypeEntity(
				TEST_MEMBER_0_NAME, DatatypeType.STRING, true, true);
		firstMemberAttributeType.setMultivalued(true);
		if (false == existingAttributeTypes.contains(firstMemberAttributeType))
			attributeTypeService.add(firstMemberAttributeType);

		AttributeTypeEntity secondMemberAttributeType = new AttributeTypeEntity(
				TEST_MEMBER_1_NAME, DatatypeType.BOOLEAN, true, true);
		secondMemberAttributeType.setMultivalued(true);
		if (false == existingAttributeTypes.contains(secondMemberAttributeType))
			attributeTypeService.add(secondMemberAttributeType);

		AttributeTypeEntity compoundAttributeType = new AttributeTypeEntity(
				TEST_COMP_NAME, DatatypeType.COMPOUNDED, true, true);
		compoundAttributeType.setMultivalued(true);
		compoundAttributeType.addMember(firstMemberAttributeType, 0, true);
		compoundAttributeType.addMember(secondMemberAttributeType, 1, true);
		if (false == existingAttributeTypes.contains(compoundAttributeType))
			attributeTypeService.add(compoundAttributeType);

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.addApplication(testApplicationName, null, "owner",
				null, false, IdScopeType.USER, null, null, null,
				this.certificate.getEncoded(), Arrays
						.asList(new IdentityAttributeTypeDO[] {
								new IdentityAttributeTypeDO(
										SafeOnlineConstants.NAME_ATTRIBUTE),
								new IdentityAttributeTypeDO(TEST_COMP_NAME) }));

		// operate: subscribe onto the application and confirm identity usage
		SubscriptionService subscriptionService = getSubscriptionService(initialContext);
		IntegrationTestUtils.login(login, password);
		subscriptionService.subscribe(testApplicationName);
		identityService.confirmIdentity(testApplicationName);

		// operate: add attribute provider
		AttributeProviderManagerService attributeProviderManagerService = getAttributeProviderManagerService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		attributeProviderManagerService.addAttributeProvider(
				testApplicationName, TEST_COMP_NAME);

		this.dataClient.setCaptureMessages(true);
		Attribute<CompoundedTestClass[]> result = this.dataClient
				.getAttributeValue(login, TEST_COMP_NAME,
						CompoundedTestClass[].class);
		LOG.debug("result message: "
				+ DomUtils.domToString(this.dataClient.getInboundMessage()));
		assertNull(result);

		// operate: add 2 compounded attribute records
		IntegrationTestUtils.login(login, password);
		AttributeDO compAttribute = new AttributeDO(TEST_COMP_NAME,
				DatatypeType.COMPOUNDED, true, -1, null, null, true, true,
				null, null);
		String attributeValue1 = "value 00";
		AttributeDO attribute1 = new AttributeDO(TEST_MEMBER_0_NAME,
				DatatypeType.STRING, true, -1, null, null, true, true,
				attributeValue1, null);
		Boolean attributeValue2 = true;
		AttributeDO attribute2 = new AttributeDO(TEST_MEMBER_1_NAME,
				DatatypeType.BOOLEAN, true, -1, null, null, true, true, null,
				attributeValue2);
		List<AttributeDO> attributes = new LinkedList<AttributeDO>();
		attributes.add(compAttribute);
		attributes.add(attribute1);
		attributes.add(attribute2);
		identityService.addAttribute(attributes);

		attribute1.setStringValue("value 10");
		attribute2.setBooleanValue(false);
		identityService.addAttribute(attributes);

		this.dataClient.setCaptureMessages(true);
		result = this.dataClient.getAttributeValue(login, TEST_COMP_NAME,
				CompoundedTestClass[].class);
		LOG.debug("result message: "
				+ DomUtils.domToString(this.dataClient.getInboundMessage()));

		assertEquals(TEST_COMP_NAME, result.getName());

		assertEquals(2, result.getValue().length);
		assertNotNull(result.getValue()[0].getId());
		LOG.debug("attributeId: " + result.getValue()[0].getId());
		assertEquals(attributeValue1, result.getValue()[0].getMember0());
		assertTrue(result.getValue()[0].isMember1());

		assertEquals("value 10", result.getValue()[1].getMember0());
		assertFalse(result.getValue()[1].isMember1());
		assertNotNull(result.getValue()[1].getId());

		// operate: write compounded attribute
		CompoundedTestClass newValue = result.getValue()[1];
		newValue.setMember0("hello world");
		this.dataClient.setCaptureMessages(true);
		try {
			this.dataClient.setAttributeValue(login, TEST_COMP_NAME, newValue);
		} finally {
			LOG.debug("request message: "
					+ DomTestUtils.domToString(this.dataClient
							.getOutboundMessage()));
		}

		// verify
		result = this.dataClient.getAttributeValue(login, TEST_COMP_NAME,
				CompoundedTestClass[].class);
		assertEquals("hello world", result.getValue()[1].getMember0());
		assertFalse(result.getValue()[1].isMember1());
		assertNotNull(result.getValue()[1].getId());

		// operate: create compound attribute
		CompoundedTestClass newCompoundAttribute = new CompoundedTestClass();
		newCompoundAttribute.setMember0("foobar");
		newCompoundAttribute.setMember1(true);
		this.dataClient.createAttribute(login, TEST_COMP_NAME,
				newCompoundAttribute);

		result = this.dataClient.getAttributeValue(login, TEST_COMP_NAME,
				CompoundedTestClass[].class);
		assertEquals(3, result.getValue().length);

		// check that the SDK can also retrieve compound attributes via maps.
		Attribute<Map[]> mapResult = this.dataClient.getAttributeValue(login,
				TEST_COMP_NAME, Map[].class);
		assertEquals(3, mapResult.getValue().length);

		assertEquals("foobar", result.getValue()[2].getMember0());
		assertTrue(result.getValue()[2].isMember1());
		assertNotNull(result.getValue()[2].getId());

		assertEquals(mapResult.getValue()[0]
				.get(CompoundBuilder.ATTRIBUTE_ID_KEY), result.getValue()[0]
				.getId());

		// operate: remove a compounded attribute record
		this.dataClient.removeAttribute(login, new Attribute(TEST_COMP_NAME,
				result.getValue()[1]));
		// verify
		result = this.dataClient.getAttributeValue(login, TEST_COMP_NAME,
				CompoundedTestClass[].class);
		assertEquals(2, result.getValue().length);
	}

	@Test
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
		userRegistrationService.registerUser(login, password);

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
		applicationService.addApplication(testApplicationName, null, "owner",
				null, false, IdScopeType.USER, null, null, null,
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

	@Test
	public void testDataServiceDateAttribute() throws Exception {
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
		userRegistrationService.registerUser(login, password);

		// operate: register certificate as application trust point
		PkiService pkiService = getPkiService(initialContext);
		IntegrationTestUtils.login("admin", "admin");
		pkiService.addTrustPoint(
				SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
				this.certificate.getEncoded());

		// operate: add date attribute type
		AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
		String attributeName = "test-attribute-name-"
				+ UUID.randomUUID().toString();
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, DatatypeType.DATE, true, true);
		attributeTypeService.add(attributeType);

		// operate: add application with certificate
		ApplicationService applicationService = getApplicationService(initialContext);
		applicationService.addApplication(testApplicationName, null, "owner",
				null, false, IdScopeType.USER, null, null, null,
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

		Attribute<Date> result = this.dataClient.getAttributeValue(login,
				attributeName, Date.class);
		LOG.debug("result date value: " + result.getValue());
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
			// expected: Date is required, not a String.
		}

		// set date attribute value + verify
		Date testDate = new DateMidnight().toDate();
		this.dataClient.setAttributeValue(login, attributeName, testDate);

		result = this.dataClient.getAttributeValue(login, attributeName,
				Date.class);
		LOG.debug("result: " + result.getValue());
		assertEquals(attributeName, result.getName());
		assertEquals(testDate, result.getValue());

		// operate & verify: setting date attribute to null
		this.dataClient.setAttributeValue(login, attributeName, null);
		result = this.dataClient.getAttributeValue(login, attributeName,
				Date.class);
		LOG.debug("result value: " + result.getValue());
		assertNull(result.getValue());
	}

	@Test
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
		userRegistrationService.registerUser(login, password);

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
						null,
						"owner",
						null,
						false,
						IdScopeType.USER,
						null,
						null,
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
		identityService.addAttribute(Collections.singletonList(attribute2));

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
		for (String value : result.getValue())
			LOG.debug("result value: " + value);
		assertEquals(attributeValue1, result.getValue()[0]);
		assertEquals(attributeValue2, result.getValue()[1]);
	}

	@Test
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
		userRegistrationService.registerUser(login, password);

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
		applicationService.addApplication(testApplicationName, null, "owner",
				null, false, IdScopeType.USER, null, null, null,
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

		// operate
		this.dataClient.createAttribute(login, attributeName, Boolean.TRUE);

		// verify
		result = this.dataClient.getAttributeValue(login, attributeName,
				Boolean.class);
		assertNotNull(result);
		assertTrue(result.getValue());

		// operate: set value to FALSE
		this.dataClient.setAttributeValue(login, attributeName, Boolean.FALSE);
		result = this.dataClient.getAttributeValue(login, attributeName,
				Boolean.class);
		assertNotNull(result);
		assertFalse(result.getValue());

		// operate: set value to NULL
		this.dataClient.setAttributeValue(login, attributeName, null);
		result = this.dataClient.getAttributeValue(login, attributeName,
				Boolean.class);
		assertNotNull(result);
		assertNull(result.getValue());

		// operate: remove the attribute
		this.dataClient.removeAttribute(login, result);

		// verify that the attribute no longer exists
		result = this.dataClient.getAttributeValue(login, attributeName,
				Boolean.class);
		assertNull(result);
	}

	public static final String TEST_COMP_NAME = "test-comp-name-6453";

	public static final String TEST_MEMBER_0_NAME = "test-member-0-name-7493";

	public static final String TEST_MEMBER_1_NAME = "test-member-1-name-3298";

	@Compound(TEST_COMP_NAME)
	public static class CompoundedTestClass {
		private String member0;

		private Boolean member1;

		private String id;

		public CompoundedTestClass() {

		}

		@CompoundId
		public String getId() {
			return this.id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@CompoundMember(TEST_MEMBER_0_NAME)
		public String getMember0() {
			return this.member0;
		}

		public void setMember0(String member0) {
			this.member0 = member0;
		}

		@CompoundMember(TEST_MEMBER_1_NAME)
		public Boolean isMember1() {
			return this.member1;
		}

		public void setMember1(Boolean member1) {
			this.member1 = member1;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("member0", this.member0)
					.append("member1", this.member1).toString();
		}
	}
}
