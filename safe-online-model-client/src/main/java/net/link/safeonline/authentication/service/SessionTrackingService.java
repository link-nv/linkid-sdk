/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;


/**
 * <h2>{@link SessionTrackingService}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * Session Tracking service used by the OLAS session tracking WS.
 * </p>
 * 
 * <p>
 * <i>Apr 2, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Local
public interface SessionTrackingService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "SessionTrackingServiceBean/local";


    /**
     * Returns list of assertions given the specified session. Application determined as the application that signed the request and logged
     * into the WS. Subject and list of application pools are optional. If no application pool is specified and the application has multiple
     * application pools, multiple assertions, one for each pool can be returned.
     * 
     * @param session
     * @param applicationUserId
     *            optional subject ID, as in application user ID
     * @param applicationPools
     *            optional list of application pool names
     * @throws SubjectNotFoundException
     * @throws ApplicationPoolNotFoundException
     */
    List<SessionAssertionEntity> getAssertions(String session, String applicationUserId, List<String> applicationPoolNames)
            throws SubjectNotFoundException, ApplicationPoolNotFoundException;

}
