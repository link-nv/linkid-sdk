/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


/**
 * Interface for node identifier mapping service component.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface NodeIdentifierMappingService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeIdentifierMappingServiceBean/local";


    /**
     * Returns the node mapping id for the specified user and authenticating remote node.
     * 
     * @param username
     * @throws NodeNotFoundException
     * @throws SubjectNotFoundException
     */
    String getNodeMappingId(String username)
            throws NodeNotFoundException, SubjectNotFoundException;
}
