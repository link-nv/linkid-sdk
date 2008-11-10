/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.SafeOnlineNodeRoles;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.NodeIdentifierMappingService;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.NodeManager;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_NODE_SECURITY_DOMAIN)
public class NodeIdentifierMappingServiceBean implements NodeIdentifierMappingService {

    private static final Log          LOG = LogFactory.getLog(NodeIdentifierMappingServiceBean.class);

    @EJB
    private NodeManager               nodeManager;

    @EJB
    private NodeMappingService        nodeMappingService;

    @EJB
    private NodeAuthenticationService nodeAuthenticationService;

    @EJB
    private SubjectService            subjectService;


    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public String getNodeMappingId(String username) throws NodeNotFoundException, SubjectNotFoundException {

        LOG.debug("get node mapping id: " + username);
        NodeEntity node = this.nodeManager.getCallerNode();
        SubjectEntity subject = this.subjectService.getSubjectFromUserName(username);
        NodeEntity localNode = this.nodeAuthenticationService.getLocalNode();

        if (node.equals(localNode))
            return subject.getUserId();

        NodeMappingEntity nodeMapping = this.nodeMappingService.getNodeMapping(subject.getUserId(), node.getName());
        return nodeMapping.getId();
    }
}
