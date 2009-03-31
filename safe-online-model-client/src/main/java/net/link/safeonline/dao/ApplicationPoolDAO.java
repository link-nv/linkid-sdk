/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;


/**
 * Application pool entity data access object interface definition.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface ApplicationPoolDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ApplicationPoolDAOBean/local";


    /**
     * Find the application pool for a given application pool name.
     * 
     * @param applicationPoolName
     *            the application pool name.
     * @return the application pool or <code>null</code> if not found.
     */
    ApplicationPoolEntity findApplicationPool(String applicationPoolName);

    /**
     * Gives back the application pool entity for a given application pool name.
     * 
     * @param applicationPoolName
     * @throws ApplicationPoolNotFoundException
     *             in case the application pool was not found.
     */
    ApplicationPoolEntity getApplicationPool(String applicationPoolName)
            throws ApplicationPoolNotFoundException;

    ApplicationPoolEntity addApplicationPool(String applicationPoolName, long ssoTimeout);

    /**
     * Gives back a list of all application pools registered within the SafeOnline system.
     * 
     */
    List<ApplicationPoolEntity> listApplicationPools();

    /**
     * Gives back a list of all common application pools between the 2 specified applications.
     * 
     */
    List<ApplicationPoolEntity> listCommonApplicationPools(ApplicationEntity application1, ApplicationEntity application2);

    List<ApplicationPoolEntity> listApplicationPools(ApplicationEntity application);

    void removeApplicationPool(ApplicationPoolEntity applicationPool);

}
