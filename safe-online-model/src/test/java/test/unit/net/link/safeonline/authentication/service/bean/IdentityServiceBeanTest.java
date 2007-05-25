/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class IdentityServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		systemStartable.postStart();

		JmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testConfirmation() throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String name = "test-name";
		String applicationName = "test-application";
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationServiceBean userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(login, password, name);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		userRegistrationService.registerUser("test-application-owner-login",
				"password", null);
		applicationService.registerApplicationOwner(
				"test-application-owner-name", "test-application-owner-login");
		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-global-operator", "global-operator");
		attributeTypeService.add(new AttributeTypeEntity("test-attribute-type",
				"string", false, false));
		attributeTypeService.add(new AttributeTypeEntity(
				"test-attribute-type-2", "string", false, false));
		applicationService.addApplication(applicationName,
				"test-application-owner-name", null, null, Collections
						.singletonList(new IdentityAttributeTypeDO(
								"test-attribute-type", true, false)));
		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, login,
				"user");
		subscriptionService.subscribe(applicationName);

		EJBTestUtils.setJBossPrincipal("test-application-owner-login", "owner");

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, login,
				"user");

		// operate
		boolean result = identityService
				.isConfirmationRequired(applicationName);
		assertTrue(result);

		List<AttributeDO> attribsToConfirm = identityService
				.listIdentityAttributesToConfirm(applicationName, Locale
						.getDefault());
		assertEquals(1, attribsToConfirm.size());
		assertEquals("test-attribute-type", attribsToConfirm.get(0).getName());
		identityService.confirmIdentity(applicationName);
		this.entityTestManager.getEntityManager().flush();
		assertFalse(identityService.isConfirmationRequired(applicationName));

		attribsToConfirm = identityService.listIdentityAttributesToConfirm(
				applicationName, Locale.getDefault());
		assertTrue(attribsToConfirm.isEmpty());

		List<ApplicationIdentityAttributeEntity> currentIdentity = applicationService
				.getCurrentApplicationIdentity(applicationName);
		assertEquals(1, currentIdentity.size());
		assertEquals("test-attribute-type", currentIdentity.get(0)
				.getAttributeTypeName());

		applicationService
				.updateApplicationIdentity(applicationName, Arrays
						.asList(new IdentityAttributeTypeDO[] {
								new IdentityAttributeTypeDO(
										"test-attribute-type"),
								new IdentityAttributeTypeDO(
										"test-attribute-type-2") }));
		assertTrue(identityService.isConfirmationRequired(applicationName));

		attribsToConfirm = identityService.listIdentityAttributesToConfirm(
				applicationName, Locale.getDefault());
		assertEquals(1, attribsToConfirm.size());
		assertEquals("test-attribute-type-2", attribsToConfirm.get(0).getName());
		identityService.confirmIdentity(applicationName);
		assertFalse(identityService.isConfirmationRequired(applicationName));
	}

	public void testIsConfirmationRequiredOnEmptyIdentityGivesFalse()
			throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String name = "test-name";
		String applicationName = "test-application";
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(login, password, name);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", "operator");
		userRegistrationService.registerUser("test-application-owner-login",
				"password", null);
		applicationService.registerApplicationOwner(
				"test-application-owner-name", "test-application-owner-login");
		applicationService.addApplication(applicationName,
				"test-application-owner-name", null, null,
				new LinkedList<IdentityAttributeTypeDO>());

		EJBTestUtils.setJBossPrincipal("test-application-owner-login", "owner");

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, login,
				"user");

		// operate
		boolean result = identityService
				.isConfirmationRequired(applicationName);
		assertFalse(result);
	}

	public void testRemoveMultivaluedAttribute() throws Exception {
		// setup
		String login = "test-login";
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		// operate: register the test user
		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		userRegistrationService.registerUser(login, "test-password", null);

		// operate
		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, login,
				SafeOnlineRoles.USER_ROLE);

		// operate: add multivalued attribute type
		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, login,
				SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
		String attributeName = "test-attribute-name";
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				attributeName, SafeOnlineConstants.STRING_TYPE, true, true);
		attributeType.setMultivalued(true);
		attributeTypeService.add(attributeType);

		refreshTransaction(entityManager);

		// operate: save an attribute
		AttributeDO attribute = new AttributeDO(attributeName,
				SafeOnlineConstants.STRING_TYPE, true, 0, null, null, true,
				true, "value 1", null);
		identityService.saveAttribute(attribute);

		refreshTransaction(entityManager);

		// operate: remove a single multi-valued attribute
		identityService.removeAttribute(attribute);

		refreshTransaction(entityManager);

		// operate: save 2 multivalued attributes
		identityService.saveAttribute(attribute);
		AttributeDO attribute2 = new AttributeDO(attributeName,
				SafeOnlineConstants.STRING_TYPE, true, 1, null, null, true,
				true, "value 2", null);
		identityService.saveAttribute(attribute2);

		refreshTransaction(entityManager);

		// operate: remove first attribute
		identityService.removeAttribute(attribute);

		refreshTransaction(entityManager);

		// verify: the remaining attribute should have index 0 and value 'value
		// 2'.
		AttributeDAO attributeDAO = EJBTestUtils.newInstance(
				AttributeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		SubjectEntity subject = subjectDAO.getSubject(login);
		List<AttributeEntity> resultAttributes = attributeDAO.listAttributes(
				subject, attributeType);

		assertEquals(1, resultAttributes.size());
		AttributeEntity resultAttribute = resultAttributes.get(0);
		assertEquals(0, resultAttribute.getAttributeIndex());
		assertEquals("value 2", resultAttribute.getStringValue());
	}

	private void refreshTransaction(EntityManager entityManager) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.commit();
		transaction.begin();
	}
}
