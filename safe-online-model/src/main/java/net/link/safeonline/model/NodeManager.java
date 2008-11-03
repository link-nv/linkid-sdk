/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.NodeEntity;


/**
 * Interface for the node manager component.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface NodeManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeManagerBean/local";

    /**
     * Gives back the caller node. Calling this method only makes sense in the context of a node login (via a node web service).
     * 
     */
    NodeEntity getCallerNode();
}
