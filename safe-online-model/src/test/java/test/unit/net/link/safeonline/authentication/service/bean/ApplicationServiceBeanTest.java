/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class ApplicationServiceBeanTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(ApplicationServiceBeanTest.class);

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
		this.entityTestManager.refreshEntityManager();
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testApplicationIdentityUseCase() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		// operate
		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager,
				"test-operator", SafeOnlineRoles.OPERATOR_ROLE);
		List<ApplicationIdentityAttributeEntity> result = applicationService
				.getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

		// verify
		assertTrue(result.isEmpty());

		// operate
		IdentityAttributeTypeDO[] applicationIdentityAttributes = new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
				SafeOnlineConstants.NAME_ATTRIBUTE, false) };
		LOG.debug("---------- UPDATING APPLICATION IDENTITY ----------");
		applicationService.updateApplicationIdentity(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, Arrays
						.asList(applicationIdentityAttributes));
		result = applicationService
				.getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

		// verify
		assertEquals(1, result.size());
		assertEquals(SafeOnlineConstants.NAME_ATTRIBUTE, result.get(0)
				.getAttributeTypeName());
		assertFalse(result.get(0).isRequired());

		// operate
		applicationIdentityAttributes = new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
				SafeOnlineConstants.NAME_ATTRIBUTE, true) };
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();
		applicationService.updateApplicationIdentity(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, Arrays
						.asList(applicationIdentityAttributes));
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();
		result = applicationService
				.getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

		// verify
		assertEquals(1, result.size());
		assertEquals(SafeOnlineConstants.NAME_ATTRIBUTE, result.get(0)
				.getAttributeTypeName());
		assertTrue(result.get(0).isRequired());
	}
}
