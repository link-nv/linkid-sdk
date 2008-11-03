/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.ApplicationEntity;


/**
 * Interface for the application manager component.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ApplicationManagerBean/local";

    /**
     * Gives back the caller application. Calling this method only makes sense in the context of an application login (via an application
     * web service).
     * 
     */
    ApplicationEntity getCallerApplication();
}
