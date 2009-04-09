/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.dao.bean;

import java.util.Collections;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.dao.bean.SessionTrackingDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

import org.joda.time.DateTime;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class SessionTrackingDAOBeanTest extends TestCase {

    private EntityTestManager      entityTestManager;

    private SessionTrackingDAOBean testedInstance;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();
        entityTestManager = new EntityTestManager();
        /*
         * If you add entities to this list, also add them to safe-online-sql-ddl.
         */
        entityTestManager.setUp(SafeOnlineTestContainer.entities);

        testedInstance = new SessionTrackingDAOBean();

        EJBTestUtils.inject(testedInstance, entityTestManager.getEntityManager());

        EJBTestUtils.init(testedInstance);
    }

    @Override
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        super.tearDown();
    }

    public void testClearExpired() {

        // setup
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-pool-name", Long.MAX_VALUE);
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("test-owner", subject);
        ApplicationEntity application = new ApplicationEntity();
        application.setApplicationOwner(owner);
        application.setName("test-application-name");
        application.setSessionTimeout(0);
        applicationPool.getApplications().add(application);

        DeviceEntity device = new DeviceEntity();
        device.setName("test-device");

        String session = UUID.randomUUID().toString();
        String ssoId = UUID.randomUUID().toString();

        // operate
        entityTestManager.getEntityManager().persist(subject);
        entityTestManager.getEntityManager().persist(owner);
        entityTestManager.getEntityManager().persist(application);
        entityTestManager.getEntityManager().persist(applicationPool);
        application.setApplicationPools(Collections.singletonList(applicationPool));

        SessionTrackingEntity tracker = testedInstance.addTracker(application, session, ssoId, applicationPool);
        SessionAssertionEntity assertion = testedInstance.addAssertion(ssoId, applicationPool);
        SessionAuthnStatementEntity statement = testedInstance.addAuthnStatement(assertion, new DateTime(), device);

        entityTestManager.getEntityManager().getTransaction().commit();
        entityTestManager.getEntityManager().getTransaction().begin();

        SessionTrackingEntity resultTracker = entityTestManager.getEntityManager().find(SessionTrackingEntity.class, tracker.getId());
        assertEquals(tracker, resultTracker);
        SessionAssertionEntity resultAssertion = entityTestManager.getEntityManager().find(SessionAssertionEntity.class, assertion.getId());
        assertEquals(assertion, resultAssertion);
        SessionAuthnStatementEntity resultStatement = entityTestManager.getEntityManager().find(SessionAuthnStatementEntity.class,
                statement.getId());
        assertEquals(statement, resultStatement);

        testedInstance.clearExpired();

        entityTestManager.refreshEntityManager();

        resultTracker = entityTestManager.getEntityManager().find(SessionTrackingEntity.class, tracker.getId());
        assertNull(resultTracker);
        resultAssertion = entityTestManager.getEntityManager().find(SessionAssertionEntity.class, assertion.getId());
        assertNull(resultAssertion);
        resultStatement = entityTestManager.getEntityManager().find(SessionAuthnStatementEntity.class, statement.getId());
        assertNull(resultStatement);
    }

    public void testClearExpiredNoneFound() {

        // setup
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-pool-name", Long.MAX_VALUE);
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());
        ApplicationOwnerEntity owner = new ApplicationOwnerEntity("test-owner", subject);
        ApplicationEntity application = new ApplicationEntity();
        application.setApplicationOwner(owner);
        application.setName("test-application-name");
        application.setSessionTimeout(1000 * 60 * 60 * 24);
        applicationPool.getApplications().add(application);

        DeviceEntity device = new DeviceEntity();
        device.setName("test-device");

        String session = UUID.randomUUID().toString();
        String ssoId = UUID.randomUUID().toString();

        // operate
        entityTestManager.getEntityManager().persist(subject);
        entityTestManager.getEntityManager().persist(owner);
        entityTestManager.getEntityManager().persist(application);
        entityTestManager.getEntityManager().persist(applicationPool);
        application.setApplicationPools(Collections.singletonList(applicationPool));

        SessionTrackingEntity tracker = testedInstance.addTracker(application, session, ssoId, applicationPool);
        SessionAssertionEntity assertion = testedInstance.addAssertion(ssoId, applicationPool);
        SessionAuthnStatementEntity statement = testedInstance.addAuthnStatement(assertion, new DateTime(), device);

        entityTestManager.getEntityManager().getTransaction().commit();
        entityTestManager.getEntityManager().getTransaction().begin();

        SessionTrackingEntity resultTracker = entityTestManager.getEntityManager().find(SessionTrackingEntity.class, tracker.getId());
        assertEquals(tracker, resultTracker);
        SessionAssertionEntity resultAssertion = entityTestManager.getEntityManager().find(SessionAssertionEntity.class, assertion.getId());
        assertEquals(assertion, resultAssertion);
        SessionAuthnStatementEntity resultStatement = entityTestManager.getEntityManager().find(SessionAuthnStatementEntity.class,
                statement.getId());
        assertEquals(statement, resultStatement);

        testedInstance.clearExpired();

        entityTestManager.refreshEntityManager();

        resultTracker = entityTestManager.getEntityManager().find(SessionTrackingEntity.class, tracker.getId());
        assertEquals(tracker, resultTracker);
        resultAssertion = entityTestManager.getEntityManager().find(SessionAssertionEntity.class, assertion.getId());
        assertEquals(assertion, resultAssertion);
        resultStatement = entityTestManager.getEntityManager().find(SessionAuthnStatementEntity.class, statement.getId());
        assertEquals(statement, resultStatement);
    }
}
