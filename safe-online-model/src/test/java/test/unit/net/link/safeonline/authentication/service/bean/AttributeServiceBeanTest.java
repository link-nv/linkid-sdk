/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.AttributeServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class AttributeServiceBeanTest {

	private static final Log LOG = LogFactory
			.getLog(AttributeServiceBeanTest.class);

	private EntityTestManager entityTestManager;

	@Before
	public void setUp() throws Exception {
		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);

		systemStartable.postStart();

		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.commit();
		entityTransaction.begin();
	}

	@After
	public void tearDown() throws Exception {
		this.entityTestManager.tearDown();
	}

	@Test
	public void testGetAttributeFailsIfUserNotSubscribed() throws Exception {
		// setup
		String testSubjectLogin = UUID.randomUUID().toString();
		String testAttributeName = UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(testSubjectLogin, null, null);

		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", "global-operator");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, true);
		attributeTypeService.add(attributeType);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						null,
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										testAttributeName, true, false) }));

		AttributeService attributeService = EJBTestUtils.newInstance(
				AttributeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testApplicationName, "application");

		// operate & verify
		try {
			attributeService.getConfirmedAttributeValue(testSubjectLogin,
					testAttributeName);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	@Test
	public void testGetAttributeFailsIfUserNotConfirmed() throws Exception {
		// setup
		String testSubjectLogin = UUID.randomUUID().toString();
		String testAttributeName = UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(testSubjectLogin, null, null);

		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", "global-operator");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, true);
		attributeTypeService.add(attributeType);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						null,
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										testAttributeName, true, false) }));

		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		subscriptionService.subscribe(testApplicationName);

		AttributeService attributeService = EJBTestUtils.newInstance(
				AttributeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testApplicationName, "application");

		// operate & verify
		try {
			attributeService.getConfirmedAttributeValue(testSubjectLogin,
					testAttributeName);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	@Test
	public void testGetAttribute() throws Exception {
		// setup
		String testSubjectLogin = UUID.randomUUID().toString();
		String testAttributeName = UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();
		String testAttributeValue = UUID.randomUUID().toString();

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(testSubjectLogin, null, null);

		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", "global-operator");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, true);
		attributeTypeService.add(attributeType);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						null,
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										testAttributeName, true, false) }));

		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		subscriptionService.subscribe(testApplicationName);

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		identityService.confirmIdentity(testApplicationName);

		AttributeDO testAttribute = new AttributeDO(testAttributeName,
				DatatypeType.STRING);
		testAttribute.setStringValue(testAttributeValue);
		testAttribute.setEditable(true);
		identityService.saveAttribute(testAttribute);

		AttributeService attributeService = EJBTestUtils.newInstance(
				AttributeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testApplicationName, "application");

		// operate
		Object result = attributeService.getConfirmedAttributeValue(
				testSubjectLogin, testAttributeName);

		// verify
		assertEquals(testAttributeValue.getClass(), String.class);
		assertEquals(testAttributeValue, result);
	}

	@Test
	public void testGetBooleanAttributeValue() throws Exception {
		// setup
		String testSubjectLogin = UUID.randomUUID().toString();
		String testAttributeName = UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();
		Boolean testAttributeValue = Boolean.TRUE;

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(testSubjectLogin, null, null);

		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", "global-operator");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.BOOLEAN, true, true);
		attributeTypeService.add(attributeType);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						null,
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										testAttributeName, true, false) }));

		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		subscriptionService.subscribe(testApplicationName);

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		identityService.confirmIdentity(testApplicationName);

		AttributeDO testAttribute = new AttributeDO(testAttributeName,
				DatatypeType.BOOLEAN);
		testAttribute.setBooleanValue(testAttributeValue);
		testAttribute.setEditable(true);
		identityService.saveAttribute(testAttribute);

		AttributeService attributeService = EJBTestUtils.newInstance(
				AttributeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testApplicationName, "application");

		// operate
		Object result = attributeService.getConfirmedAttributeValue(
				testSubjectLogin, testAttributeName);

		// verify
		assertEquals(result.getClass(), Boolean.class);
		assertEquals(testAttributeValue, result);
	}

	@Test
	public void testGetUnconfirmedAttributeFails() throws Exception {
		// setup
		String testSubjectLogin = UUID.randomUUID().toString();
		String testAttributeName = UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();
		String testAttributeValue = UUID.randomUUID().toString();
		String unconfirmedAttributeName = UUID.randomUUID().toString();

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(testSubjectLogin, null, null);

		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", "global-operator");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, true);
		attributeTypeService.add(attributeType);
		AttributeTypeEntity unconfirmedAttributeType = new AttributeTypeEntity(
				unconfirmedAttributeName, DatatypeType.STRING, true, true);
		attributeTypeService.add(unconfirmedAttributeType);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						null,
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										testAttributeName, true, false) }));

		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		subscriptionService.subscribe(testApplicationName);

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		identityService.confirmIdentity(testApplicationName);

		AttributeDO testAttribute = new AttributeDO(testAttributeName,
				DatatypeType.STRING);
		testAttribute.setStringValue(testAttributeValue);
		identityService.saveAttribute(testAttribute);

		AttributeService attributeService = EJBTestUtils.newInstance(
				AttributeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testApplicationName, "application");

		// operate & verify
		try {
			attributeService.getConfirmedAttributeValue(testSubjectLogin,
					unconfirmedAttributeName);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	@Test
	public void testGetAttributeFailsIfDataMining() throws Exception {
		// setup
		String testSubjectLogin = UUID.randomUUID().toString();
		String testAttributeName = UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();
		String testAttributeValue = UUID.randomUUID().toString();

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(testSubjectLogin, null, null);

		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", "global-operator");
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, true);
		attributeTypeService.add(attributeType);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						null,
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										testAttributeName, true, true) }));

		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		subscriptionService.subscribe(testApplicationName);

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		identityService.confirmIdentity(testApplicationName);

		AttributeDO testAttribute = new AttributeDO(testAttributeName,
				DatatypeType.STRING);
		testAttribute.setStringValue(testAttributeValue);
		identityService.saveAttribute(testAttribute);

		AttributeService attributeService = EJBTestUtils.newInstance(
				AttributeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testApplicationName, "application");

		// operate & verify
		try {
			attributeService.getConfirmedAttributeValue(testSubjectLogin,
					testAttributeName);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}

	}

	@Test
	public void testGetMultivaluedAttribute() throws Exception {
		// setup
		String testSubjectLogin = UUID.randomUUID().toString();
		String testAttributeName = UUID.randomUUID().toString();
		String testApplicationName = UUID.randomUUID().toString();
		String testAttributeValue1 = UUID.randomUUID().toString();
		String testAttributeValue2 = UUID.randomUUID().toString();

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		// register test user
		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(testSubjectLogin, null, null);

		// register multivalued attribute type
		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-admin", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, true);
		attributeType.setMultivalued(true);
		attributeTypeService.add(attributeType);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", SafeOnlineRoles.OPERATOR_ROLE);
		applicationService
				.addApplication(
						testApplicationName,
						"owner",
						null,
						null,
						Arrays
								.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
										testAttributeName, true, false) }));

		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		subscriptionService.subscribe(testApplicationName);

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testSubjectLogin, "user");
		identityService.confirmIdentity(testApplicationName);

		AttributeDO testAttribute = new AttributeDO(testAttributeName,
				DatatypeType.STRING);
		testAttribute.setStringValue(testAttributeValue1);
		testAttribute.setEditable(true);
		identityService.saveAttribute(testAttribute);
		testAttribute.setStringValue(testAttributeValue2);
		identityService.addAttribute(Collections.singletonList(testAttribute));

		AttributeService attributeService = EJBTestUtils.newInstance(
				AttributeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				testApplicationName,
				SafeOnlineApplicationRoles.APPLICATION_ROLE);

		// operate
		Object result = attributeService.getConfirmedAttributeValue(
				testSubjectLogin, testAttributeName);

		// verify
		assertNotNull(result);
		assertEquals(String[].class, result.getClass());
		String[] stringArrayResult = (String[]) result;
		assertTrue(contains(stringArrayResult, testAttributeValue1));
		assertTrue(contains(stringArrayResult, testAttributeValue2));

		// operate
		LOG.debug("operate2");
		Map<String, Object> resultMap = attributeService
				.getConfirmedAttributeValues(testSubjectLogin);

		// verify
		assertNotNull(resultMap);
		assertEquals(1, resultMap.size());
		assertTrue(resultMap.containsKey(testAttributeName));
		result = resultMap.get(testAttributeName);
		assertNotNull(result);
		assertEquals(String[].class, result.getClass());
		stringArrayResult = (String[]) result;
		assertTrue(contains(stringArrayResult, testAttributeValue1));
		assertTrue(contains(stringArrayResult, testAttributeValue2));
	}

	private boolean contains(String[] list, String value) {
		for (String entry : list) {
			if (value.equals(entry)) {
				return true;
			}
		}
		return false;
	}
}
