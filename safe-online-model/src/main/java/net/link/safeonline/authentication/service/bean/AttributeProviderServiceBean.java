/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.authentication.service.AttributeProviderServiceRemote;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.ApplicationManager;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = AttributeProviderService.JNDI_BINDING)
@RemoteBinding(jndiBinding = AttributeProviderServiceRemote.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class AttributeProviderServiceBean implements AttributeProviderService, AttributeProviderServiceRemote {

    private static final Log      LOG = LogFactory.getLog(AttributeProviderServiceBean.class);

    @EJB(mappedName = ProxyAttributeService.JNDI_BINDING)
    private ProxyAttributeService proxyAttributeService;

    @EJB(mappedName = AttributeProviderDAO.JNDI_BINDING)
    private AttributeProviderDAO  attributeProviderDAO;

    @EJB(mappedName = ApplicationManager.JNDI_BINDING)
    private ApplicationManager    applicationManager;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO      attributeTypeDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService        subjectService;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO            historyDAO;


    /**
     * Check whether the caller application is an attribute provider for the given attribute type.
     * 
     * <p>
     * It's an interesting design-pattern to combine access control checking with retrieval of required entities for further processing.
     * That way you're always sure that the checks have been executed.
     * </p>
     * 
     * @param attributeName
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     */
    private AttributeTypeEntity checkAttributeProviderPermission(String attributeName)
            throws AttributeTypeNotFoundException, PermissionDeniedException {

        ApplicationEntity application = applicationManager.getCallerApplication();
        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        AttributeProviderEntity attributeProvider = attributeProviderDAO.findAttributeProvider(application, attributeType);
        if (null == attributeProvider)
            throw new PermissionDeniedException("not an attribute provider");

        return attributeType;
    }

    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public Object getAttributes(String subjectLogin, String attributeName)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeUnavailableException {

        LOG.debug("get attributes of type " + attributeName + " for subject " + subjectLogin);
        AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        return proxyAttributeService.findAttributeValue(subject, attributeType);
    }

    @SuppressWarnings("unchecked")
    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public void createAttribute(String subjectLogin, String attributeName, Object attributeValue)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, DatatypeMismatchException,
            NodeNotFoundException {

        LOG.debug("create attribute: " + attributeName + " for " + subjectLogin);
        AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.ATTRIBUTE_PROPERTY, attributeName);
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY, applicationManager.getCallerApplication().getFriendlyName());
        historyDAO.addHistoryEntry(subject, HistoryEventType.ATTRIBUTE_PROVIDER_ADD, historyProperties);

        proxyAttributeService.createAttribute(subject, attributeType, attributeValue);
    }

    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public void setAttribute(String subjectLogin, String attributeName, Object attributeValue)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeNotFoundException,
            DatatypeMismatchException, NodeNotFoundException {

        LOG.debug("set attribute " + attributeName + " for " + subjectLogin);
        AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.ATTRIBUTE_PROPERTY, attributeName);
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY, applicationManager.getCallerApplication().getFriendlyName());
        historyDAO.addHistoryEntry(subject, HistoryEventType.ATTRIBUTE_PROVIDER_CHANGE, historyProperties);

        proxyAttributeService.setAttribute(subject, attributeType, attributeValue);
    }

    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public void setCompoundAttributeRecord(String subjectLogin, String attributeName, String attributeId, Map<String, Object> memberValues)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, DatatypeMismatchException,
            AttributeNotFoundException, NodeNotFoundException {

        LOG.debug("set compound attribute " + attributeName + " for " + subjectLogin);
        AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
        if (false == attributeType.isCompounded())
            throw new DatatypeMismatchException();

        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        proxyAttributeService.setCompoundAttribute(subject, attributeType, attributeId, memberValues);
    }

    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public void removeAttribute(String subjectLogin, String attributeName)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeNotFoundException,
            NodeNotFoundException {

        LOG.debug("remove attribute " + attributeName + " from subject " + subjectLogin);
        AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.ATTRIBUTE_PROPERTY, attributeName);
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY, applicationManager.getCallerApplication().getFriendlyName());
        historyDAO.addHistoryEntry(subject, HistoryEventType.ATTRIBUTE_PROVIDER_REMOVE, historyProperties);

        proxyAttributeService.removeAttribute(subject, attributeType);
    }

    @RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
    public void removeCompoundAttributeRecord(String subjectLogin, String attributeName, String attributeId)
            throws AttributeTypeNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeNotFoundException,
            NodeNotFoundException {

        LOG.debug("remove compound attribute " + attributeName + " from subject " + subjectLogin + " with attrib Id " + attributeId);
        AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
        SubjectEntity subject = subjectService.getSubject(subjectLogin);

        proxyAttributeService.removeCompoundAttribute(subject, attributeType, attributeId);
    }
}
