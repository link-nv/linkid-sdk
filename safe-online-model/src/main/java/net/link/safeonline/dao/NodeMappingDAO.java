/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface NodeMappingDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/NodeMappingDAOBean/local";


    public NodeMappingEntity addNodeMapping(SubjectEntity subject, NodeEntity node);

    public List<NodeMappingEntity> listNodeMappings(SubjectEntity subject);

    public NodeMappingEntity findNodeMapping(SubjectEntity subject, NodeEntity node);

    public NodeMappingEntity findNodeMapping(String id);

    public NodeMappingEntity getNodeMapping(String id)
            throws NodeMappingNotFoundException;

    public void removeNodeMappings(SubjectEntity subject);

    public List<NodeMappingEntity> listNodeMappings(NodeEntity node);

}
