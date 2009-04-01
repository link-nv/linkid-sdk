/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
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


    /**
     * TODO
     */
    SessionTrackingEntity findTracker(ApplicationEntity application, String session, String ssoId, ApplicationPoolEntity applicationPool);

    /**
     * TODO
     */
    SessionTrackingEntity addTracker(ApplicationEntity application, String session, String ssoId, ApplicationPoolEntity applicationPool);

    /**
     * TODO
     */
    SessionAssertionEntity findAssertion(String ssoId, ApplicationPoolEntity applicationPool);

    /**
     * TODO
     */
    SessionAssertionEntity addAssertion(String ssoId, ApplicationPoolEntity applicationPool);

    /**
     * TODO
     * 
     * @param assertion
     */
    SessionAuthnStatementEntity addAuthnStatement(SessionAssertionEntity assertion, DateTime time, DeviceEntity device);
}
