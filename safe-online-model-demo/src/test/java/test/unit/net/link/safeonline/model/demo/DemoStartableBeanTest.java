/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.demo;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.Startable;
import net.link.safeonline.dao.bean.AllowedDeviceDAOBean;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeProviderDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.dao.bean.SchedulingDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.TaskDAOBean;
import net.link.safeonline.dao.bean.TaskHistoryDAOBean;
import net.link.safeonline.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.dao.bean.TrustPointDAOBean;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.ConfigGroupEntity;
import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SchedulingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.TaskEntity;
import net.link.safeonline.entity.TaskHistoryEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.model.bean.ApplicationIdentityManagerBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.model.beid.BeIdStartableBean;
import net.link.safeonline.model.demo.DemoStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class DemoStartableBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private static Class[] container = new Class[] { SubjectDAOBean.class,
			ApplicationDAOBean.class, SubscriptionDAOBean.class,
			AttributeDAOBean.class, TrustDomainDAOBean.class,
			ApplicationOwnerDAOBean.class, AttributeTypeDAOBean.class,
			ApplicationIdentityDAOBean.class, ConfigGroupDAOBean.class,
			ConfigItemDAOBean.class, TaskDAOBean.class,
			SchedulingDAOBean.class, TaskHistoryDAOBean.class,
			ApplicationIdentityManagerBean.class, TrustPointDAOBean.class,
			AttributeProviderDAOBean.class, DeviceDAOBean.class,
			AllowedDeviceDAOBean.class };

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SubjectEntity.class,
				ApplicationEntity.class, ApplicationOwnerEntity.class,
				AttributeEntity.class, AttributeTypeEntity.class,
				SubscriptionEntity.class, TrustDomainEntity.class,
				ApplicationIdentityEntity.class, ConfigGroupEntity.class,
				ConfigItemEntity.class, SchedulingEntity.class,
				TaskEntity.class, TaskHistoryEntity.class,
				TrustPointEntity.class,
				ApplicationIdentityAttributeEntity.class,
				AttributeTypeDescriptionEntity.class,
				AttributeProviderEntity.class, DeviceEntity.class,
				AllowedDeviceEntity.class);

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class, container,
				entityManager);

		systemStartable.postStart();

		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.commit();
		entityTransaction.begin();
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();

		super.tearDown();
	}

	public void testPostStart() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		DemoStartableBean demoStartableBean = EJBTestUtils.newInstance(
				DemoStartableBean.class, container, entityManager);
		BeIdStartableBean beIdStartableBean = EJBTestUtils.newInstance(
				BeIdStartableBean.class, container, entityManager);

		EJBTestUtils.setJBossPrincipal("test-operator", "operator");

		// operate
		beIdStartableBean.postStart();
		demoStartableBean.postStart();
	}
}
