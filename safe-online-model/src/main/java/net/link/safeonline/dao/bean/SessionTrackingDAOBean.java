/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.SessionTrackingDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.joda.time.DateTime;


@Stateless
@LocalBinding(jndiBinding = SessionTrackingDAO.JNDI_BINDING)
public class SessionTrackingDAOBean implements SessionTrackingDAO {

    private static final Log                           LOG = LogFactory.getLog(SessionTrackingDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                              entityManager;

    private SessionTrackingEntity.QueryInterface       queryObject;
    private SessionAssertionEntity.QueryInterface      assertionQueryObject;
    private SessionAuthnStatementEntity.QueryInterface statementQueryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, SessionTrackingEntity.QueryInterface.class);
        assertionQueryObject = QueryObjectFactory.createQueryObject(entityManager, SessionAssertionEntity.QueryInterface.class);
        statementQueryObject = QueryObjectFactory.createQueryObject(entityManager, SessionAuthnStatementEntity.QueryInterface.class);
    }

    /**
     * {@inheritDoc}
     */
    public SessionTrackingEntity addTracker(ApplicationEntity application, String session, String ssoId,
                                            ApplicationPoolEntity applicationPool) {

        LOG.debug("add session tracker for app=" + application.getName() + " session=" + session + " ssoId=" + ssoId + " pool="
                + applicationPool.getName());
        SessionTrackingEntity tracker = new SessionTrackingEntity(application, session, ssoId, applicationPool);
        entityManager.persist(tracker);
        return tracker;
    }

    /**
     * {@inheritDoc}
     */
    public SessionTrackingEntity findTracker(ApplicationEntity application, String session, String ssoId,
                                             ApplicationPoolEntity applicationPool) {

        LOG.debug("find session tracker for app=" + application.getName() + " session=" + session + " ssoId=" + ssoId + " pool="
                + applicationPool.getName());
        SessionTrackingEntity tracker = queryObject.find(application, session, ssoId, applicationPool);
        return tracker;
    }

    /**
     * {@inheritDoc}
     */
    public SessionAssertionEntity addAssertion(String ssoId, ApplicationPoolEntity applicationPool) {

        LOG.debug("add assertion: ssoId=" + ssoId + " applicationPool=" + applicationPool.getName());
        SessionAssertionEntity assertion = new SessionAssertionEntity(ssoId, applicationPool);
        entityManager.persist(assertion);
        return assertion;
    }

    /**
     * {@inheritDoc}
     */
    public SessionAuthnStatementEntity addAuthnStatement(SessionAssertionEntity assertion, DateTime time, DeviceEntity device) {

        LOG.debug("add session authn statement: time=" + time.toString() + " device=" + device.getName());
        SessionAuthnStatementEntity statement = new SessionAuthnStatementEntity(assertion, time.toDate(), device);
        entityManager.persist(statement);
        return statement;
    }

    /**
     * {@inheritDoc}
     */
    public SessionAssertionEntity findAssertion(String ssoId, ApplicationPoolEntity applicationPool) {

        LOG.debug("find session assertion: ssoId=" + ssoId + " applicationPool=" + applicationPool.getName());
        SessionAssertionEntity assertion = assertionQueryObject.find(ssoId, applicationPool);
        return assertion;
    }

    /**
     * {@inheritDoc}
     */
    public void clearExpired() {

        LOG.debug("clear expired session trackers");
        List<SessionTrackingEntity> trackers = queryObject.listSessionTrackers();
        for (SessionTrackingEntity tracker : trackers) {
            Date now = new Date(System.currentTimeMillis());
            Date expiration = new Date(tracker.getTimestamp().getTime() + tracker.getApplication().getSessionTimeout());
            if (now.after(expiration)) {
                // expired
                removeTracker(tracker);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeAssertions(SubjectEntity subject) {

        LOG.debug("remove sesssion assertions containing subject: " + subject.toString());

        List<SessionAssertionEntity> assertions = assertionQueryObject.listAssertions(subject);
        for (SessionAssertionEntity assertion : assertions) {
            removeStatements(assertion);
            entityManager.flush();
            entityManager.remove(assertion);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void removeStatements(SessionAssertionEntity assertion) {

        LOG.debug("remove session statements from assertion: " + assertion.toString());
        List<SessionAuthnStatementEntity> statements = statementQueryObject.listStatements(assertion);
        for (SessionAuthnStatementEntity statement : statements) {
            LOG.debug("remove session authn statement: " + statement.toString());
            /*
             * Manage relationship
             */
            assertion.getStatements().remove(statement);
            /*
             * Remove from database
             */
            entityManager.remove(statement);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeTrackers(ApplicationEntity application) {

        LOG.debug("remove session trackers for application: " + application.toString());
        List<SessionTrackingEntity> trackers = queryObject.listSessionTrackers(application);
        for (SessionTrackingEntity tracker : trackers) {
            removeTracker(tracker);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeTrackers(ApplicationPoolEntity applicationPool) {

        LOG.debug("remove session trackers for application pool: " + applicationPool.getName());
        List<SessionTrackingEntity> trackers = queryObject.listSessionTrackers(applicationPool);
        for (SessionTrackingEntity tracker : trackers) {
            removeTracker(tracker);
        }
    }

    private void removeTracker(SessionTrackingEntity tracker) {

        LOG.debug("remove session tracker: " + tracker.toString());
        /*
         * Lookup session assertion related to this tracker
         */
        SessionAssertionEntity assertion = assertionQueryObject.find(tracker.getSsoId(), tracker.getApplicationPool());
        if (null != assertion) {
            LOG.debug("remove session assertion: " + assertion.toString());

            removeStatements(assertion);
            entityManager.flush();
            /*
             * Remove from database
             */
            entityManager.remove(assertion);
            entityManager.flush();
            entityManager.remove(tracker);
        }
    }
}
