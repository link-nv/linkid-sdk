/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import javax.persistence.EntityManager;

import net.link.safeonline.dao.bean.CachedOcspResponseDAOBean;
import net.link.safeonline.entity.CachedOcspResponseEntity;

import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class CachedOcspResponseDAOBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	private CachedOcspResponseDAOBean testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.entityTestManager = new EntityTestManager();
		/*
		 * If you add entities to this list, also add them to
		 * safe-online-sql-ddl.
		 */
		this.entityTestManager.setUp(TrustDomainEntity.class,
				CachedOcspResponseEntity.class);

		this.testedInstance = new CachedOcspResponseDAOBean();
		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.getEntityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		this.entityTestManager.tearDown();
		super.tearDown();
	}

	public void testAddRemoveCachedOcspResponse() throws Exception {
		String key = "1234";
		boolean result = true;
		CachedOcspResponseEntity cachedOcspResponse = this.testedInstance
				.addCachedOcspResponse(key, result, null);
		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.refreshEntityManager());
		cachedOcspResponse = this.testedInstance.findCachedOcspResponse(key);
		this.testedInstance.removeCachedOcspResponse(cachedOcspResponse);
		EJBTestUtils.inject(this.testedInstance, this.entityTestManager
				.refreshEntityManager());
		this.testedInstance.addCachedOcspResponse(key, result, null);

	}

	public void testClearOcspCacheExpired() {
		TrustDomainEntity trustDomainExpired = new TrustDomainEntity(
				"trustdomainExpired", true, 0);
		TrustDomainEntity trustDomainNotExpired = new TrustDomainEntity(
				"trustdomainNotExpired", true, System.currentTimeMillis());
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		entityManager.persist(trustDomainExpired);
		entityManager.persist(trustDomainNotExpired);
		String keyExpired = "1234";
		String keyNotExpired = "4321";
		this.testedInstance.addCachedOcspResponse(keyExpired, true,
				trustDomainExpired);
		this.testedInstance.addCachedOcspResponse(keyNotExpired, true,
				trustDomainNotExpired);
		entityManager.flush();

		this.testedInstance
				.clearOcspCacheExpiredForTrustDomain(trustDomainExpired);
		this.testedInstance
				.clearOcspCacheExpiredForTrustDomain(trustDomainNotExpired);
		CachedOcspResponseEntity result = this.testedInstance
				.findCachedOcspResponse(keyExpired);
		assertNull(result);
		result = this.testedInstance.findCachedOcspResponse(keyNotExpired);
		assertNotNull(result);
	}
}
