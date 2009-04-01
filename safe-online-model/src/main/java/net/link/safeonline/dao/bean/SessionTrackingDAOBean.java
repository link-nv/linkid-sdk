/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.SessionTrackingDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
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

    private static final Log                      LOG = LogFactory.getLog(SessionTrackingDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                         entityManager;

    private SessionTrackingEntity.QueryInterface  queryObject;
    private SessionAssertionEntity.QueryInterface assertionQueryObject;


    @PostConstruct
    public void postConstructCallback() {

        queryObject = QueryObjectFactory.createQueryObject(entityManager, SessionTrackingEntity.QueryInterface.class);
        assertionQueryObject = QueryObjectFactory.createQueryObject(entityManager, SessionAssertionEntity.QueryInterface.class);
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
}
