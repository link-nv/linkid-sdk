/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
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
import net.link.safeonline.model.bean.ApplicationOwnerManagerBean;
import net.link.safeonline.model.bean.SubjectManagerBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

public class ApplicationServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private static final Class[] container = new Class[] {
			SubjectDAOBean.class, ApplicationDAOBean.class,
			SubscriptionDAOBean.class, AttributeDAOBean.class,
			TrustDomainDAOBean.class, ApplicationOwnerDAOBean.class,
			AttributeTypeDAOBean.class, ApplicationIdentityDAOBean.class,
			SubjectManagerBean.class, HistoryDAOBean.class,
			ApplicationOwnerManagerBean.class };

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

		EJBTestUtils.setJBossPrincipal("test-operator", "operator");

		// operate
		ApplicationService applicationService = EJBTestUtils.newInstance(
				ApplicationServiceBean.class, container, entityManager,
				"test-operator", SafeOnlineRoles.OPERATOR_ROLE);
		List<AttributeTypeEntity> result = applicationService
				.getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);
		assertTrue(result.isEmpty());
		String[] applicationIdentityAttributeTypeNames = new String[] { SafeOnlineConstants.NAME_ATTRIBUTE };
		applicationService.updateApplicationIdentity(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
				applicationIdentityAttributeTypeNames);
		result = applicationService
				.getCurrentApplicationIdentity(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

		// verify
		assertEquals(1, result.size());
		assertEquals(SafeOnlineConstants.NAME_ATTRIBUTE, result.get(0)
				.getName());
	}
}
