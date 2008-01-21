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
import net.link.safeonline.config.dao.bean.ConfigGroupDAOBean;
import net.link.safeonline.config.dao.bean.ConfigItemDAOBean;
import net.link.safeonline.dao.bean.AllowedDeviceDAOBean;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeProviderDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.DeviceClassDAOBean;
import net.link.safeonline.dao.bean.DeviceDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubjectIdentifierDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.dao.bean.UsageAgreementDAOBean;
import net.link.safeonline.device.backend.bean.PasswordManagerBean;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.entity.tasks.SchedulingEntity;
import net.link.safeonline.entity.tasks.TaskEntity;
import net.link.safeonline.entity.tasks.TaskHistoryEntity;
import net.link.safeonline.model.bean.ApplicationIdentityManagerBean;
import net.link.safeonline.model.bean.IdGeneratorBean;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.model.bean.UsageAgreementManagerBean;
import net.link.safeonline.model.beid.BeIdStartableBean;
import net.link.safeonline.model.demo.DemoStartableBean;
import net.link.safeonline.pkix.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.pkix.dao.bean.TrustPointDAOBean;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.tasks.dao.bean.SchedulingDAOBean;
import net.link.safeonline.tasks.dao.bean.TaskDAOBean;
import net.link.safeonline.tasks.dao.bean.TaskHistoryDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DemoStartableBeanTest {

	private EntityTestManager entityTestManager;

	private static Class<?>[] container = new Class[] { SubjectDAOBean.class,
			ApplicationDAOBean.class, SubscriptionDAOBean.class,
			AttributeDAOBean.class, TrustDomainDAOBean.class,
			ApplicationOwnerDAOBean.class, AttributeTypeDAOBean.class,
			ApplicationIdentityDAOBean.class, ConfigGroupDAOBean.class,
			ConfigItemDAOBean.class, TaskDAOBean.class,
			SchedulingDAOBean.class, TaskHistoryDAOBean.class,
			ApplicationIdentityManagerBean.class, TrustPointDAOBean.class,
			AttributeProviderDAOBean.class, DeviceDAOBean.class,
			DeviceClassDAOBean.class, AllowedDeviceDAOBean.class,
			PasswordManagerBean.class, SubjectServiceBean.class,
			SubjectIdentifierDAOBean.class, IdGeneratorBean.class,
			UsageAgreementDAOBean.class, UsageAgreementManagerBean.class };

	@Before
	public void setUp() throws Exception {
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
				DeviceClassEntity.class, RegisteredDeviceEntity.class,
				DeviceDescriptionEntity.class, DevicePropertyEntity.class,
				DeviceClassDescriptionEntity.class, AllowedDeviceEntity.class,
				CompoundedAttributeTypeMemberEntity.class,
				SubjectIdentifierEntity.class, UsageAgreementEntity.class,
				UsageAgreementTextEntity.class);

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class, container,
				entityManager);

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
	public void postStart() throws Exception {
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
