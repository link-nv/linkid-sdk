/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationIdentityDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
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
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

public class SystemInitializationStartableBeanTest extends TestCase {

	public void testPostStart() throws Exception {
		// setup
		Class[] container = new Class[] { SubjectDAOBean.class,
				ApplicationDAOBean.class, SubscriptionDAOBean.class,
				AttributeDAOBean.class, TrustDomainDAOBean.class,
				ApplicationOwnerDAOBean.class, AttributeTypeDAOBean.class,
				ApplicationIdentityDAOBean.class };

		EntityTestManager entityTestManager = new EntityTestManager();
		entityTestManager.setUp(SubjectEntity.class, ApplicationEntity.class,
				ApplicationOwnerEntity.class, AttributeEntity.class,
				AttributeTypeEntity.class, SubscriptionEntity.class,
				TrustDomainEntity.class, ApplicationIdentityEntity.class);
		EntityManager entityManager = entityTestManager.getEntityManager();

		Startable testedInstance = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class, container,
				entityManager);

		// operate
		testedInstance.postStart();
		entityTestManager.tearDown();
	}
}
