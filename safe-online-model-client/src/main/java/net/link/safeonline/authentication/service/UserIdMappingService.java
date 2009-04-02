/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;


@Local
public interface UserIdMappingService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "UserIdMappingServiceBean/local";


    /**
     * Returns the application specific user id as specified in the application's id scope.
     * 
     * @param applicationId
     * @param userId
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     */
    String getApplicationUserId(long applicationId, String userId)
            throws ApplicationNotFoundException, SubscriptionNotFoundException;

    /**
     * Returns the SafeOnline global user id using the application's id scope settings and provided application id. Returns null if not
     * found.
     * 
     * @param applicationName
     * @param applicationUserId
     * @throws ApplicationNotFoundException
     */
    String findUserId(long applicationId, String applicationUserId)
            throws ApplicationNotFoundException;

    /**
     * Returns the SafeOnline global user id using the application's id scope settings and provided application id. Returns null if not
     * found.
     * 
     * @param application
     * @param applicationUserId
     */
    String findUserId(ApplicationEntity application, String applicationUserId);
}
