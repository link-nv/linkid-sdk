/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationPoolException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.ApplicationPoolEntity;


/**
 * Interface to service for retrieving information about application pools.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface ApplicationPoolService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ApplicationPoolServiceBean/local";


    /**
     * Gives back all available applications pools.
     * 
     */
    List<ApplicationPoolEntity> listApplicationPools();

    /**
     * Gives back the application pool entity for a given application pool name.
     * 
     * @param applicationPoolName
     * @throws ApplicationPoolNotFoundException
     */
    ApplicationPoolEntity getApplicationPool(String applicationPoolName)
            throws ApplicationPoolNotFoundException;

    /**
     * /**
     * 
     * @param name
     * @param ssoTimeout
     *            Single Sign-On timeout for this pool
     * @param applicationList
     *            list of applications belonging to this pool
     * @throws ExistingApplicationPoolException
     * @throws ApplicationNotFoundException
     * 
     */
    ApplicationPoolEntity addApplicationPool(String name, Long ssoTimeout, List<String> applicationList)
            throws ExistingApplicationPoolException, ApplicationNotFoundException;

    /**
     * Removes an application pool.
     * 
     * @param name
     * @throws ApplicationPoolNotFoundException
     */
    void removeApplicationPool(String name)
            throws ApplicationPoolNotFoundException, PermissionDeniedException;

    /**
     * Sets the Single Sign-On timeout for this application pool.
     * 
     * @throws ApplicationPoolNotFoundException
     */
    void setSsoTimeout(String applicationPoolName, Long ssoTimeout)
            throws ApplicationPoolNotFoundException;

    /**
     * Sets the application list of this pool.
     * 
     * @throws ApplicationPoolNotFoundException
     * @throws ApplicationNotFoundException
     */
    void updateApplicationList(String applicationPoolName, List<String> applicationNameList)
            throws ApplicationPoolNotFoundException, ApplicationNotFoundException;

}
