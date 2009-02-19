/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.SafeOnlineNodeRoles;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAttributeService;
import net.link.safeonline.authentication.service.NodeAttributeServiceRemote;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Attribute Service Implementation for nodes.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_NODE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = NodeAttributeService.JNDI_BINDING)
@RemoteBinding(jndiBinding = NodeAttributeServiceRemote.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class NodeAttributeServiceBean implements NodeAttributeService, NodeAttributeServiceRemote {

    private static final Log      LOG = LogFactory.getLog(NodeAttributeServiceBean.class);

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService        subjectService;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO      attributeTypeDAO;

    @EJB(mappedName = ProxyAttributeService.JNDI_BINDING)
    private ProxyAttributeService proxyAttributeService;


    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public Object getAttributeValue(String subjectId, String attributeName)
            throws PermissionDeniedException, AttributeTypeNotFoundException, SubjectNotFoundException, AttributeUnavailableException {

        LOG.debug("get attribute " + attributeName + " for login " + subjectId);
        return proxyAttributeService.findAttributeValue(subjectId, attributeName);
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public void createAttribute(String subjectLogin, String attributeName, Object attributeValue)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, DatatypeMismatchException,
            NodeNotFoundException {

        LOG.debug("create attribute: " + attributeName + " for " + subjectLogin);

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        proxyAttributeService.createAttribute(subject, attributeType, attributeValue);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public Object getAttributes(String subjectLogin, String attributeName)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeUnavailableException {

        LOG.debug("get attributes of type " + attributeName + " for subject " + subjectLogin);

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        return proxyAttributeService.findAttributeValue(subject, attributeType);
    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public void removeAttribute(String subjectLogin, String attributeName)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeNotFoundException,
            NodeNotFoundException {

        LOG.debug("remove attribute " + attributeName + " from subject " + subjectLogin);
        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        proxyAttributeService.removeAttribute(subject, attributeType);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public void removeCompoundAttributeRecord(String subjectLogin, String attributeName, String attributeId)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeNotFoundException,
            NodeNotFoundException {

        LOG.debug("remove compound attribute " + attributeName + " from subject " + subjectLogin + " with attrib Id " + attributeId);

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        proxyAttributeService.removeCompoundAttribute(subject, attributeType, attributeId);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public void setAttribute(String subjectLogin, String attributeName, Object attributeValue)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeNotFoundException,
            DatatypeMismatchException, NodeNotFoundException {

        LOG.debug("set attribute " + attributeName + " for " + subjectLogin);

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        proxyAttributeService.setAttribute(subject, attributeType, attributeValue);

    }

    /**
     * {@inheritDoc}
     */
    @RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
    public void setCompoundAttributeRecord(String subjectLogin, String attributeName, String attributeId, Map<String, Object> memberValues)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, DatatypeMismatchException,
            AttributeNotFoundException, NodeNotFoundException {

        LOG.debug("set compound attribute " + attributeName + " for " + subjectLogin);

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        if (false == attributeType.isCompounded())
            throw new DatatypeMismatchException();

        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        proxyAttributeService.setCompoundAttribute(subject, attributeType, attributeId, memberValues);

    }
}
