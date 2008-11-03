/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Interface definition for subject manager component.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface SubjectManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/SubjectManagerBean/local";

    /**
     * Gives back the subject entity corresponding with the SafeOnline core security domain caller principal.
     * 
     */
    SubjectEntity getCallerSubject();

    /**
     * Gives back the subject login corresponding with the SafeOnline security domain caller principal.
     * 
     */
    String getCallerLogin();
}
