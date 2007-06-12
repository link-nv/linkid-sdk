/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.dao.bean;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.test.util.EntityTestManager;

public class TrustDomainDAOBeanTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(TrustDomainDAOBeanTest.class);

	private TrustDomainDAO testedInstance;

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(TrustDomainEntity.class);

		this.testedInstance = this.entityTestManager
				.newInstance(TrustDomainDAOBean.class);
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
		try {
			this.testedInstance.removeTrustDomain(resultTrustDomain);
			fail();
		} catch (IllegalArgumentException e) {
			LOG.debug("expected exception: " + e.getMessage());
			/*
			 * Because the entity test manager newInstance method implements the
			 * REQUIRES_NEW transaction semantics, we get an exception that we
			 * try to remove a detached entity.
			 */
		}
	}
}
