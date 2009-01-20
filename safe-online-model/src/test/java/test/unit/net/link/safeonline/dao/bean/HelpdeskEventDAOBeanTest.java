/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.dao.bean.HelpdeskContextDAOBean;
import net.link.safeonline.helpdesk.dao.bean.HelpdeskEventDAOBean;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class HelpdeskEventDAOBeanTest extends TestCase {

    private EntityTestManager      entityTestManager;

    private HelpdeskEventDAOBean   eventDAO;
    private HelpdeskContextDAOBean contextDAO;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(SafeOnlineTestContainer.entities);

        eventDAO = new HelpdeskEventDAOBean();
        contextDAO = new HelpdeskContextDAOBean();

        EJBTestUtils.inject(eventDAO, entityTestManager.getEntityManager());
        EJBTestUtils.inject(contextDAO, entityTestManager.getEntityManager());

        EJBTestUtils.init(eventDAO);
        EJBTestUtils.init(contextDAO);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testLogs() {

        HelpdeskContextEntity context = contextDAO.createHelpdeskContext("test-location");
        List<HelpdeskEventEntity> events = new Vector<HelpdeskEventEntity>();
        events.add(new HelpdeskEventEntity(context, new Date(), "test-message-1", "test-principal", LogLevelType.INFO));
        events.add(new HelpdeskEventEntity(context, new Date(), "test-message-2", "test-principal", LogLevelType.ERROR));
        eventDAO.persist(events);

        List<HelpdeskEventEntity> persisted_events = eventDAO.listEvents(context.getId());
        assertEquals(persisted_events.size(), events.size());
    }

}
