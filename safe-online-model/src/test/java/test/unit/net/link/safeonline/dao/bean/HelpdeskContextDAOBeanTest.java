/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.List;

import junit.framework.TestCase;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.helpdesk.dao.bean.HelpdeskContextDAOBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class HelpdeskContextDAOBeanTest extends TestCase {

    private EntityTestManager      entityTestManager;

    private HelpdeskContextDAOBean testedInstance;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(SafeOnlineTestContainer.entities);

        testedInstance = new HelpdeskContextDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());

        EJBTestUtils.init(testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testContext() {

        HelpdeskContextEntity context = testedInstance.createHelpdeskContext("test-location");
        List<HelpdeskContextEntity> contexts = testedInstance.listContexts();
        assertEquals(context.getId(), contexts.get(0).getId());
    }

}
