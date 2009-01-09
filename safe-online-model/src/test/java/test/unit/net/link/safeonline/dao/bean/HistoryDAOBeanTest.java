/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.bean.HistoryDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.HistoryPropertyEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;


public class HistoryDAOBeanTest extends TestCase {

    private EntityTestManager entityTestManager;

    private HistoryDAOBean    testedInstance;

    private SubjectDAOBean    subjectDAO;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(HistoryEntity.class, HistoryPropertyEntity.class, SubjectEntity.class);

        testedInstance = new HistoryDAOBean();
        subjectDAO = new SubjectDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());
        EJBTestUtils.inject(subjectDAO, entityTestManager.getEntityManager());

        EJBTestUtils.init(testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testHistoryDAO() {

        SubjectEntity subject = subjectDAO.addSubject("testsubject");

        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY, "test-application");
        historyProperties.put(SafeOnlineConstants.DEVICE_PROPERTY, "test-device");
        HistoryEntity history = testedInstance.addHistoryEntry(subject, HistoryEventType.LOGIN_SUCCESS, historyProperties);

        entityTestManager.getEntityManager().getTransaction().commit();
        entityTestManager.getEntityManager().getTransaction().begin();

        HistoryEntity resultHistory = entityTestManager.getEntityManager().find(HistoryEntity.class, history.getId());
        assertEquals(history, resultHistory);

        testedInstance.clearAllHistory(subject);

        entityTestManager.refreshEntityManager();

        resultHistory = entityTestManager.getEntityManager().find(HistoryEntity.class, history.getId());
        assertNull(resultHistory);
    }
}
