/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;

import org.joda.time.DateTime;


/**
 * {@link SessionTrackingEntity} data access object interface definition.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface SessionTrackingDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "SessionTrackingDAOBean/local";


    SessionTrackingEntity findTracker(ApplicationEntity application, String session, String ssoId, ApplicationPoolEntity applicationPool);

    SessionTrackingEntity addTracker(ApplicationEntity application, String session, String ssoId, ApplicationPoolEntity applicationPool);

    SessionAssertionEntity findAssertion(String ssoId, ApplicationPoolEntity applicationPool);

    SessionAssertionEntity findAssertion(SessionTrackingEntity tracker);

    SessionAssertionEntity addAssertion(String ssoId, ApplicationPoolEntity applicationPool);

    SessionAuthnStatementEntity addAuthnStatement(SessionAssertionEntity assertion, DateTime time, DeviceEntity device);

    /**
     * Clears all expired session trackers and the session assertions related to these.
     */
    void clearExpired();

    void removeAssertions(SubjectEntity subject);

    void removeStatements(SessionAssertionEntity assertion);

    void removeTrackers(ApplicationEntity application);

    void removeTrackers(ApplicationPoolEntity applicationPool);

    List<SessionTrackingEntity> listTrackers(ApplicationEntity application, String session, ApplicationPoolEntity applicationPool);

    List<SessionTrackingEntity> listTrackers(ApplicationEntity application, String session);

    List<SessionAuthnStatementEntity> listStatements(SessionAssertionEntity assertion);

}
