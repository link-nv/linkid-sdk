/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeProviderNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeProviderException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AttributeProviderManagerService;
import net.link.safeonline.authentication.service.AttributeProviderManagerServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class AttributeProviderManagerServiceBean implements AttributeProviderManagerService, AttributeProviderManagerServiceRemote {

    private static final Log     LOG = LogFactory.getLog(AttributeProviderManagerServiceBean.class);

    @EJB
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB
    private AttributeProviderDAO attributeProviderDAO;

    @EJB
    private ApplicationDAO       applicationDAO;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AttributeProviderEntity> getAttributeProviders(String attributeName) throws AttributeTypeNotFoundException {

        LOG.debug("get attribute providers for attribute " + attributeName);
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeName);
        List<AttributeProviderEntity> attributeProviders = this.attributeProviderDAO.listAttributeProviders(attributeType);
        return attributeProviders;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeAttributeProvider(AttributeProviderEntity attributeProvider) throws AttributeProviderNotFoundException {

        AttributeProviderEntity attachedEntity = this.attributeProviderDAO.findAttributeProvider(attributeProvider.getApplication(),
                attributeProvider.getAttributeType());
        if (null == attachedEntity)
            throw new AttributeProviderNotFoundException();
        this.attributeProviderDAO.removeAttributeProvider(attachedEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addAttributeProvider(String applicationName, String attributeName) throws ApplicationNotFoundException,
                                                                                  AttributeTypeNotFoundException,
                                                                                  ExistingAttributeProviderException,
                                                                                  PermissionDeniedException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeName);
        if (!attributeType.isLocal()) {
            throw new PermissionDeniedException("Cannot set attribute provider on remote attribute");
        }
        AttributeProviderEntity existingAttributeProvider = this.attributeProviderDAO.findAttributeProvider(application, attributeType);
        if (null != existingAttributeProvider)
            throw new ExistingAttributeProviderException();
        this.attributeProviderDAO.addAttributeProvider(application, attributeType);
    }
}
