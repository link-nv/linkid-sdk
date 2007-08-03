/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.dao.bean;

import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class TrustDomainDAOBeanTest extends TestCase {

	private TrustDomainDAO testedInstance;

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(TrustDomainEntity.class);

		this.testedInstance = EJBTestUtils.newInstance(
				TrustDomainDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				this.entityTestManager.getEntityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testAddAndRemoveTrustDomain() throws Exception {
		// setup
		String name = UUID.randomUUID().toString();

		// operate & verify
		this.testedInstance.addTrustDomain(name, true);
		TrustDomainEntity resultTrustDomain = this.testedInstance
				.findTrustDomain(name);
		assertNotNull(resultTrustDomain);
		this.testedInstance.removeTrustDomain(resultTrustDomain);
	}
}
