/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.dao.bean;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.bean.TrustDomainDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class TrustDomainDAOBeanTest {

    private TrustDomainDAO    testedInstance;

    private EntityTestManager entityTestManager;


    @Before
    public void setUp()
            throws Exception {

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(TrustDomainEntity.class);

        testedInstance = EJBTestUtils.newInstance(TrustDomainDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityTestManager.getEntityManager());
    }

    @After
    public void tearDown()
            throws Exception {

        entityTestManager.tearDown();
    }

    @Test
    public void testAddAndRemoveTrustDomain()
            throws Exception {

        // setup
        String name = UUID.randomUUID().toString();

        // operate & verify
        testedInstance.addTrustDomain(name, true);
        TrustDomainEntity resultTrustDomain = testedInstance.findTrustDomain(name);
        assertNotNull(resultTrustDomain);
        testedInstance.removeTrustDomain(resultTrustDomain);
    }
}
