/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class IdentityServiceBeanTest {

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

		JmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");
	}

	@After
	public void tearDown() throws Exception {
		this.entityTestManager.tearDown();
	}

	@Test
	public void confirmation() throws Exception {
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

	@Test
	public void isConfirmationRequiredOnEmptyIdentityGivesFalse()
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

	@Test
	public void removeMultivaluedAttribute() throws Exception {
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

	@Test
	public void missingAttributes() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		String login = "test-login-" + UUID.randomUUID().toString();
		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager, login,
				SafeOnlineRoles.USER_ROLE);
		String applicationName = "test-application-name-"
				+ UUID.randomUUID().toString();

		String ownerLogin = "test-subject-login-"
				+ UUID.randomUUID().toString();

		SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		subjectDAO.addSubject(login);
		SubjectEntity ownerSubject = subjectDAO.addSubject(ownerLogin);

		ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(
				ApplicationOwnerDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		ApplicationOwnerEntity applicationOwner = applicationOwnerDAO
				.addApplicationOwner("test-application-owner", ownerSubject);

		ApplicationDAO applicationDAO = EJBTestUtils.newInstance(
				ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		ApplicationEntity application = applicationDAO.addApplication(
				applicationName, applicationOwner, null, null);

		AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(
				AttributeTypeDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		AttributeTypeEntity optionalAttributeType = new AttributeTypeEntity(
				"test-optional-attribute-type",
				SafeOnlineConstants.STRING_TYPE, true, true);
		attributeTypeDAO.addAttributeType(optionalAttributeType);
		AttributeTypeEntity requiredAttributeType = new AttributeTypeEntity(
				"test-required-attribute-type",
				SafeOnlineConstants.STRING_TYPE, true, true);
		attributeTypeDAO.addAttributeType(requiredAttributeType);
		AttributeTypeEntity compoundedAttributeType = new AttributeTypeEntity(
				"test-compounded-attribute-type",
				SafeOnlineConstants.STRING_TYPE, true, true);
		compoundedAttributeType.addMember(optionalAttributeType, 0);
		compoundedAttributeType.addMember(requiredAttributeType, 1);
		attributeTypeDAO.addAttributeType(compoundedAttributeType);

		ApplicationIdentityDAO applicationIdentityDAO = EJBTestUtils
				.newInstance(ApplicationIdentityDAOBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);
		ApplicationIdentityEntity applicationIdentity = applicationIdentityDAO
				.addApplicationIdentity(application, 0);
		applicationIdentityDAO.addApplicationIdentityAttribute(
				applicationIdentity, requiredAttributeType, true, false);
		applicationIdentityDAO.addApplicationIdentityAttribute(
				applicationIdentity, optionalAttributeType, false, false);
		applicationIdentityDAO.addApplicationIdentityAttribute(
				applicationIdentity, compoundedAttributeType, true, false);

		// operate
		List<AttributeDO> result = identityService.listMissingAttributes(
				applicationName, null);

		// verify
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(requiredAttributeType.getName(), result.get(0).getName());

	}

	private void refreshTransaction(EntityManager entityManager) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.commit();
		transaction.begin();
	}
}
