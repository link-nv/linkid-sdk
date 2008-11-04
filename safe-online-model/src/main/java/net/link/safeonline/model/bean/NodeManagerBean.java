/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.SafeOnlineNodeRoles;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.dao.NodeDAO;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.model.NodeManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_NODE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = NodeManager.JNDI_BINDING)
public class NodeManagerBean implements NodeManager {

    private static final Log LOG = LogFactory.getLog(NodeManagerBean.class);

    @Resource
    private SessionContext   context;

    @EJB
    private NodeDAO          nodeDAO;


    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public NodeEntity getCallerNode() {

        Principal callerPrincipal = this.context.getCallerPrincipal();
        String nodeName = callerPrincipal.getName();
        LOG.debug("get caller node: " + nodeName);
        NodeEntity callerNode;
        try {
            callerNode = this.nodeDAO.getNode(nodeName);
        } catch (NodeNotFoundException e) {
            throw new EJBException("node not found: " + e.getMessage(), e);
        }
        return callerNode;
    }
}
