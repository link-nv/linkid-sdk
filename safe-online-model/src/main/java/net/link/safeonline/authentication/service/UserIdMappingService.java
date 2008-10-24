/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;


@Local
public interface UserIdMappingService {

    /**
     * Returns the application specific user id as specified in the application's id scope.
     * 
     * @param applicationName
     * @param userId
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     */
    String getApplicationUserId(String applicationName, String userId) throws ApplicationNotFoundException, SubscriptionNotFoundException;

    /**
     * Returns the SafeOnline global user id using the application's id scope settings and provided application id. Returns null if not
     * found.
     * 
     * @param applicationName
     * @param applicationUserId
     * @throws ApplicationNotFoundException
     */
    String findUserId(String applicationName, String applicationUserId) throws ApplicationNotFoundException;
}
