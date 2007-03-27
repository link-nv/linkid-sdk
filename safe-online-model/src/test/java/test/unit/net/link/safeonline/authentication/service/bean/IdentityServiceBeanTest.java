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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

		MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
		ObjectName jaasManagerName = new ObjectName(
				"jboss.security:service=JaasSecurityManager");
		Object jaasManager = new TestDynamicMBean();
		mbeanServer.registerMBean(jaasManager, jaasManagerName);
	}

	public static class TestDynamicMBean implements DynamicMBean {

		private static final Log LOG = LogFactory
				.getLog(TestDynamicMBean.class);

		public Object getAttribute(String attribute)
				throws AttributeNotFoundException, MBeanException,
				ReflectionException {
			return null;
		}

		public AttributeList getAttributes(String[] attributes) {
			return null;
		}

		public MBeanInfo getMBeanInfo() {
			return new MBeanInfo(this.getClass().getName(), "test", null, null,
					null, null);
		}

		public Object invoke(String actionName, Object[] params,
				String[] signature) throws MBeanException, ReflectionException {
			LOG.debug("invoked");
			return null;
		}

		public void setAttribute(Attribute attribute)
				throws AttributeNotFoundException,
				InvalidAttributeValueException, MBeanException,
				ReflectionException {
		}

		public AttributeList setAttributes(AttributeList attributes) {
			return null;
		}

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
								"test-attribute-type", true)));
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

		List<AttributeTypeEntity> attribsToConfirm = identityService
				.listIdentityAttributesToConfirm(applicationName);
		assertEquals(1, attribsToConfirm.size());
		assertEquals("test-attribute-type", attribsToConfirm.get(0).getName());
		identityService.confirmIdentity(applicationName);
		this.entityTestManager.getEntityManager().flush();
		assertFalse(identityService.isConfirmationRequired(applicationName));

		attribsToConfirm = identityService
				.listIdentityAttributesToConfirm(applicationName);
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

		attribsToConfirm = identityService
				.listIdentityAttributesToConfirm(applicationName);
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
}
