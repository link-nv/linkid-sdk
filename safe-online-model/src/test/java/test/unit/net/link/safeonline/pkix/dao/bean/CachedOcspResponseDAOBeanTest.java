/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.dao.bean;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.CachedOcspResultType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.dao.bean.CachedOcspResponseDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class CachedOcspResponseDAOBeanTest extends TestCase {

    private EntityTestManager         entityTestManager;

    private CachedOcspResponseDAOBean testedInstance;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(TrustDomainEntity.class, CachedOcspResponseEntity.class);

        testedInstance = EJBTestUtils.newInstance(CachedOcspResponseDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityTestManager.getEntityManager());
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testAddRemoveCachedOcspResponse()
            throws Exception {

        String key = "1234";
        CachedOcspResultType result = CachedOcspResultType.GOOD;
        CachedOcspResponseEntity cachedOcspResponse = testedInstance.addCachedOcspResponse(key, result, null);
        cachedOcspResponse = testedInstance.findCachedOcspResponse(key);
        testedInstance.removeCachedOcspResponse(cachedOcspResponse);

        testedInstance = EJBTestUtils.newInstance(CachedOcspResponseDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityTestManager.refreshEntityManager());

        testedInstance.addCachedOcspResponse(key, result, null);
    }

    public void testClearOcspCacheExpired() {

        TrustDomainEntity trustDomainExpired = new TrustDomainEntity("trustdomainExpired", true, 0);
        TrustDomainEntity trustDomainNotExpired = new TrustDomainEntity("trustdomainNotExpired", true, System.currentTimeMillis());
        EntityManager entityManager = entityTestManager.getEntityManager();
        entityManager.persist(trustDomainExpired);
        entityManager.persist(trustDomainNotExpired);
        String keyExpired = "1234";
        String keyNotExpired = "4321";
        testedInstance.addCachedOcspResponse(keyExpired, CachedOcspResultType.GOOD, trustDomainExpired);
        testedInstance.addCachedOcspResponse(keyNotExpired, CachedOcspResultType.GOOD, trustDomainNotExpired);
        entityManager.flush();

        testedInstance.clearOcspCacheExpiredForTrustDomain(trustDomainExpired);
        testedInstance.clearOcspCacheExpiredForTrustDomain(trustDomainNotExpired);
        CachedOcspResponseEntity result = testedInstance.findCachedOcspResponse(keyExpired);
        assertNull(result);
        result = testedInstance.findCachedOcspResponse(keyNotExpired);
        assertNotNull(result);
    }
}
