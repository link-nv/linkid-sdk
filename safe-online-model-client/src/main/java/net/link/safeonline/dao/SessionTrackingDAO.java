/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;


/**
 * {@link SessionTrackingEntity} data access object interface definition.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface SessionTrackingDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "SessionTrackingDAOBean/local";
}
