/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

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
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.IdentityServiceBean;
import net.link.safeonline.authentication.service.bean.SubscriptionServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.HistoryDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.bean.ApplicationIdentityServiceBean;
import net.link.safeonline.model.bean.ApplicationOwnerManagerBean;
import net.link.safeonline.model.bean.SubjectManagerBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IdentityServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private static final Class[] container = new Class[] {
			SubjectDAOBean.class, ApplicationDAOBean.class,
			SubscriptionDAOBean.class, AttributeDAOBean.class,
			TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class,
			AttributeTypeDAOBean.class, ApplicationIdentityDAOBean.class,
			SubjectManagerBean.class, HistoryDAOBean.class,
			ApplicationOwnerManagerBean.class,
			ApplicationIdentityServiceBean.class };

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SubjectEntity.class,
				ApplicationEntity.class, ApplicationOwnerEntity.class,
				AttributeEntity.class, AttributeTypeEntity.class,
				SubscriptionEntity.class, TrustDomainEntity.class,
				ApplicationIdentityEntity.class);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class, container,
				entityManager);
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
				.newInstance(UserRegistrationServiceBean.class, container,
						entityManager);
		userRegistrationService.registerUser(login, password, name);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class, container, entityManager,
				"test-operator", "operator");
		userRegistrationService.registerUser("test-application-owner-login",
				"password", null);
		applicationService.registerApplicationOwner(
				"test-application-owner-name", "test-application-owner-login");
		AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(
				AttributeTypeServiceBean.class, container, entityManager,
				"test-global-operator", "global-operator");
		attributeTypeService.add(new AttributeTypeEntity("test-attribute-type",
				"string", false, false));
		attributeTypeService.add(new AttributeTypeEntity(
				"test-attribute-type-2", "string", false, false));
		applicationService.addApplication(applicationName,
				"test-application-owner-name", null, null,
				new String[] { "test-attribute-type" });
		SubscriptionService subscriptionService = EJBTestUtils.newInstance(
				SubscriptionServiceBean.class, container, entityManager, login,
				"user");
		subscriptionService.subscribe(applicationName);

		EJBTestUtils.setJBossPrincipal("test-application-owner-login", "owner");

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class, container, entityManager, login,
				"user");

		// operate
		boolean result = identityService
				.isConfirmationRequired(applicationName);
		assertTrue(result);

		List<AttributeTypeEntity> attribsToConfirm = identityService
				.getIdentityAttributesToConfirm(applicationName);
		assertEquals(1, attribsToConfirm.size());
		assertEquals("test-attribute-type", attribsToConfirm.get(0).getName());
		identityService.confirmIdentity(applicationName);
		this.entityTestManager.getEntityManager().flush();
		assertFalse(identityService.isConfirmationRequired(applicationName));

		attribsToConfirm = identityService
				.getIdentityAttributesToConfirm(applicationName);
		assertTrue(attribsToConfirm.isEmpty());

		List<AttributeTypeEntity> currentIdentity = applicationService
				.getCurrentApplicationIdentity(applicationName);
		assertEquals(1, currentIdentity.size());
		assertEquals("test-attribute-type", currentIdentity.get(0).getName());

		applicationService
				.updateApplicationIdentity(applicationName, new String[] {
						"test-attribute-type", "test-attribute-type-2" });
		assertTrue(identityService.isConfirmationRequired(applicationName));

		attribsToConfirm = identityService
				.getIdentityAttributesToConfirm(applicationName);
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
				.newInstance(UserRegistrationServiceBean.class, container,
						entityManager);
		userRegistrationService.registerUser(login, password, name);

		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class, container, entityManager,
				"test-operator", "operator");
		userRegistrationService.registerUser("test-application-owner-login",
				"password", null);
		applicationService.registerApplicationOwner(
				"test-application-owner-name", "test-application-owner-login");
		applicationService.addApplication(applicationName,
				"test-application-owner-name", null, null, new String[] {});

		EJBTestUtils.setJBossPrincipal("test-application-owner-login", "owner");

		IdentityService identityService = EJBTestUtils.newInstance(
				IdentityServiceBean.class, container, entityManager, login,
				"user");

		// operate
		boolean result = identityService
				.isConfirmationRequired(applicationName);
		assertFalse(result);
	}
}
