/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * <h2>{@link NodeMappingService} - Service for node mapping registration.</h2>
 * 
 * <p>
 * Creates node mappings for subject-node issuer pair.
 * </p>
 * 
 * <p>
 * <i>Aug 27, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Local
public interface NodeMappingService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeMappingServiceBean/local";

    /**
     * Get or create if not existing a node mapping for the specified user and node. This node mapping will be used to communicate with the
     * remote nodes
     * 
     * @param userId
     * @param nodeName
     * @return the node mapping
     * @throws SubjectNotFoundException
     * @throws NodeNotFoundException
     */
    public NodeMappingEntity getNodeMapping(String userId, String nodeName) throws SubjectNotFoundException, NodeNotFoundException;

    public NodeMappingEntity getNodeMapping(String id) throws NodeMappingNotFoundException;

    public List<NodeMappingEntity> listNodeMappings(SubjectEntity subject);
}
