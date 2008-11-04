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
        this.entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        this.entityTestManager.setUp(HistoryEntity.class, HistoryPropertyEntity.class, SubjectEntity.class);

        this.testedInstance = new HistoryDAOBean();
        this.subjectDAO = new SubjectDAOBean();

        EJBTestUtils.inject(this.testedInstance, this.entityTestManager.getEntityManager());
        EJBTestUtils.inject(this.subjectDAO, this.entityTestManager.getEntityManager());

        EJBTestUtils.init(this.testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        this.entityTestManager.tearDown();
        super.tearDown();
    }

    public void testHistoryDAO() {

        SubjectEntity subject = this.subjectDAO.addSubject("testsubject");

        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY, "test-application");
        historyProperties.put(SafeOnlineConstants.DEVICE_PROPERTY, "test-device");
        HistoryEntity history = this.testedInstance.addHistoryEntry(subject, HistoryEventType.LOGIN_SUCCESS, historyProperties);

        this.entityTestManager.getEntityManager().getTransaction().commit();
        this.entityTestManager.getEntityManager().getTransaction().begin();

        HistoryEntity resultHistory = this.entityTestManager.getEntityManager().find(HistoryEntity.class, history.getId());
        assertEquals(history, resultHistory);

        this.testedInstance.clearAllHistory(subject);

        this.entityTestManager.refreshEntityManager();

        resultHistory = this.entityTestManager.getEntityManager().find(HistoryEntity.class, history.getId());
        assertNull(resultHistory);
    }
}
